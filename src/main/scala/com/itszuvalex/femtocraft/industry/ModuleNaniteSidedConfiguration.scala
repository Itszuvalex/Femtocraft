package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.nanite.SidedNaniteStorageConfiguration
import com.itszuvalex.itszulib.api.core.IModule
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.TileEntityModule
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing

object ModuleNaniteSidedConfiguration {
  val CONFIG_NBT = "Config"
}

class ModuleNaniteSidedConfiguration(val configuration: SidedNaniteStorageConfiguration) extends TileEntityModule[SidedNaniteStorageConfiguration] {
  override def module: IModule[SidedNaniteStorageConfiguration] = ManagerModules.NANITE_STORAGE_CONFIGURABLE

  override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[SidedNaniteStorageConfiguration] = _ => Some(configuration)

  override def hasDescriptionNBT: Boolean = true

  override def hasWorldNBT: Boolean = true

  override def writeDescriptionNBT(tag: NBTTagCompound): Unit = writeToNBT(tag)

  override def readDescriptionNBT(tag: NBTTagCompound): Unit = readFromNBT(tag)

  override def writeWorldNBT(tag: NBTTagCompound): Unit = writeToNBT(tag)

  override def readWorldNBT(tag: NBTTagCompound): Unit = readFromNBT(tag)

  def writeToNBT(tag: NBTTagCompound): Unit = {
    tag.setTag(ModuleNaniteSidedConfiguration.CONFIG_NBT, configuration.serializeNBT())
  }

  def readFromNBT(tag: NBTTagCompound): Unit = {
    configuration.deserializeNBT(tag.getCompoundTag(ModuleNaniteSidedConfiguration.CONFIG_NBT))
  }
}
