package com.itszuvalex.femtocraft.logistics.distributed

import com.itszuvalex.itszulib.api.utility.LocationTracker
import com.itszuvalex.itszulib.api.utility.Loc4

/**
 * A unit of work hosted by an [ITaskProvider] and carried out by [IWorker]s. Port of v3's `ITask`.
 */
interface ITask {
    /** Kind of task, for [IWorker.canWorkTask]. */
    val taskType: String

    /** Higher values are assigned workers first. */
    val priority: Int

    /** The provider hosting this task. */
    val provider: ITaskProvider

    /** Maximum number of workers assigned at once. */
    val workerCap: Int

    /** Workers assigned to this task. */
    val workers: Set<IWorker>

    /**
     * @return True if [worker] was assigned, false if it cannot be (wrong type, full).
     */
    fun addWorker(worker: IWorker): Boolean

    fun removeWorker(worker: IWorker)

    /**
     * Ends the task: removes it from its provider's [ITaskProvider.activeTasks], then calls
     * [DistributedManager.onTaskEnd].
     */
    fun cancel()

    /** Called every tick by the provider. */
    fun onTick()
}

/**
 * Hosts [ITask]s. Register with [DistributedManager.addTaskProvider] when loaded and remove with
 * [DistributedManager.removeTaskProvider] when unloaded or removed. Port of v3's `ITaskProvider`.
 */
interface ITaskProvider {
    val activeTasks: Collection<ITask>

    val providerLocation: Loc4

    /**
     * How far away workers may be. Keep it small: finding workers checks on the order of (radius / 16)^2 chunks.
     */
    val workerConnectionRadius: Float
}

/**
 * Something that carries out [ITask]s, offered by an [IWorkerProvider]. Port of v3's `IWorker`.
 */
interface IWorker {
    val provider: IWorkerProvider

    /** The task this worker is assigned to, or null. Set by [DistributedManager]. */
    var task: ITask?

    fun canWorkTask(task: ITask): Boolean

    /**
     * Efficiency for [attribute]: 1.0 is normal, higher is better.
     */
    fun getEfficiency(attribute: String): Double

    /** Lets a task pass information to its worker. */
    fun inform(key: String, value: Double)

    /** Called every tick by the provider. */
    fun onTick()
}

/**
 * Offers [IWorker]s. Register with [DistributedManager.addWorkerProvider] when loaded and remove with
 * [DistributedManager.removeWorkerProvider] when unloaded or removed. Port of v3's `IWorkerProvider`.
 */
interface IWorkerProvider {
    val providedWorkers: Collection<IWorker>

    val providerLocation: Loc4

    /**
     * How far away tasks may be. Keep it small: finding tasks checks on the order of (radius / 16)^2 chunks.
     */
    val taskConnectionRadius: Float
}

/**
 * Matches idle workers with open tasks in range. Port of v3's `DistributedManager`; server side only, cleared when
 * the server stops.
 *
 * A worker and a task match when each is within the other provider's radius and [IWorker.canWorkTask] accepts the
 * task. Tasks are tried by highest priority, then fewest workers relative to their cap, then distance.
 *
 * Unlike v3, providers are remembered by location here instead of being looked up as tile entities, and:
 * - fill (workers / cap) is compared as a fraction (v3 divided integers, so every task that was not full tied);
 * - a worker is only assigned when [ITask.addWorker] accepts it (v3 ignored the result);
 * - workers are copied before being unassigned (v3 removed them from the set it was iterating).
 */
object DistributedManager {
    private val taskProviders = HashMap<Loc4, ITaskProvider>()
    private val workerProviders = HashMap<Loc4, IWorkerProvider>()

    /** Task providers with a task that has room for more workers. */
    private val availableTasks = LocationTracker()

    /** Worker providers with an idle worker. */
    private val availableWorkers = LocationTracker()

    fun addTaskProvider(provider: ITaskProvider) {
        taskProviders[provider.providerLocation] = provider
        seekNewWorkers(provider)
    }

    fun addWorkerProvider(provider: IWorkerProvider) {
        workerProviders[provider.providerLocation] = provider
        seekNewTasks(provider)
    }

    fun <T> addDualProvider(provider: T) where T : ITaskProvider, T : IWorkerProvider {
        taskProviders[provider.providerLocation] = provider
        workerProviders[provider.providerLocation] = provider
        seekNewWorkers(provider)
        seekNewTasks(provider)
    }

    fun isTaskProvider(loc: Loc4): Boolean = loc in taskProviders

    fun isWorkerProvider(loc: Loc4): Boolean = loc in workerProviders

    private fun taskOrder(from: Loc4): Comparator<ITask> =
        compareByDescending<ITask> { it.priority }
            .thenBy { it.workers.size.toDouble() / it.workerCap }
            .thenBy { it.provider.providerLocation.distSqr(from) }

    private fun ITask.hasRoom(): Boolean = workers.size < workerCap

    private fun inRange(a: Loc4, b: Loc4, radius: Float): Boolean = a.distSqr(b) <= radius.toDouble() * radius

    /** Assigns [worker] to the first task in [tasks] it can work that accepts it. @return The task, or null. */
    private fun assign(worker: IWorker, tasks: List<ITask>): ITask? {
        val task = tasks.firstOrNull { it.hasRoom() && worker.canWorkTask(it) && it.addWorker(worker) } ?: return null
        worker.task = task
        return task
    }

    /**
     * Gives [provider]'s idle workers open tasks in range. Call when a worker becomes idle.
     */
    fun seekNewTasks(provider: IWorkerProvider, order: Comparator<ITask> = taskOrder(provider.providerLocation)) {
        val loc = provider.providerLocation
        val tasks = availableTasks.getLocationsInRange(loc, provider.taskConnectionRadius)
            .mapNotNull { taskProviders[it] }
            .filter { inRange(it.providerLocation, loc, it.workerConnectionRadius) }
            .flatMap { it.activeTasks }
            .filter { it.hasRoom() }
            .sortedWith(order)
            .toList()
        val touched = HashSet<ITaskProvider>()
        provider.providedWorkers.filter { it.task == null }.forEach { worker ->
            assign(worker, tasks)?.let { touched += it.provider }
        }
        refreshWorkerStatus(provider)
        touched.forEach(::refreshTaskStatus)
    }

    /**
     * Gives [provider]'s open tasks idle workers in range, nearest providers first. Call when a task is added.
     */
    fun seekNewWorkers(provider: ITaskProvider, order: Comparator<ITask> = taskOrder(provider.providerLocation)) {
        val loc = provider.providerLocation
        val workers = availableWorkers.getLocationsInRange(loc, provider.workerConnectionRadius)
            .mapNotNull { workerProviders[it] }
            .filter { inRange(it.providerLocation, loc, it.taskConnectionRadius) }
            .sortedBy { it.providerLocation.distSqr(loc) }
            .flatMap { wp -> wp.providedWorkers.filter { it.task == null } }
            .toList()
        val tasks = provider.activeTasks.filter { it.hasRoom() }.sortedWith(order)
        val touched = HashSet<IWorkerProvider>()
        workers.forEach { worker ->
            if (assign(worker, tasks) != null) touched += worker.provider
        }
        refreshTaskStatus(provider)
        touched.forEach(::refreshWorkerStatus)
    }

    fun refreshWorkerStatus(provider: IWorkerProvider) {
        if (provider.providedWorkers.any { it.task == null }) availableWorkers.trackLocation(provider.providerLocation)
        else availableWorkers.removeLocation(provider.providerLocation)
    }

    fun refreshTaskStatus(provider: ITaskProvider) {
        if (provider.activeTasks.any { it.hasRoom() }) availableTasks.trackLocation(provider.providerLocation)
        else availableTasks.removeLocation(provider.providerLocation)
    }

    /**
     * Call when [task] ends (finished or cancelled), after removing it from its provider's
     * [ITaskProvider.activeTasks]. Its workers look for new tasks. Use [removeTaskProvider] when the whole provider
     * goes away.
     */
    fun onTaskEnd(task: ITask) {
        val freed = unassignAll(task)
        if (task.provider.providerLocation in taskProviders) refreshTaskStatus(task.provider)
        freed.forEach(::reseek)
    }

    fun removeTaskProvider(provider: ITaskProvider) {
        val freed = HashSet<IWorkerProvider>()
        provider.activeTasks.forEach { freed += unassignAll(it) }
        taskProviders.remove(provider.providerLocation)
        availableTasks.removeLocation(provider.providerLocation)
        freed.forEach(::reseek)
    }

    fun removeWorkerProvider(provider: IWorkerProvider) {
        val left = HashSet<ITaskProvider>()
        provider.providedWorkers.forEach { worker ->
            val task = worker.task ?: return@forEach
            left += task.provider
            task.removeWorker(worker)
            worker.task = null
        }
        workerProviders.remove(provider.providerLocation)
        availableWorkers.removeLocation(provider.providerLocation)
        left.forEach { if (taskProviders[it.providerLocation] === it) seekNewWorkers(it) }
    }

    /** Freed workers look for new tasks, unless their provider was removed meanwhile. */
    private fun reseek(provider: IWorkerProvider) {
        if (workerProviders[provider.providerLocation] === provider) seekNewTasks(provider)
    }

    /** Unassigns every worker of [task]. @return Their providers. */
    private fun unassignAll(task: ITask): Set<IWorkerProvider> =
        task.workers.toList().mapTo(HashSet()) { worker ->
            task.removeWorker(worker)
            worker.task = null
            worker.provider
        }

    fun clear() {
        taskProviders.clear()
        workerProviders.clear()
        availableTasks.clear()
        availableWorkers.clear()
    }
}
