package com.itszuvalex.femtocraft.nanite.Attribute

import com.itszuvalex.femtocraft.nanite.Attribute.NaniteAttribute._
import com.itszuvalex.femtocraft.nanite.{INaniteStrain, NaniteStrain}
import com.itszuvalex.itszulib.api.core.Configurable
import net.minecraft.item.ItemStack

/**
  * Created by Christopher on 7/31/2015.
  */
@Configurable
object NaniteAttribute {
  val MAX_BONUS_LEVELS       = 5
  val BONUS_LEVEL_MODIFIER   = .05f
  val ATTRIBUTE_COMPOUND_TAG = "Attributes"
  val ATTRIBUTE_BONUS_TAG    = "Bonus"

  def getAttributesTag(item: ItemStack) = NaniteStrain.getNaniteTag(item).map(_.getCompoundTag(ATTRIBUTE_COMPOUND_TAG))

  def getAttributeTag(item: ItemStack, attribute: String) = getAttributesTag(item).map(_.getCompoundTag(attribute))

  def getAttributeBonusLevel(item: ItemStack, attribute: String) = getAttributeTag(item, attribute).map(_.getInteger(ATTRIBUTE_BONUS_TAG))

  def setAttributeBonusLevel(item: ItemStack, attribute: String, level: Int) = getAttributeTag(item, attribute).foreach(_.setInteger(ATTRIBUTE_BONUS_TAG, level))
}

@Configurable
class NaniteAttribute(val name: String) extends INaniteAttribute {
  override def getName = name

  override def getAttributeModified(item: ItemStack, strain: INaniteStrain): Float = getAttributeBase(item, strain) * (1f + BONUS_LEVEL_MODIFIER * getAttributeBonusLevel(item, getName).getOrElse(0))

  override def getMaxBonus = MAX_BONUS_LEVELS

  override def getAttributeBase(item: ItemStack, strain: INaniteStrain) = 1f

  override def getAttributeBonus(item: ItemStack, strain: INaniteStrain) = BONUS_LEVEL_MODIFIER
}
