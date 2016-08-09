package com.itszuvalex.femtocraft.nanite

import com.itszuvalex.femtocraft.nanite.Attribute.AttributeRegistry
import com.itszuvalex.femtocraft.nanite.`trait`.INaniteTrait
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
  val EXPERIENCE_COMPOUND_TAG         = "Experience"
  val LEVEL_TAG                       = "Level"
  val EXPERIENCE_CURRENT_TAG          = "Experience"
  val TRAITS_COMPOUND_TAG             = "Traits"

  def getNanite(item: ItemStack) = getNaniteTag(item).map(_.getString(NANITE_TAG)).orNull

  def getNaniteTag(item: ItemStack): Option[NBTTagCompound] = {
    if (item == null) return None
    if (item.getTagCompound == null || item.getTagCompound.hasNoTags) return None
    Option(item.getTagCompound.getCompoundTag(NANITE_STRAIN_DATA_COMPOUND_TAG))
  }

  def setNanite(item: ItemStack, nanite: String) = getNaniteTag(item).foreach(_.setString(NANITE_TAG, nanite))

  def getLevel(item: ItemStack) = getExperienceTag(item).map(_.getInteger(LEVEL_TAG)).getOrElse(0)

  def setLevel(item: ItemStack, level: Int) = getExperienceTag(item).foreach(_.setInteger(LEVEL_TAG, level))

  def getExperience(item: ItemStack) = getExperienceTag(item).map(_.getInteger(EXPERIENCE_CURRENT_TAG)).getOrElse(0)

  def getExperienceTag(item: ItemStack) = getNaniteTag(item).map(_.getCompoundTag(EXPERIENCE_COMPOUND_TAG))

  def setExperience(item: ItemStack, exp: Int) = getExperienceTag(item).foreach(_.setInteger(EXPERIENCE_CURRENT_TAG, exp))

  def getTraits(item: ItemStack, traitType: String) = getTraitsTag(item).map(_.getCompoundTag(traitType)).map(_.getKeySet.asInstanceOf[java.util.Set[String]]).get

  def getTraitsTag(item: ItemStack) = getNaniteTag(item).map(_.getCompoundTag(TRAITS_COMPOUND_TAG))

  def getTraits(item: ItemStack) = getTraitsTag(item).map { traitsCompound =>
    traitsCompound.getKeySet.map(traitsCompound.getCompoundTag).flatMap(_.getKeySet())
  }.orNull

  def addTrait(item: ItemStack, naniteTrait: INaniteTrait): Unit = addTrait(item, naniteTrait.getName, naniteTrait.getClassification)

  def addTrait(item: ItemStack, naniteTrait: String, classification: String): Unit = {
    getTraitsTag(item).foreach { traits => {if (traits.hasKey(classification)) {traits.getCompoundTag(classification)} else {val compound = new NBTTagCompound; traits.setTag(TRAITS_COMPOUND_TAG, compound); compound}}.setTag(naniteTrait, new NBTTagCompound)
    }
  }

  def removeTrait(item: ItemStack, naniteTrait: INaniteTrait): Unit = removeTrait(item, naniteTrait.getName, naniteTrait.getClassification)

  def removeTrait(item: ItemStack, naniteTrait: String, classification: String): Unit = {
    getTraitsTag(item).map(_.getCompoundTag(classification)).foreach(_.removeTag(naniteTrait))
  }
}

trait NaniteStrain extends Item with INaniteStrain {
  override def getNanite(item: ItemStack): String = NaniteStrain.getNanite(item)

  override def getAttribute(item: ItemStack, attribute: String): Float = AttributeRegistry.getAttribute(attribute).map(_.getAttributeModified(item, this)).getOrElse(0)

  override def getLevel(item: ItemStack): Int = NaniteStrain.getLevel(item)

  override def getExperience(item: ItemStack): Int = NaniteStrain.getExperience(item)

  override def getTraits(item: ItemStack, traitType: String): Iterable[String] = NaniteStrain.getTraits(item, traitType)

  override def getTraits(item: ItemStack): Iterable[String] = NaniteStrain.getTraits(item)
}