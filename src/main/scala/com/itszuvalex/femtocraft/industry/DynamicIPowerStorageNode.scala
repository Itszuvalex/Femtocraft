package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.api.power.{IPowerStorageNode, PowerStorageNodeType}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.IBattery

class DynamicIPowerStorageNode(getter: () => IPowerStorageNode) extends IPowerStorageNode {
  override def battery: IBattery = getter().battery

  override def storageType: PowerStorageNodeType = getter().storageType

  override def transferRate: Double = getter().transferRate

  override def getStorageLoc: Loc4 = getter().getStorageLoc

  override def changeForLastTick: Double = getter().changeForLastTick
}
