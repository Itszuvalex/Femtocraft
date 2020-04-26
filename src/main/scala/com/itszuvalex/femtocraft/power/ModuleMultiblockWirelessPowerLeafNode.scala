package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.api.power.{IWirelessPowerLeafNode, IWirelessPowerNetworkNode, PowerStorageNodeType}
import com.itszuvalex.itszulib.api.core.{IModule, Loc4}
import com.itszuvalex.itszulib.api.multiblock.MultiBlockInfo
import com.itszuvalex.itszulib.api.storage.IBattery
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.TileEntityModule
import net.minecraft.util.EnumFacing

class ModuleMultiblockWirelessPowerLeafNode(val info: MultiBlockInfo, val getter: () => IWirelessPowerLeafNode) extends TileEntityModule[IWirelessPowerLeafNode] with IWirelessPowerLeafNode {
  override def module: IModule[IWirelessPowerLeafNode] = ManagerModules.TILE_WIRELESS_POWER_LEAF_NODE

  override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[IWirelessPowerLeafNode] = _ => Some(this)

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
