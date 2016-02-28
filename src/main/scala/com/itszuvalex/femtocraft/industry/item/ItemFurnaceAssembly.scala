package com.itszuvalex.femtocraft.industry.item

import java.util

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.industry.item.ItemFurnaceAssembly._
import com.itszuvalex.femtocraft.industry.tile.ITileAssemblyArray
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.crafting.FurnaceRecipes
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound

import scala.collection.JavaConversions._

/**
  * Created by Christopher Harris (Itszuvalex) on 2/27/2016.
  */
object ItemFurnaceAssembly {
  val AssemblyType = "Furnace"

  /**
    * Ticks required = 20 tps * 10s
    */
  val TicksRequired = 20 * 10

  /**
    * Power required = Ticks required * 10 p/t
    */
  val PowerRequired = TicksRequired * 10

  private val FURNACE_COMPOUND_KEY        = "FurnaceAssembly"
  private val SMELTING_STACK_COMPOUND_KEY = "SmeltingItem"
  private val RESULT_STACK_COMPOUND_KEY   = "ResultStack"
  private val POWER_PROGRESS_KEY          = "Progress"

  def isWorking(item: ItemStack): Boolean = {
    if (item == null) return false

    getResultItemCompound(item).isDefined || getSmeltingItemCompound(item).isDefined
  }

  def getResultItemCompound(item: ItemStack) = getFurnaceCompound(item) match {
    case Some(comp) =>
      if (comp.hasKey(RESULT_STACK_COMPOUND_KEY)) {
        Option(comp.getCompoundTag(RESULT_STACK_COMPOUND_KEY))
      }
      else None
    case None => None
  }

  def getSmeltingItem(item: ItemStack): Option[ItemStack] = getSmeltingItemCompound(item) match {
    case Some(comp) =>
      Option(ItemStack.loadItemStackFromNBT(comp))
    case None => None
  }

  def getSmeltingItemCompound(item: ItemStack): Option[NBTTagCompound] = getFurnaceCompound(item) match {
    case Some(comp) =>
      if (comp.hasKey(SMELTING_STACK_COMPOUND_KEY)) {
        Option(comp.getCompoundTag(SMELTING_STACK_COMPOUND_KEY))
      }
      else None
    case None => None
  }

  def setSmeltingItem(item: ItemStack, smelting: ItemStack): Unit = getFurnaceCompound(item, force = true).foreach { compound =>
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

  def setResultItem(item: ItemStack, result: ItemStack): Unit = getFurnaceCompound(item, force = true).foreach { compound =>
    if (result == null) {
      compound.removeTag(RESULT_STACK_COMPOUND_KEY)
    }
    else {
      val c = new NBTTagCompound
      result.writeToNBT(c)
      compound.setTag(RESULT_STACK_COMPOUND_KEY, c)
    }
                                                                                                               }

  def getFurnaceCompound(item: ItemStack, force: Boolean = false): Option[NBTTagCompound] = Option(item).flatMap { item =>
    if (force) {
      if (item.getTagCompound == null) {
        item.setTagCompound(new NBTTagCompound)
      }
    }
    Option(item.getTagCompound)

                                                                                                                 }.map { comp =>
    if (force && !comp.hasKey(FURNACE_COMPOUND_KEY)) {
      comp.setTag(FURNACE_COMPOUND_KEY, new NBTTagCompound)
    }
    comp.getCompoundTag(FURNACE_COMPOUND_KEY)
                                                                                                                       }

  def getCurrentPowerProgress(item: ItemStack): Double = getFurnaceCompound(item).map(_.getDouble(POWER_PROGRESS_KEY)).getOrElse(0)

  def setCurrentPowerProgress(item: ItemStack, progress: Double): Unit = getFurnaceCompound(item, force = true).foreach(_.setDouble(POWER_PROGRESS_KEY, progress))
}

class ItemFurnaceAssembly extends Item with IItemAssembly {
  setCreativeTab(Femtocraft.tab)

  setMaxDamage(1000)

  override def registerIcons(register: IIconRegister): Unit = {
    this.itemIcon = register.registerIcon("Femtocraft" + ":" + "ItemDissassemblyArray")
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
    getSmeltingItem(item) match {
      case Some(res) =>
        stringTooltip += "Smelting Item: " + res.getDisplayName
      case None =>
    }
  }

  override def getType(item: ItemStack) = AssemblyType

  override def onTick(item: ItemStack, tile: ITileAssemblyArray): Unit = {
    if (isWorking(item)) {
      val resultC = getResultItemCompound(item)
      if (resultC.isDefined) {
        getResultItem(item) match {
          case Some(res) =>
            var it = res
            (0 until tile.getOutputSlots).exists { i =>
              it = tile.addOrMergeOutputItem(it, i)
              it == null
                                                 }
            setResultItem(item, it)
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
        getSmeltingItem(item) match {
          case None =>
            setSmeltingItem(item, null)
          case Some(smelt) =>
            val ret = FurnaceRecipes.smelting().getSmeltingResult(smelt).copy()
            if ((0 until tile.getOutputSlots).forall(i => tile.addOrMergeOutputItem(ret, i) != null)) {
              setResultItem(item, ret)
              setSmeltingItem(item, null)
              setCurrentPowerProgress(item, 0)
            }
            else {
              setResultItem(item, null)
              setSmeltingItem(item, null)
              setCurrentPowerProgress(item, 0)
            }
        }
      }
    } else {
      (0 until tile.getInputSlots).map(i => (i, tile.getInputItem(i))).filter { case (i, it) => it != null }.exists { case (i, it) => Option(FurnaceRecipes.smelting().getSmeltingResult(it)) match {
        case Some(stack) =>
          val ite = it.copy()
          ite.stackSize = 1
          setSmeltingItem(item, ite)
          tile.removeInputItem(i, 1)
          true
        case None =>
          false
      }
                                                                                                                    }
    }
  }
}
