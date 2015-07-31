package com.itszuvalex.femtocraft.core.Nanites

import com.itszuvalex.femtocraft.core.Nanites.Attribute.INaniteAttribute
import net.minecraft.item.ItemStack

/**
 * Created by Christopher Harris (Itszuvalex) on 7/3/15.
 */
object Nanite {
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

class Nanite(private val arch: String, private val col: Int) extends INanite {
  override def getArchetype = arch

  override def getColor = col

  override def getAttributeBase(itemStack: ItemStack, strain: INaniteStrain, attribute: INaniteAttribute): Float = attribute.getAttributeBase(itemStack, strain)

  override def getAttributeLevelBonus(itemStack: ItemStack, strain: INaniteStrain, attribute: INaniteAttribute): Float = attribute.getAttributeBonus(itemStack, strain)

  override def getAttributeModified(itemStack: ItemStack, strain: INaniteStrain, attribute: INaniteAttribute): Float = attribute.getAttributeModified(itemStack, strain)
}
