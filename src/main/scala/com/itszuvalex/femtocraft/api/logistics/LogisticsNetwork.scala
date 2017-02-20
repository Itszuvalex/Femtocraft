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
      val connections = nodeMap.values.flatMap(node => EnumFacing.VALUES.flatMap(node.getConnectionsForResource(_, resource)))
      resourceDistributionLoop(resource, connections)
    }
  }

  private def resourceDistributionLoop[T](resource: IResource[T], connections: Iterable[IConnection[T]]) = {
    //Generate Flops

    //Distribute Flops

    //Distribute Resources
    var outputSet = TreeMultiset.create[IConnection[T]](ResourceConnectionComparer.FromResource(resource))
    val inputs = connections.withFilter(_.direction == ConnectionDirection.INPUT)
    connections.withFilter(_.direction == ConnectionDirection.OUTPUT).foreach(outputSet.add(_, 1))

    // This will not take into account the change of an empty output slot to an itemstack output slot for cases of ordering of insertion
    // I.E. In the case of inserting into an empty item location (which will be last in the list, anyways), we don't reorder so that slot is further up
    // However, in this case, it's already assumed we skipped everything to get to the empty location
    // Don't think it matters too much.
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
