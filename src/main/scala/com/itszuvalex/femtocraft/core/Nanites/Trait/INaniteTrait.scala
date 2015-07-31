package com.itszuvalex.femtocraft.core.Nanites.Trait

import com.itszuvalex.femtocraft.core.Nanites.INaniteStrain
import net.minecraft.item.ItemStack

/**
 * Created by Christopher on 7/31/2015.
 */
trait INaniteTrait {

  def getName: String

  def getClassification: String

  def getModifiedAttribute(itemStack: ItemStack, strain: INaniteStrain, attribute: String, default: Float) : Float

}
