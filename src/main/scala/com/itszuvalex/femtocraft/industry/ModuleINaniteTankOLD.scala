package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.api.nanite.INaniteTankOLD
import com.itszuvalex.itszulib.api.core.IModule
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.TileEntityModule
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing

object ModuleINaniteTankOLD {
  val TANK_NBT = "Tank"
}

class ModuleINaniteTankOLD(val tank: INaniteTankOLD) extends TileEntityModule[INaniteTankOLD] {
  override def module: IModule[INaniteTankOLD] = ManagerModules.TILE_NANITE_STORAGE_TANK_OLD

  override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[INaniteTankOLD] = {
    case null => Some(tank)
    case f =>
      tile.moduleOption(ManagerModules.NANITE_STORAGE_CONFIGURABLE_OLD, null).map(io => Option(io.getStorageForGlobalFacing(f))).getOrElse(Option(tank))
  }

  override def hasWorldNBT: Boolean = true

  override def writeWorldNBT(tag: NBTTagCompound): Unit = {
    tag.setTag(ModuleINaniteTankOLD.TANK_NBT, tank.serializeNBT())
  }

  override def readWorldNBT(tagCompound: NBTTagCompound): Unit = {
    tank.deserializeNBT(tagCompound.getCompoundTag(ModuleINaniteTankOLD.TANK_NBT))
  }
}
