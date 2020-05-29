package com.itszuvalex.femtocraft.api.power

import com.itszuvalex.femtocraft.api._
import com.itszuvalex.itszulib.api.core.IModule
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
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
    val nodes = new mutable.ArrayBuffer[IWiredPowerLeafNode]()

    try {
      NetworkNodeVisitor(nodeMap.iterator).withNeighborFilter((node, _, facing) => node.isConnectedWiredPower(facing)).withVisitor({
        case (tile: ITileEntity, facing: EnumFacing) if tile.hasModule(ManagerModules.TILE_WIRED_POWER_LEAF_NODE, facing.getOpposite) =>
          Option(tile.getModule(ManagerModules.TILE_WIRED_POWER_LEAF_NODE, facing.getOpposite)).map(nodes += _)
      }).visit()

      new DistributionAlgorithm(nodes.withFilter(_.powerType == PowerStorageNodeType.PRODUCER).map(n => DistributableBattery(n.battery, n.transferRate _)),
                                nodes.withFilter(_.powerType == PowerStorageNodeType.STORAGE).map(n => DistributableBattery(n.battery, n.transferRate _)),
                                nodes.withFilter(_.powerType == PowerStorageNodeType.CONSUMER).map(n => DistributableBattery(n.battery, n.transferRate _)))
        .distribute()
    } catch {
      case _: Throwable =>
    }
  }
}