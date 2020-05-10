package com.itszuvalex.femtocraft.api.power

import com.itszuvalex.femtocraft.api.{DistributableBattery, DistributionAlgorithm, IConduitTier, ManagerModules}
import com.itszuvalex.itszulib.api.core.IModule
import com.itszuvalex.itszulib.logistics.{ManagerNetwork, TileNetwork}
import net.minecraft.util.EnumFacing

import scala.collection.mutable

class WiredPowerNetwork(tier: IConduitTier) extends TileNetwork[IWiredPowerNode, WiredPowerNetwork](ManagerNetwork.instance.getNextID) {
  override def networkModule: IModule[IWiredPowerNode] = ManagerModules.TILE_WIRED_POWER_NODE

  override def create(): WiredPowerNetwork = new WiredPowerNetwork(tier)

  override def onTakeover(iNetwork: WiredPowerNetwork): Unit = {

  }

  override def onSplit(iNetwork: WiredPowerNetwork): Unit = {

  }

  override def canConnectNodes(a: IWiredPowerNode, b: IWiredPowerNode): Boolean = {
    Option(a.tier).exists(_.canConnect(b.tier)) && super.canConnectNodes(a, b)
  }

  override def onTickStart(): Unit = {

  }

  override def onTickEnd(): Unit = {
    try {
      val nodes = getLeafNodes
      new DistributionAlgorithm(nodes.withFilter(_.powerType == PowerStorageNodeType.PRODUCER).map(n => DistributableBattery(n.battery, n.transferRate _)).toSeq,
                                nodes.withFilter(_.powerType == PowerStorageNodeType.STORAGE).map(n => DistributableBattery(n.battery, n.transferRate _)).toSeq,
                                nodes.withFilter(_.powerType == PowerStorageNodeType.CONSUMER).map(n => DistributableBattery(n.battery, n.transferRate _)).toSeq)
        .distribute()
    } catch {
      case _: Throwable =>
    }
  }

  def getLeafNodes: mutable.Set[IWiredPowerLeafNode] = {
    val hashSet = new mutable.HashSet[IWiredPowerLeafNode]()
    nodeMap.foreach { node =>
      EnumFacing.VALUES.filter(node._2.isConnectedWiredPower).foreach { facing =>
        node._1.getOffset(facing).getITileEntity().flatMap(_.moduleOption(ManagerModules.TILE_WIRED_POWER_LEAF_NODE, facing.getOpposite)).foreach(hashSet += _)
      }
    }
    hashSet
  }
}