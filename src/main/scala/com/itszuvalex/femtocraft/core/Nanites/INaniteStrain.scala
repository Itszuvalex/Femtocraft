package com.itszuvalex.femtocraft.core.Nanites

import net.minecraft.item.ItemStack

/**
 * Created by Christopher on 7/29/2015.
 */
trait INaniteStrain {

  def getNanite(item: ItemStack): String

  def getLevel(item: ItemStack): Int

  def getExperience(item: ItemStack): Int

  def getAttribute(item: ItemStack, attribute: String): Float

  def getTraits(item: ItemStack, traitType: String): Iterable[String]

  def getTraits(item: ItemStack): Iterable[String]

}
