package com.itszuvalex.femtocraft.api.computation

import com.itszuvalex.femtocraft.api.{IConduitTier, ManagerModules, NetworkNodeVisitor}
import com.itszuvalex.itszulib.api.core.IModule
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.logistics.{ManagerNetwork, TileNetwork}
import net.minecraft.util.EnumFacing

import scala.collection.mutable.ArrayBuffer

class WiredComputationNetwork(tier: IConduitTier) extends TileNetwork[IWiredComputationNode, WiredComputationNetwork](ManagerNetwork.instance.getNextID) {
  override def networkModule: IModule[IWiredComputationNode] = ManagerModules.TILE_WIRED_COMPUTATION_NODE

  override def create(): WiredComputationNetwork = new WiredComputationNetwork(tier)

  override def onTakeover(iNetwork: WiredComputationNetwork): Unit = {

  }

  override def onSplit(iNetwork: WiredComputationNetwork): Unit = {

  }

  override def canConnectNodes(a: IWiredComputationNode, b: IWiredComputationNode): Boolean = {
    Option(a.tier).exists(_.canConnect(b.tier)) && super.canConnectNodes(a, b)
  }

  override def onTickStart(): Unit = {

  }

  override def onTickEnd(): Unit = {
    try {
      val nodes = new ArrayBuffer[IWiredComputationLeafNode]()

      NetworkNodeVisitor(nodeMap.iterator).withNeighborFilter((node, _, facing) => node.isConnectedWiredComputation(facing)).withVisitor({
        case (tile: ITileEntity, facing: EnumFacing) if tile.hasModule(ManagerModules.TILE_WIRED_COMPUTATION_LEAF_NODE, facing.getOpposite) =>
          Option(tile.getModule(ManagerModules.TILE_WIRED_COMPUTATION_LEAF_NODE, facing.getOpposite)).map(nodes += _)
      }).visit()

      val computers = nodes.view.flatMap(_.computers)
      val jobs      = nodes.view.flatMap(_.jobs)
      new ComputationDistributionAlgorithm(computers, jobs)
        .distribute()
    } catch {
      case _: Throwable =>
    }
  }
}