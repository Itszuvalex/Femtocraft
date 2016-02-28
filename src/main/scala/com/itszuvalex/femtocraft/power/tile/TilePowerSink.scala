package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.logistics.distributed.{DistributedManager, ITask}
import com.itszuvalex.femtocraft.power.{ICrystalMount, IPowerPedestal}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.util.PlayerUtils
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.common.util.ForgeDirection

import scala.collection.Set

/**
  * Created by Christopher Harris (Itszuvalex) on 1/28/2016.
  */
object TilePowerSink {
  val POWER_CONNECTION_RADIUS = 16f
}

class TilePowerSink extends TileEntityBase with IPowerSink {
  val taskDumpPower = new TaskDumpPower(this, TaskDumpPower.TASK_TYPE_DUMP_POWER, 5L, 1, 0)

  override def getMod: AnyRef = Femtocraft

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
  override def getWorkerConnectionRadius = TilePowerSink.POWER_CONNECTION_RADIUS

  override def hasDescription: Boolean = true


  override def serverUpdate(): Unit = {
    super.serverUpdate()
    taskDumpPower.onTick()
  }

  override def onSideActivate(par5EntityPlayer: EntityPlayer, side: Int): Boolean = {
    val ret = super.onSideActivate(par5EntityPlayer, side)
    if (worldObj.isRemote) return ret
    PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, "Power = " + getCurrentPower + "/" + getMaximumPower)
    PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, "Tasks(" + getActiveTasks.size + "):")
    getActiveTasks.collect { case task: ITask =>
      PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, "    Task:  workers:" + task.getWorkers.size + "-" + task.getWorkerCap)
      task.getWorkers.foreach { worker =>
        PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, "       Worker:" + worker.getProvider.getProviderLocation)
                              }
                           }
    ret
  }

  /**
    *
    * @return Set of tasks hosted by the provider.
    */
  override def getActiveTasks: Set[ITask] = Set(taskDumpPower)

  override def getCurrentPower: Double = getPedestal.flatMap(_.mountLoc.getTileEntity(true)).collect { case i: ICrystalMount => i }.map(_.getPowerCurrent).getOrElse(0)

  override def getMaximumPower: Double = getPedestal.flatMap(_.mountLoc.getTileEntity(true)).collect { case i: ICrystalMount => i }.map(_.getPowerMax).getOrElse(0)

  /* Tile Entity */
  override def validate(): Unit = {
    super.validate()
    DistributedManager.addTaskProvider(this)
  }

  override def invalidate(): Unit = {
    super.invalidate()
    DistributedManager.removeTaskProvider(this)
  }

  /**
    *
    * @param amt      Amount to attempt to charge
    * @param doCharge False to simulate, true to actually do
    * @return Amount of amt used to actually charge.
    */
  override def charge(amt: Double, doCharge: Boolean): Double = getPedestal.flatMap(_.mountLoc.getTileEntity(true)).collect { case i: ICrystalMount => i }.map(_.addPower(amt, doCharge)).getOrElse(0)

  /**
    *
    * @param amt     Amount of power to drain
    * @param doDrain False to simulate, true to actually remove power.
    * @return Amount of amt that was successfully drained.
    */
  override def drain(amt: Double, doDrain: Boolean): Double = getPedestal.flatMap(_.mountLoc.getTileEntity(true)).collect { case i: ICrystalMount => i }.map(_.usePower(amt, doDrain)).getOrElse(0)

  def getPedestal: Option[IPowerPedestal] = {
    var loc = getLoc.getOffset(ForgeDirection.UP)
    if (!loc.getTileEntity(true).exists(_.isInstanceOf[IPowerPedestal])) {
      loc = getLoc.getOffset(ForgeDirection.DOWN)
      if (!loc.getTileEntity(true).exists(_.isInstanceOf[IPowerPedestal])) {
        loc = null
      }
    }
    Option(loc).flatMap(_.getTileEntity(true)).collect { case i: IPowerPedestal => i }
  }
}
