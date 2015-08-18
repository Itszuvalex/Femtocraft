package com.itszuvalex.femtocraft.logistics.test

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.logistics.distributed._
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.util.PlayerUtils
import net.minecraft.entity.player.EntityPlayer

import scala.collection._

/**
 * Created by Christopher Harris (Itszuvalex) on 8/15/15.
 */
class TileTaskProviderTest extends TileEntityBase with ITaskProvider {
  val tasks = new mutable.HashSet[ITask]()

  override def updateEntity(): Unit = {
    if(worldObj.isRemote) return

    getActiveTasks.foreach(_.onTick())

    if (tasks.isEmpty) {
      tasks += new TestTask(this)
      DistributedManager.seekNewWorkers(this)
    }
  }

  override def getMod = Femtocraft

  /**
   *
   * @return Set of tasks hosted by the provider.
   */
  override def getActiveTasks = tasks

  /**
   *
   * @return Location of this provider, for use in distance calculations.
   */
  override def getProviderLocation = getLoc

  /**
   *
   * @return Distance to accept new workers when task completes.  Do not pass high values, as this will lead to excessive blank location checking on the order of
   *         (distance/16)&#94;2
   */
  override def getWorkerConnectionRadius = 30f

  override def hasDescription = false


  override def invalidate(): Unit = {
    super.invalidate()
    if(worldObj.isRemote) return
    DistributedManager.removeTaskProvider(this)
  }

  override def validate(): Unit = {
    super.validate()
    if(worldObj.isRemote) return
    DistributedManager.addTaskProvider(this)
  }


  override def onSideActivate(par5EntityPlayer: EntityPlayer, side: Int): Boolean = {
    val ret = super.onSideActivate(par5EntityPlayer, side)
    if (worldObj.isRemote) return ret
    PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, "Tasks(" + tasks.size + "):")
    tasks.collect { case task: TestTask =>
      PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, "    Task:  workers:" + task.getWorkers.size + "-" + task.getWorkerCap + "   progress:" + task.progress + "-" + task.progressToFinish)
      task.getWorkers.foreach { worker =>
        PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, "       Worker:" + worker.getProvider.getProviderLocation)
                              }
                  }
    ret
  }

  class TestTask(val provider: TileTaskProviderTest) extends ITask {
    var progressF        = 0.0
    var progress         = 0
    val progressToFinish = 20
    val workers          = new mutable.HashSet[IWorker]()

    /**
     *
     * @param worker Worker to add.
     * @return True if worker successfully assigned, false otherwise (incompatible type, storage is full.)
     */
    override def addWorker(worker: IWorker): Boolean = {
      if (workers.size >= getWorkerCap) false
      else {
        workers += worker
        true
      }
    }

    /**
     *
     * @return Maximum number of workers to be assigned to this task.
     */
    override def getWorkerCap: Int = 1

    /**
     * Called every tick by the ITaskProvider.
     */
    override def onTick(): Unit = {
      progressF += workers.size * .05
      progress = math.floor(progressF).toInt
      if (progress >= progressToFinish) {
        cancel()
      }
    }

    /**
     * Cancels the task.  Removes all workers, removes it from ITaskProvider's open task list.
     */
    override def cancel(): Unit = {
      provider.tasks -= this
      DistributedManager.onTaskEnd(this)
    }

    /**
     *
     * @return String identification of 'type' of task.  Used to check for worker suitability.
     */
    override def getTaskType: String = "Some type"

    /**
     *
     * @return The Provider hosting this task.
     */
    override def getProvider = provider

    /**
     *
     * @return Task priority.  Higher values are higher priority.
     */
    override def getPriority: Int = 0

    /**
     *
     * @param worker Worker to remove.
     */
    override def removeWorker(worker: IWorker): Unit = {
      workers -= worker
    }

    /**
     *
     * @return Set of Workers assigned to this task.
     */
    override def getWorkers = workers
  }

}
