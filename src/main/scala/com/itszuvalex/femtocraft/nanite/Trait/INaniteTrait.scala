package com.itszuvalex.femtocraft.nanite.Trait

import com.itszuvalex.femtocraft.nanite.INaniteStrain
import net.minecraft.item.ItemStack

/**
  * Created by Christopher on 7/31/2015.
  */
trait INaniteTrait {

  def getName: String

  def getClassification: String

  def getModifiedAttribute(itemStack: ItemStack, strain: INaniteStrain, attribute: String, default: Float): Float

}
