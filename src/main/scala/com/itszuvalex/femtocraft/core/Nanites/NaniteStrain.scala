package com.itszuvalex.femtocraft.core.Nanites

import com.itszuvalex.femtocraft.core.Nanites.NaniteStrain._
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound

import scala.collection.JavaConversions._

/**
 * Created by Christopher on 7/29/2015.
 */
object NaniteStrain {
  val MAX_LEVEL                       = 10
  val XP_PER_LEVEL_MULT               = 100
  val LEVEL_EXP_MILESTONES            = {
    var total = 0
    (2 to MAX_LEVEL).map { level =>
      val amount = level * XP_PER_LEVEL_MULT
      total += amount
      total
                         }.toArray
  }
  val MAX_ATTRIBUTE_BONUS             = 5
  val NANITE_STRAIN_DATA_COMPOUND_TAG = "NaniteStrain"
  val NANITE_TAG                      = "Nanite"
  val ATTRIBUTE_COMPOUND_TAG          = "Attributes"
  val ATTRIBUTE_BONUS_TAG             = "Bonus"
  val EXPERIENCE_COMPOUND_TAG         = "Experience"
  val LEVEL_TAG                       = "Level"
  val EXPERIENCE_CURRENT_TAG          = "Experience"
  val TRAITS_COMPOUND_TAG             = "Traits"
}

trait NaniteStrain extends Item with INaniteStrain {
  override def getNanite(item: ItemStack): String = getNaniteTag(item).map(_.getString(NANITE_TAG)).orNull

  override def getAttribute(item: ItemStack, attribute: String): Float = {
    val attributeCompound = getNaniteTag(item).map(_.getCompoundTag(ATTRIBUTE_COMPOUND_TAG)).map(_.getCompoundTag(attribute)).orNull
    if (attributeCompound == null) return 0
    val bonus = attributeCompound.getInteger(ATTRIBUTE_BONUS_TAG)
    val naniteKey = getNanite(item)
    val nanite = NaniteRegistry.getNanite(naniteKey).orNull
    if (nanite == null) return 0
    val attBase = nanite.attributeBase(item, this, attribute)
    val bonusBase = nanite.attributeLevelBonus(item, this, attribute)
    val bonusAmt = bonus * bonusBase
    attBase * (1f + bonusAmt)
  }

  override def getLevel(item: ItemStack): Int = getNaniteTag(item).map(_.getCompoundTag(EXPERIENCE_COMPOUND_TAG)).map(_.getInteger(LEVEL_TAG)).getOrElse(0)

  override def getExperience(item: ItemStack): Int = getNaniteTag(item).map(_.getCompoundTag(EXPERIENCE_COMPOUND_TAG)).map(_.getInteger(EXPERIENCE_CURRENT_TAG)).getOrElse(0)

  def getNaniteTag(item: ItemStack): Option[NBTTagCompound] = {
    if (item == null) return None
    if (item.stackTagCompound == null || item.stackTagCompound.hasNoTags) return None
    Option(item.stackTagCompound.getCompoundTag(NANITE_STRAIN_DATA_COMPOUND_TAG))
  }

  override def getTraits(item: ItemStack, traitType: String): Iterable[String] = getNaniteTag(item).map(_.getCompoundTag(TRAITS_COMPOUND_TAG)).map(_.getCompoundTag(traitType)).map(_.func_150296_c().asInstanceOf[java.util.Set[String]]).get

  override def getTraits(item: ItemStack): Iterable[String] = getNaniteTag(item).map(_.getCompoundTag(TRAITS_COMPOUND_TAG)).map { traitsCompound =>
    traitsCompound.func_150296_c().asInstanceOf[java.util.Set[String]].map(traitsCompound.getCompoundTag).flatMap(_.func_150296_c().asInstanceOf[java.util.Set[String]])
                                                                                                                                }.orNull
}