package com.itszuvalex.femtocraft.api.power

import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.logistics.TileNetworkNode

/**
  * Created by Chris on 1/1/2017.
  */
trait IPowerNetworkNode extends TileNetworkNode[IPowerNetworkNode, PowerNetwork] {
  override def canConnect(loc: Loc4): Boolean = {
    if (!(getLoc.distSqr(loc) <= connectionRadius * connectionRadius))
      return false

    getLoc.compareTo(loc) != 0
  }

  def connectionRadius: Float

  def leafNodes(force: Boolean): scala.collection.Set[IPowerLeafNode]

  def canAddLeafNode(node: IPowerLeafNode): Boolean = node.getStorageLoc.distSqr(getLoc) <= connectionRadius * connectionRadius

  def addLeafNode(node: IPowerLeafNode): Unit

  def removeLeafNode(node: IPowerLeafNode): Unit

  def storageNodes(force: Boolean): scala.collection.Set[IPowerStorageNode]

  def rendersConnections: Boolean

  def setRenderLocations(set: scala.collection.Set[Loc4]): Unit

  def renderLocations: scala.collection.Set[Loc4]

  def leafTransferRate: Double
}
