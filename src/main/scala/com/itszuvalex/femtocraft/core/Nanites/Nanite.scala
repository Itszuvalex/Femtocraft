package com.itszuvalex.femtocraft.core.Nanites

import net.minecraft.item.ItemStack

/**
 * Created by Christopher Harris (Itszuvalex) on 7/3/15.
 */

class Nanite(private val arch: String, private val col: Int) extends INanite {
  override def archetype = arch

  override def color = col

  override def attributeBase(itemStack: ItemStack, strain: INaniteStrain, attribute: String): Float = 1f

  override def attributeLevelBonus(itemStack: ItemStack, strain: INaniteStrain, attribute: String): Float = .05f
}
