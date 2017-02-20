package com.itszuvalex.femtocraft.api.logistics

import java.util

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
      val connections = nodeMap.values.view.flatMap(node => EnumFacing.VALUES.view.flatMap(node.getConnectionsForResource(_, resource)))
      val inputs = connections.withFilter(_.direction == ConnectionDirection.INPUT)
      val outputs = connections.withFilter(_.direction == ConnectionDirection.OUTPUT)

      inputs.withFilter(_.active).foreach { icon =>
        outputs.withFilter(icon.canInsert).forall { ocon =>
          icon.setBuffer(ocon.insert(icon.buffer))
          icon.active
        }
      }
    }
  }

  override def onTakeover(iNetwork: LogisticsNetwork): Unit = {}

  override def onSplit(iNetwork: LogisticsNetwork): Unit = {}

}
