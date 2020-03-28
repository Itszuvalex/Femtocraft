package com.itszuvalex.femtocraft.nanite

import com.itszuvalex.femtocraft.api.nanite.NaniteTank
import com.itszuvalex.itszulib.core.TileEntityCore
import net.minecraft.nbt.NBTTagCompound

/**
  * Created by Chris on 12/11/2016.
  */
object TileNaniteStorage {
  val NANITE_TANK_KEY = "NaniteStorageTank"
}

trait TileNaniteStorage extends TileEntityCore with INaniteStorageTile {
  val storageTank: NaniteTank = defaultStorageTank

  def naniteStorageTank: NaniteTank = storageTank

  def defaultStorageTank: NaniteTank

  override def readFromNBT(par1nbtTagCompound: NBTTagCompound): Unit = {
    super.readFromNBT(par1nbtTagCompound)
    storageTank.deserializeNBT(par1nbtTagCompound.getCompoundTag(TileNaniteStorage.NANITE_TANK_KEY))
  }

  override def writeToNBT(par1nbtTagCompound: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(par1nbtTagCompound)
    par1nbtTagCompound.setTag(TileNaniteStorage.NANITE_TANK_KEY, storageTank.serializeNBT())
    par1nbtTagCompound
  }
}
