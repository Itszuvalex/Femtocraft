package com.itszuvalex.femtocraft.api.power

import com.itszuvalex.femtocraft.power.PowerManager
import com.itszuvalex.femtocraft.util.data.DataSpec
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.IBattery
import com.itszuvalex.itszulib.core.TileEntityBase


class PowerNetworkLeafNodeDelegate(
  tileEntity: TileEntityBase,
  radius: () => Float,
  getBattery: () => IBattery,
  stype: PowerStorageNodeType,
  transfer: () => Double
) extends IPowerLeafNode with DataSpec {
  var parentLoc: Loc4 = _

  override def connectionRadius: Float = radius()

  override def getParent: Loc4 = parentLoc

  override def setParent(node: IPowerNetworkNode): Unit = {
    parentLoc = node.getLoc
    tileEntity.setUpdate()
    tileEntity.setModified()
  }

  override def onParentBroken(node: IPowerNetworkNode): Unit = {
    parentLoc = null
    tileEntity.setUpdate()
    tileEntity.setModified()

    if (tileEntity.getWorld.isRemote) return
    PowerManager.refreshLeaf(this)
  }

  override def battery: IBattery = getBattery()

  override def storageType: PowerStorageNodeType = stype

  override def transferRate: Double = transfer()

  override def getStorageLoc: Loc4 = new Loc4(tileEntity)
}
