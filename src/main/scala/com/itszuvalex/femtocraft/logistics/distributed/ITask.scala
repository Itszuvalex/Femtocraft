package com.itszuvalex.femtocraft.logistics.distributed

/**
 * Created by Christopher on 8/15/2015.
 */
trait ITask {

  private var ticks : Int = 0

  /*  /**
     *
     * @return UUID identification of task.  This should persist through save/load, and should not repeat across separate instances of the task.
     */
    def getID: String
  */
  /**
   *
   * @return String identification of 'type' of task.  Used to check for worker suitability.
   */
  def getTaskType: String

  /**
   *
   * @return Task priority.  Higher values are higher priority.
   */
  def getPriority: Int

  /**
   *
   * @return The Provider hosting this task.
   */
  def getProvider: ITaskProvider

  /**
   *
   * @return Maximum number of workers to be assigned to this task.
   */
  def getWorkerCap: Int

  /**
   *
   * @return Set of Workers assigned to this task.
   */
  def getWorkers: scala.collection.Set[IWorker]

  /**
   *
   * @param worker Worker to add.
   * @return True if worker successfully assigned, false otherwise (incompatible type, storage is full.)
   */
  def addWorker(worker: IWorker): Boolean

  /**
   *
   * @param worker Worker to remove.
   */
  def removeWorker(worker: IWorker): Unit

  /**
   * Cancels the task.  Removes all workers, removes it from ITaskProvider's open task list.
   */
  def cancel(): Unit

  /**
   * Called every tick by the ITaskProvider.
   */
  def onTick(): Unit

}
