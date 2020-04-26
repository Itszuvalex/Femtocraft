package com.itszuvalex.femtocraft.api.power

import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.logistics.TileNetworkNode

/**
  * Created by Chris on 1/1/2017.
  */
trait IWirelessPowerNetworkNode extends TileNetworkNode[IWirelessPowerNetworkNode, WirelessPowerNetwork] {
  override def canConnect(loc: Loc4): Boolean = {
    if (!(getLoc.distSqr(loc) <= connectionRadius * connectionRadius))
      return false

    getLoc.compareTo(loc) != 0
  }

  def connectionRadius: Float

  def leafNodes(force: Boolean): scala.collection.Set[IWirelessPowerLeafNode]

  def canAddLeafNode(node: IWirelessPowerLeafNode): Boolean = node.getStorageLoc.distSqr(getLoc) <= connectionRadius * connectionRadius

  def addLeafNode(node: IWirelessPowerLeafNode): Unit

  def removeLeafNode(node: IWirelessPowerLeafNode): Unit

  def storageNodes(force: Boolean): scala.collection.Set[IWirelessPowerStorageNode]

  def rendersConnections: Boolean

  def setRenderLocations(set: scala.collection.Set[Loc4]): Unit

  def renderLocations: scala.collection.Set[Loc4]

  def leafTransferRate: Double
}
