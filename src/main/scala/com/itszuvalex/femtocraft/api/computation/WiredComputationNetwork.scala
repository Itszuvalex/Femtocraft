package com.itszuvalex.femtocraft.api.computation

import com.itszuvalex.femtocraft.api.{IConduitTier, ManagerModules}
import com.itszuvalex.itszulib.api.core.IModule
import com.itszuvalex.itszulib.logistics.{ManagerNetwork, TileNetwork}
import net.minecraft.util.EnumFacing

import scala.collection.mutable

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
      val nodes = getLeafNodes
      val computers = new mutable.HashSet[IComputer]() ++ nodes.view.flatMap(_.computers)
      val jobs = new mutable.HashSet[IComputationJob]() ++ nodes.view.flatMap(_.jobs)
      new ComputationDistributionAlgorithm(computers.toSeq, jobs.toSeq)
        .distribute()
    } catch {
      case _: Throwable =>
    }
  }

  def getLeafNodes: mutable.Set[IWiredComputationLeafNode] = {
    val hashSet = new mutable.HashSet[IWiredComputationLeafNode]()
    nodeMap.foreach { node =>
      EnumFacing.VALUES.filter(node._2.isConnectedWiredComputation).foreach { facing =>
        node._1.getOffset(facing).getITileEntity().flatMap(_.moduleOption(ManagerModules.TILE_WIRED_COMPUTATION_LEAF_NODE, facing.getOpposite)).foreach(hashSet += _)
      }
    }
    hashSet
  }
}