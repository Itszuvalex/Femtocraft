package com.itszuvalex.femtocraft.industry.item

import java.util

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.industry.DustRecipeRegistry
import com.itszuvalex.femtocraft.industry.item.ItemGrinderAssembly._
import com.itszuvalex.femtocraft.industry.tile.ITileAssemblyArray
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound

import scala.collection.JavaConversions._

object ItemGrinderAssembly {
  val AssemblyType = "Grinder"

  /**
    * Ticks required = 20 tps * 10s
    */
  val TicksRequired = 20 * 10

  /**
    * Power required = Ticks required * 10 p/t
    */
  val PowerRequired = TicksRequired * 10

  private val GRINDER_COMPOUND_KEY        = "GrinderAssembly"
  private val SMELTING_STACK_COMPOUND_KEY = "GrindingStack"
  private val RESULT_STACK_COMPOUND_KEY   = "ResultStack"
  private val POWER_PROGRESS_KEY          = "Progress"

  def isWorking(item: ItemStack): Boolean = {
    if (item == null) return false

    getResultItemCompound(item).isDefined || getGrindingItemCompound(item).isDefined
  }

  def getGrindingItem(item: ItemStack): Option[ItemStack] = getGrindingItemCompound(item) match {
    case Some(comp) =>
      Option(ItemStack.loadItemStackFromNBT(comp))
    case None => None
  }

  def getGrindingItemCompound(item: ItemStack): Option[NBTTagCompound] = getGrinderCompound(item) match {
    case Some(comp) =>
      if (comp.hasKey(SMELTING_STACK_COMPOUND_KEY)) {
        Option(comp.getCompoundTag(SMELTING_STACK_COMPOUND_KEY))
      }
      else None
    case None => None
  }

  def setGrindingItem(item: ItemStack, smelting: ItemStack): Unit = getGrinderCompound(item, force = true).foreach { compound =>
    if (smelting == null) {
      compound.removeTag(SMELTING_STACK_COMPOUND_KEY)
    }
    else {
      val c = new NBTTagCompound
      smelting.writeToNBT(c)
      compound.setTag(SMELTING_STACK_COMPOUND_KEY, c)
    }
                                                                                                                   }

  def getResultItem(item: ItemStack): Option[ItemStack] = getResultItemCompound(item) match {
    case Some(comp) =>
      Option(ItemStack.loadItemStackFromNBT(comp))
    case None => None
  }

  def getResultItemCompound(item: ItemStack) = getGrinderCompound(item) match {
    case Some(comp) =>
      if (comp.hasKey(RESULT_STACK_COMPOUND_KEY)) {
        Option(comp.getCompoundTag(RESULT_STACK_COMPOUND_KEY))
      }
      else None
    case None => None
  }

  def setResultItem(item: ItemStack, result: ItemStack): Unit = getGrinderCompound(item, force = true).foreach { compound =>
    if (result == null) {
      compound.removeTag(RESULT_STACK_COMPOUND_KEY)
    }
    else {
      val c = new NBTTagCompound
      result.writeToNBT(c)
      compound.setTag(RESULT_STACK_COMPOUND_KEY, c)
    }
                                                                                                               }

  def getCurrentPowerProgress(item: ItemStack): Double = getGrinderCompound(item).map(_.getDouble(POWER_PROGRESS_KEY)).getOrElse(0)

  def setCurrentPowerProgress(item: ItemStack, progress: Double): Unit = getGrinderCompound(item, force = true).foreach(_.setDouble(POWER_PROGRESS_KEY, progress))

  def getGrinderCompound(item: ItemStack, force: Boolean = false): Option[NBTTagCompound] = Option(item).flatMap { item =>
    if (force) {
      if (item.getTagCompound == null) {
        item.setTagCompound(new NBTTagCompound)
      }
    }
    Option(item.getTagCompound)

                                                                                                                 }.map { comp =>
    if (force && !comp.hasKey(GRINDER_COMPOUND_KEY)) {
      comp.setTag(GRINDER_COMPOUND_KEY, new NBTTagCompound)
    }
    comp.getCompoundTag(GRINDER_COMPOUND_KEY)
                                                                                                                       }
}

class ItemGrinderAssembly extends Item with IItemAssembly {
  setCreativeTab(Femtocraft.tab)

  setMaxDamage(1000)

  override def registerIcons(register: IIconRegister): Unit = {
    this.itemIcon = register.registerIcon("Femtocraft" + ":" + "ItemKineticPulverizer")
  }

  override def addInformation(item: ItemStack, player: EntityPlayer, tooltip: util.List[_], advTooltip: Boolean): Unit = {
    var stringTooltip = tooltip.asInstanceOf[util.List[String]]
    if (getCurrentPowerProgress(item) > 0) {
      stringTooltip += "Progress: " + (100d - ((item.getItemDamageForDisplay.toDouble / item.getMaxDamage.toDouble) * 100d)).formatted("%.1f") + "%"
    }
    getResultItem(item) match {
      case Some(res) =>
        stringTooltip += "Result Item: " + res.getDisplayName
      case None =>
    }
    getGrindingItem(item) match {
      case Some(res) =>
        stringTooltip += "Grinding Item: " + res.getDisplayName
      case None =>
    }
  }

  override def getType(item: ItemStack) = AssemblyType

  override def onTick(item: ItemStack, tile: ITileAssemblyArray): Unit = {
    if (isWorking(item)) {
      val resultC = getResultItemCompound(item)
      if (resultC.isDefined) {
        getResultItem(item) match {
          case Some(res) => setResultItem(item, tile.addOutputItem(res))
          case None => setResultItem(item, null)
        }
        return
      }

      val time = TicksRequired * tile.getTimeMultiplier
      val power = PowerRequired * tile.getPowerMultiplier
      val powerThisTick = Math.min(power / time, power - getCurrentPowerProgress(item))
      val powerConsumed = tile.drain(powerThisTick, doDrain = true)
      val powerNext = getCurrentPowerProgress(item) + powerConsumed
      setCurrentPowerProgress(item, powerNext)
      item.setItemDamage(item.getMaxDamage - ((powerNext / power) * item.getMaxDamage).toInt)
      if (getCurrentPowerProgress(item) >= power) {
        getGrindingItem(item) match {
          case None =>
            clearGrindingItem(item)
          case Some(grind) =>
            val retOrig = DustRecipeRegistry.getDust(grind).orNull
            if (retOrig == null) {
              clearGrindingItem(item)
            }
            else {
              val ret = tile.addOutputItem(retOrig.copy())
              if (ret != null) {
                setResultItem(item, ret)
                setGrindingItem(item, null)
                setCurrentPowerProgress(item, 0)
              }
              else {
                clearGrindingItem(item)
              }
            }
        }
      }
    } else {
      (0 until tile.getInputSlots).map(i => (i, tile.getInputItem(i))).filter { case (i, it) => it != null }.exists { case (i, it) => DustRecipeRegistry.getDust(it) match {
        case Some(stack) =>
          val ite = it.copy()
          ite.stackSize = 1
          setGrindingItem(item, ite)
          tile.removeInputItem(i, 1)
          true
        case None =>
          false
      }
                                                                                                                    }
    }
  }

  def clearGrindingItem(item: ItemStack): Unit = {
    setResultItem(item, null)
    setGrindingItem(item, null)
    setCurrentPowerProgress(item, 0)
  }
}
