package com.itszuvalex.femtocraft.industry.item

import net.minecraft.nbt.NBTTagCompound

import scala.collection.mutable

/**
  * Created by Chris on 2/11/2017.
  */
object MultitoolUpgradeRegistry {
  val upgradeMap = new mutable.HashMap[String, IMultitoolUpgradeProvider]()

  def registerUpgrade(provider: IMultitoolUpgradeProvider): Unit = {
    upgradeMap(provider.getUpgradeName) = provider
  }

  def getMultitoolUpgrade(name: String, comp: NBTTagCompound): Option[IMultitoolUpgrade] = getProvider(name).map(_.getMultitoolUpgrade(comp))

  def getProvider(name: String): Option[IMultitoolUpgradeProvider] = upgradeMap.get(name)

  def init(): Unit = {

  }
}
