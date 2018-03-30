package com.itszuvalex.femtocraft.api.power

import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.IBattery
import net.minecraft.tileentity.TileEntity

class PowerStorageNodeDelegate(tileEntity: TileEntity,
  getBattery: () => IBattery,
  storageT: PowerStorageNodeType,
  transfer: () => Double)
  extends IPowerStorageNode {

  override def battery: IBattery = getBattery()

  override def storageType: PowerStorageNodeType = storageT

  override def transferRate: Double = transfer()

  override def getStorageLoc: Loc4 = new Loc4(tileEntity)
}

