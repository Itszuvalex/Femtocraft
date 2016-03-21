package com.itszuvalex.femtocraft.util

import com.itszuvalex.itszulib.implicits.IDImplicits._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.util.Comparators.ItemStack._
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound


/**
  * Created by Christopher Harris (Itszuvalex) on 2/1/2016.
  */
object ItemStackFilterRule {
  lazy val registered = registerRule()
  registered

  val filterType = "ItemStackFilter"

  val STACK_KEY            = "ItemStack"
  val NBT_SENSITIVE_KEY    = "NBTSensitive"
  val DAMAGE_SENSITIVE_KEY = "DamageSensitive"

  private def registerRule(): Boolean = {
    ItemFilterRule.registerFilterType(filterType, classOf[ItemStackFilterRule])
    true
  }
}

class ItemStackFilterRule extends IItemFilterRule {
  ItemStackFilterRule.registered

  private var stack: ItemStack = null
  private var nbtSensitive     = true
  private var damageSensitive  = true

  private var stackID: Int = -1

  def getStack = stack

  def setStack(stack: ItemStack): Unit = {
    this.stack = stack
    stackID = stack match {
      case null => -1
      case is =>
        is.getItem match {
          case null => -1
          case item =>
            is.itemID
        }
    }
  }

  override def itemMatches(item: ItemStack) = {
    (stack, item, nbtSensitive, damageSensitive) match {
      case (a, b, false, false) => IDComparator.compare(a, b) == 0
      case (a, b, true, _) => IDDamageWildCardComparator.compare(a, b) == 0
      case (a, b, false, true) => IDDamageWildCardNBTComparator.compare(a, b) == 0
      case _ => false
    }
  }

  override def ruleType = ItemStackFilterRule.filterType

  override def loadFromNBT(compound: NBTTagCompound): Unit = {
    setDamageSensitive(compound.Bool(ItemStackFilterRule.DAMAGE_SENSITIVE_KEY))
    setNBTSensitive(compound.Bool(ItemStackFilterRule.NBT_SENSITIVE_KEY))
    compound.NBTCompound(ItemStackFilterRule.STACK_KEY) { comp => stack = ItemStack.loadItemStackFromNBT(comp)
      Unit
                                                        }
  }

  override def saveToNBT(compound: NBTTagCompound): Unit = {
    compound(ItemStackFilterRule.DAMAGE_SENSITIVE_KEY -> isDamageSensitive,
             ItemStackFilterRule.NBT_SENSITIVE_KEY -> isNBTSensitive,
             ItemStackFilterRule.STACK_KEY -> {
               val comp = new NBTTagCompound
               stack.writeToNBT(comp)
               comp
             }
            )
  }

  def isDamageSensitive = damageSensitive

  def setDamageSensitive(sensitive: Boolean) = {
    damageSensitive = sensitive
    if (isNBTSensitive)
      setNBTSensitive(false)
  }

  def isNBTSensitive = nbtSensitive

  def setNBTSensitive(sensitive: Boolean) = {
    nbtSensitive = sensitive
    if (isNBTSensitive)
      damageSensitive = true
  }
}
