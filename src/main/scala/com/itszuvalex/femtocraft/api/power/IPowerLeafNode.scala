package com.itszuvalex.femtocraft.api.power

import com.itszuvalex.itszulib.api.core.Loc4

/**
  * Created by Chris on 1/4/2017.
  */
trait IPowerLeafNode extends IPowerStorageNode {

  def connectionRadius: Float

  def getParent: Loc4

  def setParent(node: IPowerNetworkNode): Unit

  def canSetParent(node: IPowerNetworkNode): Boolean = node.getLoc.distSqr(getStorageLoc) <= connectionRadius * connectionRadius

  def onParentBroken(node: IPowerNetworkNode): Unit

}
