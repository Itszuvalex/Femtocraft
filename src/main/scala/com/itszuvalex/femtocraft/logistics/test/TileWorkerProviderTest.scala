package com.itszuvalex.femtocraft.logistics.test

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.logistics.distributed.{DistributedManager, ITask, IWorker, IWorkerProvider}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.util.PlayerUtils
import net.minecraft.entity.player.EntityPlayer

/**
 * Created by Christopher Harris (Itszuvalex) on 8/15/15.
 */
class TileWorkerProviderTest extends TileEntityBase with IWorkerProvider {
  val worker: IWorker = new TestWorker(this)

  override def updateEntity(): Unit = {
    worker.onTick()
  }

  override def getMod = Femtocraft

  /**
   *
   * @return Distance to accept new tasks when workers complete their own.  Do not pass high values, as this will lead to excessive blank location checking on the order of
   *         (distance/16)&#94;2
   */
  override def getTaskConnectionRadius = 30f

  /**
   *
   * @return Set of workers available to be assigned.
   */
  override def getProvidedWorkers: Set[IWorker] = Set(worker)

  /**
   *
   * @return Location of this worker provider, for use in distance calculations.
   */
  override def getProviderLocation = getLoc

  override def invalidate(): Unit = {
    super.invalidate()
    DistributedManager.removeWorkerProvider(this)
  }

  override def validate(): Unit = {
    super.validate()
    DistributedManager.addWorkerProvider(this)
  }


  override def hasDescription = false

  override def onSideActivate(par5EntityPlayer: EntityPlayer, side: Int): Boolean = {
    val ret = super.onSideActivate(par5EntityPlayer, side)
    if (worldObj.isRemote) return ret
    PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, "Workers(" + getProvidedWorkers.size + "):")
    getProvidedWorkers.foreach { worker =>
      PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, "    Worker:" + (if (worker.getTask == null) " no task" else worker.getTask))
                               }
    ret
  }


  private class TestWorker(val provider: IWorkerProvider) extends IWorker {
    var task: ITask = null

    /**
     *
     * @param task Task to be assigned to.
     * @return True if this worker can work upon the task, false otherwise.
     */
    override def canWorkTask(task: ITask) = true

    /**
     *
     * @return The task the worker is assigned to, null otherwise.
     */
    override def getTask = task

    /**
     * Called every tick by the IWorkerProvider.
     */
    override def onTick(): Unit = {

    }

    /**
     *
     * @param attribute Attribute to ask about.
     * @return Efficiency rating for that attribute.  1d is normal.  Higher is better, lower is worse.
     */
    override def getEfficiency(attribute: String) = 1d

    /**
     *
     * @return The provider offering up this worker.
     */
    override def getProvider = provider

    /**
     *
     * @return Sets the worker to the assigned task.
     */
    override def setTask(task: ITask): Unit = {
      this.task = task
    }
  }

}
