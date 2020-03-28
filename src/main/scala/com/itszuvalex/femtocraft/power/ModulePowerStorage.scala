package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.itszulib.api.core.IModule
import com.itszuvalex.itszulib.api.storage.IBattery
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.TileEntityModule
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing

object ModulePowerStorage {
  val BAT_NBT = "Battery"
}

class ModulePowerStorage(val battery: IBattery) extends TileEntityModule[IBattery] {
  override def module: IModule[IBattery] = ManagerModules.POWER_STORAGE

  override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[IBattery] = _ => Some(battery)

  override def hasWorldNBT: Boolean = true

  override def writeWorldNBT(tag: NBTTagCompound): Unit = {
    tag.setTag(ModulePowerStorage.BAT_NBT, battery.serializeNBT())
  }

  override def readWorldNBT(tagCompound: NBTTagCompound): Unit = {
    battery.deserializeNBT(tagCompound.getCompoundTag(ModulePowerStorage.BAT_NBT))
  }
}
