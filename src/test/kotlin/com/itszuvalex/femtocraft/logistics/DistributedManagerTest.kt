package com.itszuvalex.femtocraft.logistics

import com.itszuvalex.femtocraft.logistics.distributed.DistributedManager
import com.itszuvalex.femtocraft.logistics.distributed.ITask
import com.itszuvalex.femtocraft.logistics.distributed.ITaskProvider
import com.itszuvalex.femtocraft.logistics.distributed.IWorker
import com.itszuvalex.femtocraft.logistics.distributed.IWorkerProvider
import com.itszuvalex.itszulib.api.utility.Loc4
import net.minecraft.core.BlockPos
import net.minecraft.resources.Identifier
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private val DIM = Identifier.parse("test")

private fun loc(x: Int) = Loc4.of(DIM, BlockPos(x, 0, 0))

private class TaskProvider(x: Int, override val workerConnectionRadius: Float = 30f) : ITaskProvider {
    val tasks = LinkedHashSet<ITask>()
    override val activeTasks: Collection<ITask> get() = tasks
    override val providerLocation: Loc4 = loc(x)

    fun add(task: Task): Task = task.also { tasks += it }
}

private class Task(
    override val provider: TaskProvider,
    override val workerCap: Int = 1,
    override val priority: Int = 0,
    override val taskType: String = "any",
    private val refuses: Boolean = false,
) : ITask {
    override val workers = LinkedHashSet<IWorker>()
    override fun addWorker(worker: IWorker): Boolean = !refuses && workers.size < workerCap && workers.add(worker)
    override fun removeWorker(worker: IWorker) {
        workers.remove(worker)
    }

    override fun cancel() {
        provider.tasks -= this
        DistributedManager.onTaskEnd(this)
    }

    override fun onTick() {}
}

private class WorkerProvider(x: Int, count: Int = 1, override val taskConnectionRadius: Float = 30f, type: String? = null) : IWorkerProvider {
    override val providerLocation: Loc4 = loc(x)
    override val providedWorkers: List<Worker> = List(count) { Worker(this, type) }
}

private class Worker(override val provider: IWorkerProvider, private val type: String?) : IWorker {
    override var task: ITask? = null
    override fun canWorkTask(task: ITask): Boolean = type == null || task.taskType == type
    override fun getEfficiency(attribute: String): Double = 1.0
    override fun inform(key: String, value: Double) {}
    override fun onTick() {}
}

class DistributedManagerTest {
    @BeforeEach
    @AfterEach
    fun clear() = DistributedManager.clear()

    @Test
    fun AddWorkerProvider_OpenTaskInRange_AssignsWorker() {
        val tp = TaskProvider(0)
        val task = tp.add(Task(tp))
        DistributedManager.addTaskProvider(tp)
        val wp = WorkerProvider(10)
        DistributedManager.addWorkerProvider(wp)
        assertSame(task, wp.providedWorkers[0].task)
        assertEquals(setOf(wp.providedWorkers[0]), task.workers)
    }

    @Test
    fun AddTaskProvider_IdleWorkerInRange_AssignsWorker() {
        val wp = WorkerProvider(10)
        DistributedManager.addWorkerProvider(wp)
        val tp = TaskProvider(0)
        val task = tp.add(Task(tp))
        DistributedManager.addTaskProvider(tp)
        assertSame(task, wp.providedWorkers[0].task)
    }

    @Test
    fun Seek_OutsideEitherRadius_DoesNotAssign() {
        val tp = TaskProvider(0, workerConnectionRadius = 5f)
        tp.add(Task(tp))
        DistributedManager.addTaskProvider(tp)
        // In the worker provider's radius, but not the task provider's.
        val wp = WorkerProvider(10, taskConnectionRadius = 30f)
        DistributedManager.addWorkerProvider(wp)
        assertNull(wp.providedWorkers[0].task)
    }

    @Test
    fun Seek_RespectsWorkerCapAndType() {
        val tp = TaskProvider(0)
        val task = tp.add(Task(tp, workerCap = 2, taskType = "mining"))
        DistributedManager.addTaskProvider(tp)
        val wrongType = WorkerProvider(1, count = 1, type = "farming")
        DistributedManager.addWorkerProvider(wrongType)
        val wp = WorkerProvider(2, count = 3)
        DistributedManager.addWorkerProvider(wp)
        assertNull(wrongType.providedWorkers[0].task)
        assertEquals(2, task.workers.size)
        assertEquals(1, wp.providedWorkers.count { it.task == null })
    }

    @Test
    fun Seek_TaskRefusesWorker_WorkerTriesTheNext() {
        val tp = TaskProvider(0)
        val refusing = tp.add(Task(tp, priority = 1, refuses = true))
        DistributedManager.addTaskProvider(tp)
        val wp = WorkerProvider(10)
        DistributedManager.addWorkerProvider(wp)
        assertNull(wp.providedWorkers[0].task)
        assertTrue(refusing.workers.isEmpty())
        // The next task in order gets the worker instead.
        val other = tp.add(Task(tp))
        DistributedManager.seekNewTasks(wp)
        assertSame(other, wp.providedWorkers[0].task)
    }

    @Test
    fun Seek_PrefersPriorityThenEmptierTask() {
        val tp = TaskProvider(0)
        val low = tp.add(Task(tp, workerCap = 4, priority = 0))
        val highA = tp.add(Task(tp, workerCap = 4, priority = 1))
        val highB = tp.add(Task(tp, workerCap = 4, priority = 1))
        DistributedManager.addTaskProvider(tp)
        // One worker at a time, so fill decides between the two high-priority tasks (v3 divided integers and tied).
        repeat(4) { DistributedManager.addWorkerProvider(WorkerProvider(10 + it)) }
        assertEquals(0, low.workers.size)
        assertEquals(2, highA.workers.size)
        assertEquals(2, highB.workers.size)
    }

    @Test
    fun OnTaskEnd_FreesWorkersWhoFindTheNextTask() {
        val tp = TaskProvider(0)
        val first = tp.add(Task(tp))
        DistributedManager.addTaskProvider(tp)
        val wp = WorkerProvider(10)
        DistributedManager.addWorkerProvider(wp)
        val second = tp.add(Task(tp))
        DistributedManager.refreshTaskStatus(tp)
        first.cancel()
        assertTrue(first.workers.isEmpty())
        assertSame(second, wp.providedWorkers[0].task)
    }

    @Test
    fun RemoveTaskProvider_FreesAllWorkers() {
        val tp = TaskProvider(0)
        val task = tp.add(Task(tp, workerCap = 3))
        DistributedManager.addTaskProvider(tp)
        val wp = WorkerProvider(10, count = 3)
        DistributedManager.addWorkerProvider(wp)
        DistributedManager.removeTaskProvider(tp)
        assertTrue(task.workers.isEmpty())
        assertTrue(wp.providedWorkers.all { it.task == null })
        assertFalse(DistributedManager.isTaskProvider(tp.providerLocation))
    }

    @Test
    fun RemoveWorkerProvider_TaskTakesAnotherWorker() {
        val tp = TaskProvider(0)
        val task = tp.add(Task(tp))
        DistributedManager.addTaskProvider(tp)
        val near = WorkerProvider(5)
        DistributedManager.addWorkerProvider(near)
        val far = WorkerProvider(20)
        DistributedManager.addWorkerProvider(far)
        assertSame(task, near.providedWorkers[0].task)
        assertNull(far.providedWorkers[0].task)
        DistributedManager.removeWorkerProvider(near)
        assertNull(near.providedWorkers[0].task)
        assertSame(task, far.providedWorkers[0].task)
    }

    @Test
    fun RemoveWorkerProvider_RemovedProviderIsNotReassigned() {
        val tp = TaskProvider(0)
        val first = tp.add(Task(tp))
        DistributedManager.addTaskProvider(tp)
        val wp = WorkerProvider(10)
        DistributedManager.addWorkerProvider(wp)
        DistributedManager.removeWorkerProvider(wp)
        tp.add(Task(tp))
        first.cancel()
        assertNull(wp.providedWorkers[0].task)
        assertFalse(DistributedManager.isWorkerProvider(wp.providerLocation))
    }
}
