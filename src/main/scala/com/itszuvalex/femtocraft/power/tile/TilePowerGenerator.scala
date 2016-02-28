package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.logistics.distributed.{DistributedManager, IWorker}
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
  val KEY_POWER_CURRENT = "CurrentPower"
}

class TilePowerGenerator extends TileEntityBase with IPowerGenerator {
  val workerPowerDumper = new WorkerPowerDumper(this, TaskDumpPower.TASK_TYPE_DUMP_POWER)
  val powerMax          = TilePowerGenerator.POWER_MAXIMUM
  var isDumping         = false
  var shouldRegister    = false
  var powerCurrent      = 0d


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
      charge(TilePowerGenerator.POWER_GEN, doCharge = true)
    }
  }

  /**
    *
    * @param amt      Amount to attempt to charge
    * @param doCharge False to simulate, true to actually do
    * @return Amount of amt used to actually charge.
    */
  override def charge(amt: Double, doCharge: Boolean): Double = {
    val amtd = Math.min(amt, getMaximumPower - getCurrentPower)
    if (doCharge) {
      powerCurrent += amtd
    }
    amtd
  }

  override def getCurrentPower = powerCurrent

  override def getMaximumPower = powerMax

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
    compound.setDouble(TilePowerGenerator.KEY_POWER_CURRENT, powerCurrent)
  }

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    isDumping = compound.getBoolean(TilePowerGenerator.KEY_IS_DUMPING)
    shouldRegister = isDumping
    powerCurrent = compound.getDouble(TilePowerGenerator.KEY_POWER_CURRENT)
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
    * @param amt     Amount of power to drain
    * @param doDrain False to simulate, true to actually remove power.
    * @return Amount of amt that was successfully drained.
    */
  override def drain(amt: Double, doDrain: Boolean): Double = {
    val amtd = Math.min(amt, powerCurrent)
    if (doDrain) {
      powerCurrent -= amtd
    }
    amtd
  }
}
