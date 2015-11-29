package com.itszuvalex.femtocraft.logistics.test

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.logistics.distributed.{DistributedManager, ITask, IWorker, IWorkerProvider}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.TileDescriptionPacket
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._
import com.itszuvalex.itszulib.render.Vector3
import com.itszuvalex.itszulib.util.PlayerUtils
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.AxisAlignedBB

import scala.collection._

/**
  * Created by Christopher Harris (Itszuvalex) on 8/15/15.
  */
class TileWorkerProviderTest extends TileEntityBase with IWorkerProvider with ILogisticsConnected with TileDescriptionPacket {
  val workers = new mutable.HashSet[IWorker]()
  workers += new TestWorker(this)
  val connections = new mutable.HashSet[Loc4]()

  override def getConnections: Set[Loc4] = {
    if (worldObj.isRemote) connections
    else {
      workers.flatMap(worker => if (worker.getTask == null) None else Option(worker.getTask.getProvider.getProviderLocation))
    }
  }


  def saveConnectionInfo(compound: NBTTagCompound) =
    compound("connections" ->
             NBTCompound(
                          "tagList" -> NBTList(getConnections.map(NBTCompound))
                        )
            )

  def loadConnectionInfo(compound: NBTTagCompound) =
    compound.NBTCompound("connections") { comp =>
      connections.clear()
      connections ++= comp.NBTList("tagList").map(Loc4(_))
                                        }


  override def updateEntity(): Unit = {
    if (worldObj.isRemote) return
    getProvidedWorkers.foreach(_.onTick())
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
  override def getProvidedWorkers: Set[IWorker] = workers

  /**
    *
    * @return Location of this worker provider, for use in distance calculations.
    */
  override def getProviderLocation = getLoc

  override def getRenderBoundingBox: AxisAlignedBB = {
    val center = Vector3(xCoord + .5f, yCoord + .5f, zCoord + .5f)
    AxisAlignedBB.getBoundingBox(center.x - 30,
                                 center.y - 30,
                                 center.z - 30,
                                 center.x + 30,
                                 center.y + 30,
                                 center.z + 30)
  }


  override def invalidate(): Unit = {
    super.invalidate()
    if (worldObj.isRemote) return
    DistributedManager.removeWorkerProvider(this)
  }

  override def validate(): Unit = {
    super.validate()
    if (worldObj.isRemote) return
    DistributedManager.addWorkerProvider(this)
  }

  override def hasDescription: Boolean = true

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    saveConnectionInfo(compound)
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    loadConnectionInfo(compound)
    setRenderUpdate()
  }

  override def onSideActivate(par5EntityPlayer: EntityPlayer, side: Int): Boolean = {
    val ret = super.onSideActivate(par5EntityPlayer, side)
    if (worldObj.isRemote) return ret
    PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, "Workers(" + getProvidedWorkers.size + "):")
    getProvidedWorkers.foreach { worker =>
      PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, "    Worker:" + (if (worker.getTask == null) " no task" else worker.getTask.getProvider.getProviderLocation))
                               }
    ret
  }


  private class TestWorker(val provider: TileWorkerProviderTest) extends IWorker {
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
      provider.setUpdate()
    }
  }

}
