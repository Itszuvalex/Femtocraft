package com.itszuvalex.femtocraft.logistics.distributed

/**
 * Created by Christopher on 8/15/2015.
 */
trait IWorker {
  /*
    /**
     *
     * @return UUID identification of worker.  This should persist through save/load.
     */
    def getID: String
  */

  /**
   *
   * @return The provider offering up this worker.
   */
  def getProvider: IWorkerProvider

  /**
   *
   * @return The task the worker is assigned to, null otherwise.
   */
  def getTask: ITask

  /**
   *
   * @param task Task to be assigned to.
   * @return True if this worker can work upon the task, false otherwise.
   */
  def canWorkTask(task: ITask) : Boolean

  /**
   *
   * @return Sets the worker to the assigned task.
   */
  def setTask(task: ITask): Unit

  /**
   *
   * @param attribute Attribute to ask about.
   * @return Efficiency rating for that attribute.  1d is normal.  Higher is better, lower is worse.
   */
  def getEfficiency(attribute: String): Double

  /**
   * Called every tick by the IWorkerProvider.
   */
  def onTick(): Unit

}
