package com.itszuvalex.femtocraft.util

import java.util.regex.Pattern

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.oredict.OreDictionary

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

/**
  * Created by Christopher Harris (Itszuvalex) on 2/1/2016.
  */
object OreNameFilterRule {
  lazy val registered = registerRule()
  registered

  val filterType = "OreNameFilter"

  val ORE_NAME_KEY = "OreName"

  private def registerRule(): Boolean = {
    ItemFilterRule.registerFilterType(filterType, classOf[OreNameFilterRule])
    true
  }
}

class OreNameFilterRule extends IItemFilterRule {
  OreNameFilterRule.registered

  private var oreName         = ""
  private var regexp: Pattern = null

  def getOreName = oreName

  def setOreName(name: String) = {
    oreName = name
    updateRegexp()
  }

  private def updateRegexp() = {
    try {
      regexp = Pattern.compile(oreName)
    }
    catch {
      case ignored: Throwable =>
        ignored.printStackTrace()
        regexp = null
    }
  }

  override def itemMatches(item: ItemStack) = {
    if (!hasValidRegexp) false
    else OreDictionary.getOreIDs(item).map(OreDictionary.getOreName).exists(regexp.matcher(_).matches())
  }

  def getAllItemStacksMatching = {
    if (!hasValidRegexp) ArrayBuffer[ItemStack]()
    else getAllOreNamesMatching.flatMap(OreDictionary.getOres(_).asScala).toBuffer
  }

  def getALlItemStackMatchingOrganizedByOreName = {
    if (!hasValidRegexp) Map[String, scala.collection.mutable.Buffer[ItemStack]]()
    else getAllOreNamesMatching.map(name => name -> OreDictionary.getOres(name).asScala)(collection.breakOut): Map[String, scala.collection.mutable.Buffer[ItemStack]]
  }

  def getAllOreNamesMatching = {
    if (!hasValidRegexp) ArrayBuffer[String]().toArray
    else OreDictionary.getOreNames.filter(regexp.matcher(_).matches())
  }

  def hasValidRegexp = regexp != null

  override def ruleType = OreNameFilterRule.filterType

  override def loadFromNBT(compound: NBTTagCompound): Unit = {
    oreName = compound.getString(OreNameFilterRule.ORE_NAME_KEY)
    updateRegexp()
  }

  override def saveToNBT(compound: NBTTagCompound): Unit = {
    compound.setString(OreNameFilterRule.ORE_NAME_KEY, oreName)
  }
}
