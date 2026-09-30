package com.itszuvalex.femtocraft.logistics.distributed

import com.itszuvalex.femtocraft.FemtoModules
import com.itszuvalex.itszulib.api.utility.Loc4
import com.itszuvalex.itszulib.api.utility.LocationTracker

/**
 * A job offered by an [ITaskProvider] that [IWorker]s from nearby [IWorkerProvider]s can take (e.g. "dump power
 * here"). Port of the 1.7.10 distributed task system.
 */
interface ITask {
    fun getTaskType(): String
    fun getPriority(): Int
    fun getProvider(): ITaskProvider
    fun getWorkerCap(): Int
    fun getWorkers(): Set<IWorker>
    fun addWorker(worker: IWorker): Boolean
    fun removeWorker(worker: IWorker)
    fun cancel()
    fun onTick()
}

interface ITaskProvider {
    fun getActiveTasks(): Set<ITask>
    fun getProviderLocation(): Loc4
    fun getWorkerConnectionRadius(): Float
}

interface IWorker {
    fun getProvider(): IWorkerProvider
    fun getTask(): ITask?
    fun canWorkTask(task: ITask): Boolean
    fun setTask(task: ITask?)

    /**
     * @return How good this worker is at [attribute] (task-type specific).
     */
    fun getEfficiency(attribute: String): Double

    /**
     * Task -> worker feedback, e.g. how much power was actually taken.
     */
    fun inform(key: String, value: Double)
    fun onTick()
}

interface IWorkerProvider {
    fun getProvidedWorkers(): Set<IWorker>
    fun getProviderLocation(): Loc4
    fun getTaskConnectionRadius(): Float
}

/**
 * Server-side matchmaker between task providers and worker providers in range of each other. Port of the 1.7.10
 * `DistributedManager`. Providers are found through [FemtoModules.TASK_PROVIDER] / [FemtoModules.WORKER_PROVIDER] on
 * loaded block entities. Cleared when the server stops.
 */
object DistributedManager {
    private val taskProviderTracker = LocationTracker()
    private val workerProviderTracker = LocationTracker()
    private val availableTasksTracker = LocationTracker()
    private val availableWorkersTracker = LocationTracker()

    private fun ITask.hasRoom() = getWorkers().size < getWorkerCap()

    /**
     * Default ordering: higher priority first, then less filled, then closer to [origin].
     */
    private fun ordering(origin: Loc4): Comparator<ITask> =
        compareByDescending<ITask> { it.getPriority() }
            .thenBy { it.getWorkers().size.toDouble() / it.getWorkerCap() }
            .thenBy { it.getProvider().getProviderLocation().distSqr(origin) }

    fun addTaskProvider(provider: ITaskProvider) {
        taskProviderTracker.trackLocation(provider.getProviderLocation())
        seekNewWorkers(provider)
    }

    fun addWorkerProvider(provider: IWorkerProvider) {
        workerProviderTracker.trackLocation(provider.getProviderLocation())
        seekNewTasks(provider)
    }

    /**
     * Assigns idle workers in range to [provider]'s open tasks.
     */
    fun seekNewWorkers(provider: ITaskProvider, order: Comparator<ITask> = ordering(provider.getProviderLocation())) {
        val origin = provider.getProviderLocation()
        val availableWorkers = availableWorkersTracker.getLocationsInRange(origin, provider.getWorkerConnectionRadius())
            .mapNotNull { it.getIBlockEntity(false)?.getModule(FemtoModules.WORKER_PROVIDER, null) }
            .filter { wp -> wp.getProviderLocation().distSqr(origin) < wp.getTaskConnectionRadius() * wp.getTaskConnectionRadius() }
            .sortedBy { it.getProviderLocation().distSqr(origin) }
            .flatMap { wp -> wp.getProvidedWorkers().filter { it.getTask() == null } }
            .toList()
        val availableTasks = provider.getActiveTasks().filter { it.hasRoom() }.sortedWith(order)
        val workerProviders = HashSet<IWorkerProvider>()
        availableWorkers.forEach { worker ->
            val task = availableTasks.firstOrNull { worker.canWorkTask(it) && it.hasRoom() } ?: return@forEach
            worker.setTask(task)
            task.addWorker(worker)
            workerProviders += worker.getProvider()
        }
        refreshTaskStatus(provider)
        workerProviders.forEach(::refreshWorkerStatus)
    }

    /**
     * Assigns [provider]'s idle workers to open tasks in range.
     */
    fun seekNewTasks(provider: IWorkerProvider, order: Comparator<ITask> = ordering(provider.getProviderLocation())) {
        val origin = provider.getProviderLocation()
        val availableTasks = availableTasksTracker.getLocationsInRange(origin, provider.getTaskConnectionRadius())
            .mapNotNull { it.getIBlockEntity(false)?.getModule(FemtoModules.TASK_PROVIDER, null) }
            .filter { tp -> tp.getProviderLocation().distSqr(origin) < tp.getWorkerConnectionRadius() * tp.getWorkerConnectionRadius() }
            .flatMap { it.getActiveTasks() }
            .filter { it.hasRoom() }
            .sortedWith(order)
            .toList()
        val taskProviders = HashSet<ITaskProvider>()
        provider.getProvidedWorkers().filter { it.getTask() == null }.forEach { worker ->
            val task = availableTasks.firstOrNull { worker.canWorkTask(it) && it.hasRoom() } ?: return@forEach
            worker.setTask(task)
            task.addWorker(worker)
            taskProviders += task.getProvider()
        }
        refreshWorkerStatus(provider)
        taskProviders.forEach(::refreshTaskStatus)
    }

    fun refreshWorkerStatus(provider: IWorkerProvider) {
        if (provider.getProvidedWorkers().any { it.getTask() == null }) availableWorkersTracker.trackLocation(provider.getProviderLocation())
        else availableWorkersTracker.removeLocation(provider.getProviderLocation())
    }

    fun refreshTaskStatus(provider: ITaskProvider) {
        if (provider.getActiveTasks().any { it.hasRoom() }) availableTasksTracker.trackLocation(provider.getProviderLocation())
        else availableTasksTracker.removeLocation(provider.getProviderLocation())
    }

    /**
     * Frees [task]'s workers and lets them look for new tasks.
     */
    fun onTaskEnd(task: ITask) {
        val workerProviders = HashSet<IWorkerProvider>()
        task.getWorkers().toList().forEach { worker ->
            task.removeWorker(worker)
            worker.setTask(null)
            workerProviders += worker.getProvider()
        }
        refreshTaskStatus(task.getProvider())
        workerProviders.forEach { seekNewTasks(it) }
    }

    fun removeTaskProvider(provider: ITaskProvider) {
        val workerProviders = HashSet<IWorkerProvider>()
        provider.getActiveTasks().forEach { task ->
            task.getWorkers().toList().forEach { worker ->
                workerProviders += worker.getProvider()
                task.removeWorker(worker)
                worker.setTask(null)
            }
        }
        taskProviderTracker.removeLocation(provider.getProviderLocation())
        availableTasksTracker.removeLocation(provider.getProviderLocation())
        workerProviders.forEach { seekNewTasks(it) }
    }

    fun removeWorkerProvider(provider: IWorkerProvider) {
        val taskProviders = HashSet<ITaskProvider>()
        provider.getProvidedWorkers().forEach { worker ->
            val task = worker.getTask() ?: return@forEach
            taskProviders += task.getProvider()
            task.removeWorker(worker)
            worker.setTask(null)
        }
        workerProviderTracker.removeLocation(provider.getProviderLocation())
        availableWorkersTracker.removeLocation(provider.getProviderLocation())
        taskProviders.forEach { seekNewWorkers(it) }
    }

    fun clear() {
        taskProviderTracker.clear()
        workerProviderTracker.clear()
        availableTasksTracker.clear()
        availableWorkersTracker.clear()
    }
}
