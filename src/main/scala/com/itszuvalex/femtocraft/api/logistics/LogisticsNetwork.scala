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
      val connections = nodeMap.values.flatMap { node =>
        EnumFacing.VALUES.flatMap(e => node.getConnectionsForResource[Any](e, resource.asInstanceOf[IResource[Any]]))
      }
      //Generate Flops

      //Distribute Flops
      //TODO Actual Impl
      connections.withFilter(_.active).foreach(_.contributeFlops(100))

      resourceDistributionLoop(resource.asInstanceOf[IResource[Any]], connections)
    }
  }

  private def resourceDistributionLoop[T](resource: IResource[T], connections: Iterable[IConnection[T]]) = {
    val comparator = ResourceConnectionComparer.FromResource(resource)
    val sortedOutputs = connections.filter(_.direction == ConnectionDirection.OUTPUT).toSeq.sortWith((a, b) => comparator.compare(a, b) < 0)
    val inputs = connections.withFilter(_.direction == ConnectionDirection.INPUT)

    // This will not take into account the change of an empty output slot to an itemstack output slot for cases of ordering of insertion
    // I.E. In the case of inserting into an empty item location (which will be last in the list, anyways), we don't reorder so that slot is further up
    // However, in this case, it's already assumed we skipped everything to get to the empty location
    // Don't think it matters too much.
    inputs.withFilter(a => !a.isEmpty).foreach { icon =>
      sortedOutputs.exists { ocon =>
        if (ocon.canInsert(icon.buffer)) {
          icon.setBuffer(ocon.insert(icon.buffer))
          icon.isEmpty
        } else false
      }
    }
  }

  override def onTakeover(iNetwork: LogisticsNetwork): Unit = {}

  override def onSplit(iNetwork: LogisticsNetwork): Unit = {}

}
