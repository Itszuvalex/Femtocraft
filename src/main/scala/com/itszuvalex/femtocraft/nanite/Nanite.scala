package com.itszuvalex.femtocraft.nanite

import com.itszuvalex.femtocraft.nanite.Attribute.INaniteAttribute
import net.minecraft.item.ItemStack

/**
  * Created by Christopher Harris (Itszuvalex) on 7/3/15.
  */
class Nanite(private val arch: String, private val col: Int) extends INanite {
  override def getArchetype = arch

  override def getColor = col

  override def getAttributeBase(itemStack: ItemStack, strain: INaniteStrain, attribute: INaniteAttribute): Float = attribute.getAttributeBase(itemStack, strain)

  override def getAttributeLevelBonus(itemStack: ItemStack, strain: INaniteStrain, attribute: INaniteAttribute): Float = attribute.getAttributeBonus(itemStack, strain)

  override def getAttributeModified(itemStack: ItemStack, strain: INaniteStrain, attribute: INaniteAttribute): Float = attribute.getAttributeModified(itemStack, strain)
}
