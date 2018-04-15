package com.itszuvalex.femtocraft.industry

import java.util
import java.util.Comparator
import java.util.regex.Pattern

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.power.item.IPowerCrystal
import com.itszuvalex.femtocraft.{FemtoBlocks, FemtoItems, Femtocraft}
import com.itszuvalex.itszulib.implicits.ItemStackImplicits._
import com.itszuvalex.itszulib.util.Comparators.ItemStack._
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary
import org.apache.logging.log4j.Level

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * Created by Christopher Harris (Itszuvalex) on 3/5/2016.
  */
object DustRecipeRegistry {
  val defaultDust = 2
  private val itemStackOverrides                                          = new util.TreeMap[ItemStack, ItemStack](new Comparator[ItemStack] {
    override def compare(x: ItemStack, y: ItemStack): Int = IDDamageWildCardNBTComparator.compare(x, y)
  }).asScala
  private val itemStackMatcherOverrides: mutable.ArrayBuffer[IDustRecipe] = new mutable.ArrayBuffer[IDustRecipe]()
  private val validOres  = mutable.Set[String]()
  private val oreDustNum = mutable.HashMap[String, Int]()
  private val oreGroupName  = "ore"
  private val dustGroupName = "dust"
  private val orePattern  = Pattern.compile("ore(?<" + oreGroupName + ">.*)")
  private val dustPattern = Pattern.compile("dust(?<" + dustGroupName + ">.*)")

  def registeredOres = validOres

  def preInit(): Unit = {}

  def init(): Unit = {}

  def postInit(): Unit = {
    extractOresFromOreDictionary()
    registerDustOverrides()

    addItemStackMapping(Blocks.STONE.newStack(), Blocks.GRAVEL.newStack())
    addItemStackMapping(Blocks.COBBLESTONE.newStack(), Blocks.GRAVEL.newStack())
    addItemStackMapping(Blocks.GRAVEL.newStack(), Blocks.SAND.newStack())
    addItemStackMapping(FemtoBlocks.blockSubstrate.newStack(), FemtoItems.itemDumbDust.newStack())
    addItemStackMapping(Items.DIAMOND.newStack(), FemtoItems.itemDiamondDust.newStack())
    addItemStackMapping(FemtoItems.itemPhasemetalIngotActivated.newStack(), FemtoItems.itemPhasemetalDust.newStack())
    addItemStackMapping(FemtoItems.itemPhasemetalIngotDevoid.newStack(), FemtoItems.itemPhasemetalDust.newStack())
    addItemStackMapping(FemtoItems.itemRiftironIngotActivated.newStack(), FemtoItems.itemRiftironDust.newStack())
    addItemStackMapping(FemtoItems.itemRiftironIngotDevoid.newStack(), FemtoItems.itemRiftironDust.newStack())

    addStackMatcher(new IDustRecipe {
      override def matches(item: ItemStack): Boolean = item != null && item.hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)

      override def result(item: ItemStack): ItemStack = {
        val amt = item.getCapability(Capabilities.ITEM_POWER_CRYSTAL, null).getType() match {
          case IPowerCrystal.TYPE_SMALL => 1
          case IPowerCrystal.TYPE_MEDIUM => 2
          case IPowerCrystal.TYPE_LARGE => 3
        }
        FemtoItems.itemCracklingDust.newStack(amt)
      }
    })
  }

  def registerDustOverrides(): Unit = {
    overrideDustMapping("oreRedstone", 6)
    overrideDustMapping("oreRedstonereplacement", 6)
  }

  def overrideDustMapping(ore: String, num: Int) = oreDustNum(ore) = num

  def addItemStackMapping(ore: ItemStack, dust: ItemStack) = {
    itemStackOverrides += ((ore, dust))
  }

  private def addStackMatcher(matcher: IDustRecipe): Unit = {
    itemStackMatcherOverrides += matcher
  }

  def extractOresFromOreDictionary(): Unit = {
    val oreOreToDustMap = mutable.HashMap[String, (Boolean, Boolean)]()

    OreDictionary.getOreNames.foreach { name =>
      val oreMatcher = orePattern.matcher(name)
      if (oreMatcher.matches()) {
        val ore = oreMatcher.group(oreGroupName)
        val prev = oreOreToDustMap.get(ore)
        oreOreToDustMap(ore) = (true, prev.exists(_._2))
      }
      val dustMatcher = dustPattern.matcher(name)
      if (dustMatcher.matches()) {
        val dust = dustMatcher.group(dustGroupName)
        val prev = oreOreToDustMap.get(dust)
        oreOreToDustMap(dust) = (prev.exists(_._1), true)
      }
    }

    oreOreToDustMap.foreach { case (ore, (bore, bdust)) =>
      Femtocraft.logger.log(Level.INFO, "Found Ore:\t%s\t\t(Ore=%b, Dust=%b)".format(ore, bore, bdust))
    }
    validOres ++= oreOreToDustMap.collect { case (ore, found) if found._1 && found._2 => ore }
    validOres.foreach { ore => Femtocraft.logger.log(Level.INFO, "Registered ore->dust mapping for Ore:\t%s".format(ore)) }
  }

  def getDust(item: ItemStack): Option[ItemStack] = {
    itemStackOverrides.find(p => ItemStack.areItemsEqual(item, p._1) && ItemStack.areItemStackTagsEqual(item, p._1)) match {
      case Some(a) =>
        return Some(a._2.copy())
      case None =>
    }

    itemStackMatcherOverrides.find(_.matches(item)) match {
      case Some(recipe) => return Some(recipe.result(item).copy())
      case None =>
    }

    OreDictionary.getOreIDs(item).map(OreDictionary.getOreName).foreach { ore =>
      getDustForOre(ore) match {
        case None =>
        case Some(grind) =>
          val ret = grind.copy()
          ret.setCount(oreDustNum.getOrElse(ore, defaultDust))
          return Some(ret)
      }
    }
    None
  }

  def getDustForOre(ore: String): Option[ItemStack] = {
    getDustOreForOre(ore) match {
      case None => None
      case Some(dust) => OreDictionary.getOres(dust).headOption
    }
  }

  def getDustOreForOre(ore: String): Option[String] = {
    val oreMatcher = orePattern.matcher(ore)
    if (oreMatcher.matches()) {
      val og = oreMatcher.group(oreGroupName)
      if (validOres.contains(og)) {
        Some("dust" + og)
      }
      else None
    }
    else None
  }

  def getDustOre(item: ItemStack): Option[String] = {
    OreDictionary.getOreIDs(item).map(OreDictionary.getOreName).foreach { ore =>
      val oreMatcher = orePattern.matcher(ore)
      if (oreMatcher.matches()) {
        val og = oreMatcher.group(oreGroupName)
        if (validOres.contains(og)) {
          return Some("dust" + og)
        }
      }
    }
    None
  }

  private trait IDustRecipe {
    def matches(item: ItemStack): Boolean

    def result(item: ItemStack): ItemStack
  }

}
