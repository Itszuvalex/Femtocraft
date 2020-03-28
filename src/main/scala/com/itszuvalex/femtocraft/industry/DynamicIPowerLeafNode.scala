package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.api.power.{IPowerLeafNode, IPowerNetworkNode, PowerStorageNodeType}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.storage.IBattery

class DynamicIPowerLeafNode(getter: () => IPowerLeafNode) extends IPowerLeafNode {
  override def connectionRadius: Float = getter().connectionRadius

  override def getParent: Loc4 = getter().getParent

  override def setParent(node: IPowerNetworkNode): Unit = getter().setParent(node)

  override def onParentBroken(node: IPowerNetworkNode): Unit = getter().onParentBroken(node)

  override def battery: IBattery = getter().battery

  override def storageType: PowerStorageNodeType = getter().storageType

  override def transferRate: Double = getter().transferRate

  override def getStorageLoc: Loc4 = getter().getStorageLoc

  override def changeForLastTick: Double = getter().changeForLastTick
}
