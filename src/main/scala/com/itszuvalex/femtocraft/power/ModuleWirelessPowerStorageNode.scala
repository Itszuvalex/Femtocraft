package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.api.power.{IWirelessPowerStorageNode, PowerStorageNodeType}
import com.itszuvalex.itszulib.api.core.{IModule, Loc4}
import com.itszuvalex.itszulib.api.storage.IBattery
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.TileEntityModule
import net.minecraft.util.EnumFacing

class ModuleWirelessPowerStorageNode(val tile: ITileEntity, val bat: IBattery, val storeType: PowerStorageNodeType, val tranRate: () => Double) extends TileEntityModule[IWirelessPowerStorageNode] with IWirelessPowerStorageNode {
  override def module: IModule[IWirelessPowerStorageNode] = ManagerModules.TILE_WIRELESS_POWER_STORAGE_NODE

  override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[IWirelessPowerStorageNode] = _ => Some(this)

  override def battery: IBattery = bat

  override def storageType: PowerStorageNodeType = storeType

  override def transferRate: Double = tranRate()

  override def getStorageLoc: Loc4 = Loc4(tile)

  override def changeForLastTick: Double = 0 // TODO

}
