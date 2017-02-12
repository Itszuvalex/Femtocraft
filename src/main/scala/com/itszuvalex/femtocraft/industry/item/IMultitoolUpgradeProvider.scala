package com.itszuvalex.femtocraft.industry.item

import net.minecraft.nbt.NBTTagCompound

/**
  * Created by Chris on 2/11/2017.
  */
trait IMultitoolUpgradeProvider {
  def getUpgradeName: String

  def getMultitoolUpgrade(comp: NBTTagCompound): IMultitoolUpgrade

}
