package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.logistics.distributed.{ITask, IWorker}
import com.itszuvalex.femtocraft.power.node.IPowerNode

/**
  * Created by Christopher Harris (Itszuvalex) on 1/28/2016.
  */
class WorkerPowerDumper(val owner: IPowerNode with IPowerGenerator, private val taskType: String) extends IWorker {
  var task: ITask  = null
  var transferRate = 0L

  /**
    *
    * @return The provider offering up this worker.
    */
  override def getProvider = owner

  /**
    *
    * @return The task the worker is assigned to, null otherwise.
    */
  override def getTask = task

  /**
    *
    * @return Sets the worker to the assigned task.
    */
  override def setTask(task: ITask): Unit = this.task = task

  /**
    * Called every tick by the IWorkerProvider.
    */
  override def onTick(): Unit = {
    if (owner.getPowerCurrent <= 0) {
      owner.onDumpNoPower(this)
    }
  }

  /**
    *
    * @param attribute Attribute to ask about.
    * @return Efficiency rating for that attribute.  1d is normal.  Higher is better, lower is worse.
    */
  override def getEfficiency(attribute: String): Double = {
    attribute match {
      case TaskDumpPower.EFFICIENCY_POWER_TO_DUMP => owner.getPowerCurrent.toDouble
      case _ => 0.toDouble
    }
  }

  /**
    *
    * @param task Task to be assigned to.
    * @return True if this worker can work upon the task, false otherwise.
    */
  override def canWorkTask(task: ITask): Boolean = task.getTaskType.equalsIgnoreCase(taskType)

  /**
    *
    * Used by the task to inform the worker of information
    *
    * @param key   String key
    * @param value Value of string
    *
    */
  override def inform(key: String, value: Double): Unit = {
    key match {
      case TaskDumpPower.INFORM_POWER_RATE => transferRate = value.toLong
      case TaskDumpPower.INFORM_POWER_DRAINED => owner.usePower(value.toLong, doUse = true)
      case _ =>
    }
  }
}
