package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.nanite.SidedNaniteStorageConfigurationOLD
import com.itszuvalex.itszulib.api.core.IModule
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.TileEntityModule
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing

object ModuleNaniteSidedConfigurationOLD {
  val CONFIG_NBT = "Config"
}

class ModuleNaniteSidedConfigurationOLD(val configuration: SidedNaniteStorageConfigurationOLD) extends TileEntityModule[SidedNaniteStorageConfigurationOLD] {
  override def module: IModule[SidedNaniteStorageConfigurationOLD] = ManagerModules.NANITE_STORAGE_CONFIGURABLE_OLD

  override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[SidedNaniteStorageConfigurationOLD] = _ => Some(configuration)

  override def hasDescriptionNBT: Boolean = true

  override def hasWorldNBT: Boolean = true

  override def writeDescriptionNBT(tag: NBTTagCompound): Unit = writeToNBT(tag)

  override def readDescriptionNBT(tag: NBTTagCompound): Unit = readFromNBT(tag)

  override def writeWorldNBT(tag: NBTTagCompound): Unit = writeToNBT(tag)

  override def readWorldNBT(tag: NBTTagCompound): Unit = readFromNBT(tag)

  def writeToNBT(tag: NBTTagCompound): Unit = {
    tag.setTag(ModuleNaniteSidedConfigurationOLD.CONFIG_NBT, configuration.serializeNBT())
  }

  def readFromNBT(tag: NBTTagCompound): Unit = {
    configuration.deserializeNBT(tag.getCompoundTag(ModuleNaniteSidedConfigurationOLD.CONFIG_NBT))
  }
}
