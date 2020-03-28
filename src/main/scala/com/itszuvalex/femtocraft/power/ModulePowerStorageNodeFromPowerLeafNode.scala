package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.api.power.{IPowerLeafNode, IPowerStorageNode}
import com.itszuvalex.itszulib.api.core.IModule
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.TileEntityModule
import net.minecraft.util.EnumFacing

class ModulePowerStorageNodeFromPowerLeafNode(val leaf: IPowerLeafNode) extends TileEntityModule[IPowerStorageNode] {
  override def module: IModule[IPowerStorageNode] = ManagerModules.TILE_POWER_STORAGE_NODE

  override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[IPowerStorageNode] = _ => Some(leaf)
}
