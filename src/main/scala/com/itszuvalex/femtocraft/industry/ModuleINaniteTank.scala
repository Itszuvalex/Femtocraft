package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.api.nanite.INaniteTank
import com.itszuvalex.itszulib.api.core.IModule
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.TileEntityModule
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing

object ModuleINaniteTank {
  val TANK_NBT = "Tank"
}

class ModuleINaniteTank(val tank: INaniteTank) extends TileEntityModule[INaniteTank] {
  override def module: IModule[INaniteTank] = ManagerModules.TILE_NANITE_STORAGE_TANK

  override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[INaniteTank] = {
    case null => Some(tank)
    case f =>
      tile.moduleOption(ManagerModules.NANITE_STORAGE_CONFIGURABLE, null).map(io => Option(io.getStorageForGlobalFacing(f))).getOrElse(Option(tank))
  }

  override def hasWorldNBT: Boolean = true

  override def writeWorldNBT(tag: NBTTagCompound): Unit = {
    tag.setTag(ModuleINaniteTank.TANK_NBT, tank.serializeNBT())
  }

  override def readWorldNBT(tagCompound: NBTTagCompound): Unit = {
    tank.deserializeNBT(tagCompound.getCompoundTag(ModuleINaniteTank.TANK_NBT))
  }
}
