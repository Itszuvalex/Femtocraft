package com.itszuvalex.femtocraft.industry.item

import java.util

/**
  * Created by Chris on 2/11/2017.
  */
trait IMultitool {

  def allInstalledUpgrades: util.Collection[IMultitoolUpgrade]

  def installedUpgrades(slot: EnumMultitoolUpgradeSlot): util.Collection[IMultitoolUpgrade]

  def canInstallUpgrade(upgrade: IMultitoolUpgrade): Boolean

  def installUpgrade(upgrade: IMultitoolUpgrade): Unit

  def removeUpgrade(upgrade: IMultitoolUpgrade): Unit

  def activePrimary: Option[IMultitoolPrimaryUpgrade]

  def activeSecondary: Option[IMultitoolSecondaryUpgrade]

  def setActivePrimary(upgrade: IMultitoolUpgrade): Unit

  def setActiveSecondary(upgrade: IMultitoolUpgrade): Unit

}
