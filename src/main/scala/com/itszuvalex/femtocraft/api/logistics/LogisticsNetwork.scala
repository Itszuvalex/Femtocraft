package com.itszuvalex.femtocraft.api.logistics

import java.util

import com.google.common.collect.TreeMultiset
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.itszulib.logistics.{ManagerNetwork, TileNetwork}
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

import scala.collection.JavaConversions._

/**
  * Created by Chris on 2/19/2017.
  */
class LogisticsNetwork extends TileNetwork[ILogisticsNetworkNode, LogisticsNetwork](ManagerNetwork.getNextID) {

  def channels: util.Collection[String] = Set[String]()

  override def networkCapability: Capability[ILogisticsNetworkNode] = Capabilities.TILE_LOGISTICS_NODE

  override def create(): LogisticsNetwork = new LogisticsNetwork

  override def onTickStart(): Unit = {}

  override def onTickEnd(): Unit = {
    LogisticsResourceRegistry.getResources.foreach { resource =>
      resourceLoop(resource)
    }
  }

  private def resourceLoop[T](resource: IResource[T]) = {
    val connections = nodeMap.values.flatMap(node => EnumFacing.VALUES.flatMap(node.getConnectionsForResource(_, resource)))

    //Generate Flops

    //Distribute Flops

    //Distribute Resources
    var outputSet = TreeMultiset.create[IConnection[T]](ResourceConnectionComparer.FromResource(resource))
    val inputs = connections.withFilter(_.direction == ConnectionDirection.INPUT)
    connections.withFilter(_.direction == ConnectionDirection.OUTPUT).foreach(outputSet.add(_, 1))

    inputs.withFilter(_.active).foreach { icon =>
      outputSet.filter(a => icon.canInsert(a.buffer)).forall { ocon =>
        icon.setBuffer(ocon.insert(icon.buffer))
        icon.active
      }
    }
  }

  override def onTakeover(iNetwork: LogisticsNetwork): Unit = {}

  override def onSplit(iNetwork: LogisticsNetwork): Unit = {}

}
