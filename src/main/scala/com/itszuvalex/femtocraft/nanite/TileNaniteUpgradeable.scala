package com.itszuvalex.femtocraft.nanite

import com.itszuvalex.femtocraft.api.nanite.NaniteTank
import com.itszuvalex.itszulib.core.TileEntityBase
import net.minecraft.nbt.NBTTagCompound


object TileNaniteUpgradeable {
  val NANITE_TANK_KEY = "NaniteUpgradeTank'"
}

trait TileNaniteUpgradeable extends TileEntityBase with INaniteStorageTile {
  val upgradeTank: NaniteTank = defaultUpgradeTank

  def naniteUpgradeTank: NaniteTank = upgradeTank

  def defaultUpgradeTank: NaniteTank

  override def readFromNBT(par1nbtTagCompound: NBTTagCompound): Unit = {
    super.readFromNBT(par1nbtTagCompound)
    upgradeTank.deserializeNBT(par1nbtTagCompound.getCompoundTag(TileNaniteUpgradeable.NANITE_TANK_KEY))
  }

  override def writeToNBT(par1nbtTagCompound: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(par1nbtTagCompound)
    par1nbtTagCompound.setTag(TileNaniteUpgradeable.NANITE_TANK_KEY, upgradeTank.serializeNBT())
    par1nbtTagCompound
  }
}
