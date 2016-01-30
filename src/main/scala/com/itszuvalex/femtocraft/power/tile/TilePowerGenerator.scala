package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.logistics.distributed.{DistributedManager, IWorker}
import com.itszuvalex.femtocraft.power.node.{IPowerNode, PowerNode}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.util.PlayerUtils
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound

import scala.collection.Set

/**
  * Created by Christopher Harris (Itszuvalex) on 1/28/2016.
  */
object TilePowerGenerator {
  val CONNECTION_RADIUS = 16f
  val POWER_MAXIMUM     = 2000L
  val POWER_GEN         = 20L
  val KEY_IS_DUMPING    = "Dumping"
}

class TilePowerGenerator extends TileEntityBase with IPowerGenerator with PowerNode {
  val workerPowerDumper = new WorkerPowerDumper(this, TaskDumpPower.TASK_TYPE_DUMP_POWER)
  var isDumping         = false
  var shouldRegister    = false
  powerMax = TilePowerGenerator.POWER_MAXIMUM


  override def serverUpdate(): Unit = {
    super.serverUpdate()

    if (shouldRegister) {
      shouldRegister = false
      DistributedManager.addWorkerProvider(this)
    }

    if (powerCurrent >= powerMax) {
      isDumping = true
      DistributedManager.addWorkerProvider(this)
    } else if (!isDumping) {
      powerCurrent += TilePowerGenerator.POWER_GEN
    }
  }

  override def getMod = Femtocraft

  /**
    *
    * @return Distance to accept new tasks when workers complete their own.  Do not pass high values, as this will lead to excessive blank location checking on the order of
    *         (distance/16)&#94;2
    */
  override def getTaskConnectionRadius = TilePowerGenerator.CONNECTION_RADIUS

  /**
    *
    * @return Location of this worker provider, for use in distance calculations.
    */
  override def getProviderLocation = getLoc

  override def hasDescription = true

  override def onDumpNoPower(worker: IWorker): Unit = {
    if (worker == workerPowerDumper) {
      DistributedManager.removeWorkerProvider(this)
      isDumping = false
    }
  }


  override def invalidate(): Unit = {
    super.invalidate()
    DistributedManager.removeWorkerProvider(this)
  }


  override def writeToNBT(compound: NBTTagCompound): Unit = {
    super.writeToNBT(compound)
    compound.setBoolean(TilePowerGenerator.KEY_IS_DUMPING, isDumping)
  }

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    isDumping = compound.getBoolean(TilePowerGenerator.KEY_IS_DUMPING)
    shouldRegister = isDumping
  }

  override def onSideActivate(par5EntityPlayer: EntityPlayer, side: Int): Boolean = {
    val ret = super.onSideActivate(par5EntityPlayer, side)
    if (worldObj.isRemote) return ret
    PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, "Power = " + powerCurrent + "/" + powerMax)
    PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, "Workers(" + getProvidedWorkers.size + "):")
    getProvidedWorkers.foreach { worker =>
      PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, "    Worker:" + (if (worker.getTask == null) " no task" else worker.getTask.getProvider.getProviderLocation))
                               }
    ret
  }

  /**
    *
    * @return Set of workers available to be assigned.
    */
  override def getProvidedWorkers: Set[IWorker] = if (isDumping) Set(workerPowerDumper) else Set()

  /**
    *
    * @return The type of PowerNode this is.
    */
  override def getType = IPowerNode.LONE_NODE
}
