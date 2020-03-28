package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.api.power.{IPowerLeafNode, IPowerNetworkNode, PowerStorageNodeType}
import com.itszuvalex.itszulib.api.core.{IModule, Loc4}
import com.itszuvalex.itszulib.api.multiblock.MultiBlockInfo
import com.itszuvalex.itszulib.api.storage.IBattery
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.TileEntityModule
import net.minecraft.util.EnumFacing

class ModuleMultiblockPowerLeafNode(val info: MultiBlockInfo, val getter: () => IPowerLeafNode) extends TileEntityModule[IPowerLeafNode] with IPowerLeafNode {
  override def module: IModule[IPowerLeafNode] = ManagerModules.TILE_POWER_LEAF_NODE

  override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[IPowerLeafNode] = _ => Some(this)

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
