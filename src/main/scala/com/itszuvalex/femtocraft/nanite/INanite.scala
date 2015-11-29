package com.itszuvalex.femtocraft.nanite

import com.itszuvalex.femtocraft.core.Nanites.Attribute.INaniteAttribute
import net.minecraft.item.ItemStack

/**
 * Created by Christopher Harris (Itszuvalex) on 7/3/15.
 */
object INanite {
  val ARCH_BUILDER     = "Builder"
  val ARCH_RECYCLER    = "Recycler"
  val ARCH_MAINTAINER  = "Maintainer"
  val ARCH_PROVIDER    = "Provider"
  val ARCH_ENERGIZER   = "Energizer"
  val COLOR_BUILDER    = 0
  val COLOR_RECYCLER   = 0
  val COLOR_MAINTAINER = 0
  val COLOR_PROVIDER   = 0
  val COLOR_ENERGIZER  = 0
}

trait INanite {

  def getArchetype: String

  def getColor: Int

  def getAttributeBase(itemStack: ItemStack, strain: INaniteStrain, attribute: INaniteAttribute): Float

  def getAttributeLevelBonus(itemStack: ItemStack, strain: INaniteStrain, attribute: INaniteAttribute): Float

  def getAttributeModified(itemStack: ItemStack, strain: INaniteStrain, attribute: INaniteAttribute): Float

}
