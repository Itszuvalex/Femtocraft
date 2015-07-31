package com.itszuvalex.femtocraft.core.Nanites

import net.minecraft.item.ItemStack

/**
 * Created by Christopher Harris (Itszuvalex) on 7/3/15.
 */
trait INanite {

  def archetype: String

  def color: Int

  def attributeBase(itemStack: ItemStack, strain: INaniteStrain, attribute: String): Float

  def attributeLevelBonus(itemStack: ItemStack, strain: INaniteStrain, attribute: String) : Float

}
