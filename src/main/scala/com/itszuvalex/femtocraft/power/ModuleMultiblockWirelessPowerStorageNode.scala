package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.api.power.{IWirelessPowerStorageNode, PowerStorageNodeType}
import com.itszuvalex.itszulib.api.core.{IModule, Loc4}
import com.itszuvalex.itszulib.api.multiblock.MultiBlockInfo
import com.itszuvalex.itszulib.api.storage.IBattery
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.TileEntityModule
import net.minecraft.util.EnumFacing

class ModuleMultiblockWirelessPowerStorageNode(val info: MultiBlockInfo, val getter: () => IWirelessPowerStorageNode) extends TileEntityModule[IWirelessPowerStorageNode] with IWirelessPowerStorageNode {
  override def module: IModule[IWirelessPowerStorageNode] = ManagerModules.TILE_WIRELESS_POWER_STORAGE_NODE

  override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[IWirelessPowerStorageNode] = _ => Some(this)

  override def battery: IBattery = getter().battery

  override def storageType: PowerStorageNodeType = getter().storageType

  override def transferRate: Double = getter().transferRate

  override def getStorageLoc: Loc4 = getter().getStorageLoc

  override def changeForLastTick: Double = getter().changeForLastTick // TODO


}
