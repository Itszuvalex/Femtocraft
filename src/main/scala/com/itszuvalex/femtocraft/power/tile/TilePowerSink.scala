package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.logistics.distributed.{DistributedManager, ITask, ITaskProvider}
import com.itszuvalex.femtocraft.power.node.{IPowerNode, PowerNode}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.util.PlayerUtils
import net.minecraft.entity.player.EntityPlayer

import scala.collection.Set

/**
  * Created by Christopher Harris (Itszuvalex) on 1/28/2016.
  */
object TilePowerSink {
  val POWER_MAX               = 10000L
  val POWER_CONNECTION_RADIUS = 16f
}

class TilePowerSink extends TileEntityBase with ITaskProvider with PowerNode {
  val taskDumpPower = new TaskDumpPower(this, TaskDumpPower.TASK_TYPE_DUMP_POWER, 5L, 1, 0)

  powerMax = TilePowerSink.POWER_MAX

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

  /**
    *
    * @return The type of PowerNode this is.
    */
  override def getType = IPowerNode.LONE_NODE


  override def serverUpdate(): Unit = {
    super.serverUpdate()
    taskDumpPower.onTick()
  }

  override def onSideActivate(par5EntityPlayer: EntityPlayer, side: Int): Boolean = {
    val ret = super.onSideActivate(par5EntityPlayer, side)
    if (worldObj.isRemote) return ret
    PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, "Power = " + powerCurrent + "/" + powerMax)
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

  /* Tile Entity */
  override def validate(): Unit = {
    super.validate()
    DistributedManager.addTaskProvider(this)
  }

  override def invalidate(): Unit = {
    super.invalidate()
    DistributedManager.removeTaskProvider(this)
  }
}
