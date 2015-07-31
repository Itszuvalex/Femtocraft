package com.itszuvalex.femtocraft.core.Nanites

import com.itszuvalex.femtocraft.core.Nanites.Attribute.INaniteAttribute
import net.minecraft.item.ItemStack

/**
 * Created by Christopher Harris (Itszuvalex) on 7/3/15.
 */
trait INanite {

  def getArchetype: String

  def getColor: Int

  def getAttributeBase(itemStack: ItemStack, strain: INaniteStrain, attribute: INaniteAttribute): Float

  def getAttributeLevelBonus(itemStack: ItemStack, strain: INaniteStrain, attribute: INaniteAttribute): Float

  def getAttributeModified(itemStack: ItemStack, strain: INaniteStrain, attribute: INaniteAttribute) : Float

}
