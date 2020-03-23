package com.itszuvalex.femtocraft.industry

import java.util
import java.util.Comparator
import java.util.regex.Pattern

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.power.item.IPowerCrystal
import com.itszuvalex.femtocraft.{FemtoBlocks, FemtoItems, Femtocraft}
import com.itszuvalex.itszulib.api.wrappers.{Converter, IItemStack}
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
  private val itemStackOverrides                                          = new util.TreeMap[IItemStack, IItemStack](new Comparator[IItemStack] {
    override def compare(x: IItemStack, y: IItemStack): Int = IDDamageWildCardNBTComparator.compare(x.toMinecraft, y.toMinecraft)
  }).asScala
  private val itemStackMatcherOverrides: mutable.ArrayBuffer[IDustRecipe] = new mutable.ArrayBuffer[IDustRecipe]()
  private val validOres                                                   = mutable.Set[String]()
  private val oreDustNum                                                  = mutable.HashMap[String, Int]()
  private val oreGroupName                                                = "ore"
  private val dustGroupName                                               = "dust"
  private val orePattern                                                  = Pattern.compile("ore(?<" + oreGroupName + ">.*)")
  private val dustPattern                                                 = Pattern.compile("dust(?<" + dustGroupName + ">.*)")

  def registeredOres = validOres

  def preInit(): Unit = {}

  def init(): Unit = {}

  def postInit(): Unit = {
    extractOresFromOreDictionary()
    registerDustOverrides()

    addItemStackMapping(Blocks.STONE.newIStack(), Blocks.GRAVEL.newIStack())
    addItemStackMapping(Blocks.COBBLESTONE.newIStack(), Blocks.GRAVEL.newIStack())
    addItemStackMapping(Blocks.GRAVEL.newIStack(), Blocks.SAND.newIStack())
    addItemStackMapping(FemtoBlocks.blockSubstrate.newIStack(), FemtoItems.itemDumbDust.newIStack())
    addItemStackMapping(Items.DIAMOND.newIStack(), FemtoItems.itemDiamondDust.newIStack())
    addItemStackMapping(FemtoItems.itemPhasemetalIngotActivated.newIStack(), FemtoItems.itemPhasemetalDust.newIStack())
    addItemStackMapping(FemtoItems.itemPhasemetalIngotDevoid.newIStack(), FemtoItems.itemPhasemetalDust.newIStack())
    addItemStackMapping(FemtoItems.itemRiftironIngotActivated.newIStack(), FemtoItems.itemRiftironDust.newIStack())
    addItemStackMapping(FemtoItems.itemRiftironIngotDevoid.newIStack(), FemtoItems.itemRiftironDust.newIStack())

    addStackMatcher(new IDustRecipe {
      override def matches(item: IItemStack): Boolean = item != null && item.hasModule(ManagerModules.ITEM_POWER_CRYSTAL, null)

      override def result(item: IItemStack): IItemStack = {
        val amt = item.getModule(ManagerModules.ITEM_POWER_CRYSTAL, null).getType() match {
          case IPowerCrystal.TYPE_SMALL => 1
          case IPowerCrystal.TYPE_MEDIUM => 2
          case IPowerCrystal.TYPE_LARGE => 3
        }
        FemtoItems.itemCracklingDust.newIStack(amt)
      }
    })
  }

  def registerDustOverrides(): Unit = {
    overrideDustMapping("oreRedstone", 6)
    overrideDustMapping("oreRedstonereplacement", 6)
  }

  def overrideDustMapping(ore: String, num: Int) = oreDustNum(ore) = num

  def addItemStackMapping(ore: IItemStack, dust: IItemStack) = {
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
        val ore  = oreMatcher.group(oreGroupName)
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

  def getDust(item: IItemStack): Option[IItemStack] = {
    itemStackOverrides.find(p => ItemStack.areItemsEqual(item.toMinecraft, p._1.toMinecraft) && ItemStack.areItemStackTagsEqual(item.toMinecraft, p._1.toMinecraft)) match {
      case Some(a) =>
        return Some(a._2.copy())
      case None =>
    }

    itemStackMatcherOverrides.find(_.matches(item)) match {
      case Some(recipe) => return Some(recipe.result(item).copy())
      case None =>
    }

    OreDictionary.getOreIDs(item.toMinecraft).map(OreDictionary.getOreName).foreach { ore =>
      getDustForOre(ore) match {
        case None =>
        case Some(grind) =>
          val ret = grind.copy()
          ret.stackSize = oreDustNum.getOrElse(ore, defaultDust)
          return Some(ret)
      }
    }
    None
  }

  def getDustForOre(ore: String): Option[IItemStack] = {
    getDustOreForOre(ore) match {
      case None => None
      case Some(dust) => OreDictionary.getOres(dust).map(Converter.IItemStackFromItemStack).headOption
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

  def getDustOre(item: IItemStack): Option[String] = {
    OreDictionary.getOreIDs(item.toMinecraft).map(OreDictionary.getOreName).foreach { ore =>
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
    def matches(item: IItemStack): Boolean

    def result(item: IItemStack): IItemStack
  }

}
