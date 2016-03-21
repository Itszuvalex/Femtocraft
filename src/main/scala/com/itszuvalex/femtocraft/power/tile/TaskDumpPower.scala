package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.logistics.distributed.{DistributedManager, ITask, IWorker}

import scala.collection.Set

/**
  * Created by Christopher Harris (Itszuvalex) on 1/28/2016.
  */
object TaskDumpPower {
  val TASK_TYPE_DUMP_POWER     = "Dump Power"
  val EFFICIENCY_POWER_TO_DUMP = "Power"
  val INFORM_POWER_RATE        = "Rate"
  val INFORM_POWER_DRAINED     = "Drained"
}

class TaskDumpPower(val owner: IPowerSink, private val taskType: String, val transferRate: Long, private val workerCap: Int, private val priority: Int) extends ITask {
  val workers = new scala.collection.mutable.HashSet[IWorker]()

  /**
    *
    * @return String identification of 'type' of task.  Used to check for worker suitability.
    */
  override def getTaskType = taskType

  /**
    * Called every tick by the ITaskProvider.
    */
  override def onTick(): Unit = {
    workers.foreach { worker =>
      worker.inform(TaskDumpPower.INFORM_POWER_RATE, transferRate.toDouble)
      val powerToDump = worker.getEfficiency(TaskDumpPower.EFFICIENCY_POWER_TO_DUMP).toLong
      var power = Math.min(powerToDump, transferRate)
      power = Math.min(power, owner.getMaximumPower.toLong - owner.getCurrentPower.toLong)
      worker.inform(TaskDumpPower.INFORM_POWER_DRAINED, power.toDouble)
      owner.charge(power, doCharge = true)
      worker.onTick()
                    }

  }

  /**
    *
    * @param worker Worker to remove.
    */
  override def removeWorker(worker: IWorker): Unit = workers -= worker

  /**
    * Cancels the task.  Removes all workers, removes it from ITaskProvider's open task list.
    */
  override def cancel(): Unit = {
    DistributedManager.onTaskEnd(this)
  }

  /**
    *
    * @return The Provider hosting this task.
    */
  override def getProvider = owner

  /**
    *
    * @return Task priority.  Higher values are higher priority.
    */
  override def getPriority = (owner.getMaximumPower - owner.getCurrentPower).toInt

  /**
    *
    * @param worker Worker to add.
    * @return True if worker successfully assigned, false otherwise (incompatible type, storage is full.)
    */
  override def addWorker(worker: IWorker): Boolean = if (workers.size >= getWorkerCap) false
  else {
    workers += worker
    true
  }

  /**
    *
    * @return Maximum number of workers to be assigned to this task.
    */
  override def getWorkerCap = workerCap

  /**
    *
    * @return Set of Workers assigned to this task.
    */
  override def getWorkers: Set[IWorker] = workers
}
