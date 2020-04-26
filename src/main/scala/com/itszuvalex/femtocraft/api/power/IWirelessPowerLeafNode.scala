package com.itszuvalex.femtocraft.api.power

import com.itszuvalex.itszulib.api.core.Loc4

/**
  * Created by Chris on 1/4/2017.
  */
trait IWirelessPowerLeafNode extends IWirelessPowerStorageNode {

  def connectionRadius: Float

  def getParent: Loc4

  def setParent(node: IWirelessPowerNetworkNode): Unit

  def canSetParent(node: IWirelessPowerNetworkNode): Boolean = node.getLoc.distSqr(getStorageLoc) <= connectionRadius * connectionRadius

  def onParentBroken(node: IWirelessPowerNetworkNode): Unit

}
