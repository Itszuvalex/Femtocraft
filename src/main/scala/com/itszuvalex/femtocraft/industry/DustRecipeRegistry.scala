package com.itszuvalex.femtocraft.industry

import java.util.regex.Pattern

import com.itszuvalex.femtocraft.Femtocraft
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary
import org.apache.logging.log4j.Level

import scala.collection.JavaConversions._
import scala.collection.mutable

/**
  * Created by Christopher Harris (Itszuvalex) on 3/5/2016.
  */
object DustRecipeRegistry {
  val defaultDust = 2

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
  }

  def registerDustOverrides(): Unit = {
    overrideDustMapping("oreRedstone", 6)
  }

  def overrideDustMapping(ore: String, num: Int) = oreDustNum(ore) = num

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
    OreDictionary.getOreIDs(item).map(OreDictionary.getOreName).foreach { ore =>
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

}
