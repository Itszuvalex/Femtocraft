package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.api.power.{IWirelessPowerLeafNode, IWirelessPowerNetworkNode, PowerStorageNodeType}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.storage.IBattery

class DynamicIWirelessWirelessPowerLeafNode(getter: () => IWirelessPowerLeafNode) extends IWirelessPowerLeafNode {
  override def connectionRadius: Float = getter().connectionRadius

  override def getParent: Loc4 = getter().getParent

  override def setParent(node: IWirelessPowerNetworkNode): Unit = getter().setParent(node)

  override def onParentBroken(node: IWirelessPowerNetworkNode): Unit = getter().onParentBroken(node)

  override def battery: IBattery = getter().battery

  override def storageType: PowerStorageNodeType = getter().storageType

  override def transferRate: Double = getter().transferRate

  override def getStorageLoc: Loc4 = getter().getStorageLoc

  override def changeForLastTick: Double = getter().changeForLastTick
}
