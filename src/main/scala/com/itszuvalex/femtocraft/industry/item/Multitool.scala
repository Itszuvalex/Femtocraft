package com.itszuvalex.femtocraft.industry.item

import java.util

import net.minecraft.nbt.NBTTagCompound

/**
  * Created by Chris on 2/11/2017.
  */
class Multitool(compound: NBTTagCompound) extends IMultitool {
  override def allInstalledUpgrades: util.Collection[IMultitoolUpgrade] = ???

  override def installedUpgrades(slot: EnumMultitoolUpgradeSlot): util.Collection[IMultitoolUpgrade] = ???

  override def canInstallUpgrade(upgrade: IMultitoolUpgrade): Boolean = ???

  override def installUpgrade(upgrade: IMultitoolUpgrade): Unit = ???

  override def removeUpgrade(upgrade: IMultitoolUpgrade): Unit = ???

  override def activePrimary: Option[IMultitoolPrimaryUpgrade] = ???

  override def activeSecondary: Option[IMultitoolSecondaryUpgrade] = ???

  override def setActivePrimary(upgrade: IMultitoolUpgrade): Unit = ???

  override def setActiveSecondary(upgrade: IMultitoolUpgrade): Unit = ???
}
