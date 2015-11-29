package com.itszuvalex.femtocraft.nanite.Attribute

import com.itszuvalex.femtocraft.nanite.INaniteStrain
import net.minecraft.item.ItemStack

/**
 * Created by Christopher on 7/31/2015.
 */
trait INaniteAttribute {

  def getName: String

  def getAttributeBase(item: ItemStack, strain: INaniteStrain): Float

  def getAttributeBonus(item: ItemStack, strain: INaniteStrain) : Float

  def getAttributeModified(item: ItemStack, strain: INaniteStrain): Float

  def getMaxBonus: Int

}
