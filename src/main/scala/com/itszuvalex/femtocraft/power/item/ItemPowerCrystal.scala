package com.itszuvalex.femtocraft.power.item

import java.util

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.item.IPowerCrystal._
import com.itszuvalex.femtocraft.power.item.ItemPowerCrystal._
import com.itszuvalex.itszulib.implicits.ItemStackImplicits._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.IIcon

import scala.collection.JavaConversions._
import scala.collection._

/**
  * Created by Christopher on 8/26/2015.
  */
object ItemPowerCrystal {
  val TEXTURE_PREFIX      = "ItemCrystal"
  val NBT_COMPOUND_KEY    = "PowerCrystal"
  val COLOR_KEY           = "Color"
  val TYPE_KEY            = "Type"
  val RANGE_KEY           = "Range"
  val STORAGE_CURRENT_KEY = "Storage_Current"
  val STORAGE_MAX_KEY     = "Storage_Max"
  val PASSIVE_GEN_KEY     = "Passive"
  val TRANSFER_KEY        = "Transfer"
  val NAME_KEY            = "Name"

  val DEFAULT_ICON_TYPE = TYPE_LARGE

  private val typeIconMap = mutable.HashMap[String, IIcon]()

  def addMapping(ctype: String, icon: IIcon) = typeIconMap.put(ctype, icon)

  def getIcon(ctype: String) = typeIconMap.get(ctype)

  def getColor(stack: ItemStack): Int = {
    if (stack != null)
      stack.getTagCompound.NBTCompound(NBT_COMPOUND_KEY) { comp =>
        return comp.Int(COLOR_KEY)
                                                         }
    Color(255.toByte, 255.toByte, 255.toByte, 255.toByte).toInt
  }

  def getTransferRate(stack: ItemStack): Int = {
    if (stack != null)
      stack.getTagCompound.NBTCompound(NBT_COMPOUND_KEY) { comp =>
        return comp.Int(TRANSFER_KEY)
                                                         }
    0
  }

  def getRange(stack: ItemStack): Float = {
    if (stack != null)
      stack.getTagCompound.NBTCompound(NBT_COMPOUND_KEY) { comp =>
        return comp.Float(RANGE_KEY)
                                                         }
    0
  }

  def getPassiveGen(stack: ItemStack): Float = {
    if (stack != null)
      stack.getTagCompound.NBTCompound(NBT_COMPOUND_KEY) { comp =>
        return comp.Float(PASSIVE_GEN_KEY)
                                                         }
    0
  }

  def getFullName(stack: ItemStack) = getType(stack) + " " + getName(stack)

  def getType(stack: ItemStack): String = {
    if (stack != null)
      stack.getTagCompound.NBTCompound(NBT_COMPOUND_KEY) { comp =>
        return comp.String(TYPE_KEY)
                                                         }
    null
  }

  def getName(stack: ItemStack): String = {
    if (stack != null)
      stack.getTagCompound.NBTCompound(NBT_COMPOUND_KEY) { comp =>
        return comp.String(NAME_KEY)
                                                         }
    ""
  }

  def store(stack: ItemStack, amount: Long, doStore: Boolean): Long = {
    var ret = 0L
    if (stack != null) {
      ret = Math.min(amount, getStorageMax(stack) - getStorageCurrent(stack))
      if (doStore)
        setStorageCurrent(stack, getStorageCurrent(stack) + ret)
    }
    ret
  }

  def getStorageMax(stack: ItemStack): Long = {
    if (stack != null)
      stack.getTagCompound.NBTCompound(NBT_COMPOUND_KEY) { comp =>
        return comp.Long(STORAGE_MAX_KEY)
                                                         }
    0
  }

  def consume(stack: ItemStack, amount: Long, doConsume: Boolean): Long = {
    var ret = 0L
    if (stack != null) {
      ret = Math.min(amount, getStorageCurrent(stack))
      if (doConsume)
        setStorageCurrent(stack, getStorageCurrent(stack) - ret)
    }
    ret
  }

  def getStorageCurrent(stack: ItemStack): Long = {
    if (stack != null)
      stack.getTagCompound.NBTCompound(NBT_COMPOUND_KEY) { comp =>
        return comp.Long(STORAGE_CURRENT_KEY)
                                                         }
    0
  }

  def setStorageCurrent(stack: ItemStack, amount: Long): ItemStack = {
    if (stack != null)
      stack.forceTag.merge(NBT_COMPOUND_KEY ->
                           NBTCompound(
                                        STORAGE_CURRENT_KEY -> amount
                                      )
                          )
    stack
  }

  def initialize(stack: ItemStack,
                 name: String,
                 rtype: String,
                 color: Int,
                 range: Float,
                 storage: Long,
                 passiveGen: Float,
                 transfer: Int) =
    setName(setType(setColor(setRange(setStorageMax(setStorageCurrent(setPassiveGen(setTransferRate(stack, transfer), passiveGen), storage), storage), range), color), rtype), name)

  def setColor(stack: ItemStack, color: Int): ItemStack = {
    if (stack != null)
      stack.forceTag.merge(NBT_COMPOUND_KEY ->
                           NBTCompound(
                                        COLOR_KEY -> color
                                      )
                          )
    stack
  }

  def setTransferRate(stack: ItemStack, rate: Int): ItemStack = {
    if (stack != null)
      stack.forceTag.merge(NBT_COMPOUND_KEY ->
                           NBTCompound(
                                        TRANSFER_KEY -> rate
                                      )
                          )
    stack
  }

  def setType(stack: ItemStack, ctype: String): ItemStack = {
    if (stack != null)
      stack.forceTag.merge(NBT_COMPOUND_KEY ->
                           NBTCompound(
                                        TYPE_KEY -> ctype
                                      )
                          )
    stack
  }

  def setRange(stack: ItemStack, range: Float): ItemStack = {
    if (stack != null)
      stack.forceTag.merge(NBT_COMPOUND_KEY ->
                           NBTCompound(
                                        RANGE_KEY -> range
                                      )
                          )
    stack
  }

  def setPassiveGen(stack: ItemStack, passiveGen: Float): ItemStack = {
    if (stack != null)
      stack.forceTag.merge(NBT_COMPOUND_KEY ->
                           NBTCompound(
                                        PASSIVE_GEN_KEY -> passiveGen)
                          )
    stack
  }

  def setName(stack: ItemStack, name: String): ItemStack = {
    if (stack != null)
      stack.forceTag.merge(NBT_COMPOUND_KEY ->
                           NBTCompound(
                                        NAME_KEY -> name
                                      )
                          )
    stack
  }

  def setStorageMax(stack: ItemStack, amount: Long): ItemStack = {
    if (stack != null)
      stack.forceTag.merge(NBT_COMPOUND_KEY ->
                           NBTCompound(
                                        STORAGE_MAX_KEY -> amount
                                      )
                          )
    stack
  }
}

class ItemPowerCrystal extends Item with IPowerCrystal {
  override def getDurabilityForDisplay(stack: ItemStack): Double = {
    val max = ItemPowerCrystal.getStorageMax(stack)
    if (max > 0)
      ItemPowerCrystal.getStorageCurrent(stack) / max
    else 0
  }

  override def getColorFromItemStack(stack: ItemStack, renderPass: Int): Int = if (renderPass > 0) super.getColorFromItemStack(stack, renderPass) else ItemPowerCrystal.getColor(stack)

  /**
    *
    * @param stack
    * @return Color of the crystal.
    */
  override def getColor(stack: ItemStack) = ItemPowerCrystal.getColor(stack)

  override def registerIcons(ir: IIconRegister): Unit = {
    addMapping(TYPE_SMALL, ir.registerIcon(Femtocraft.ID + ":" + TEXTURE_PREFIX + "_" + TYPE_SMALL))
    addMapping(TYPE_MEDIUM, ir.registerIcon(Femtocraft.ID + ":" + TEXTURE_PREFIX + "_" + TYPE_MEDIUM))
    addMapping(TYPE_LARGE, ir.registerIcon(Femtocraft.ID + ":" + TEXTURE_PREFIX + "_" + TYPE_LARGE))
  }

  override def getIcon(stack: ItemStack, pass: Int): IIcon = getIconFromStack(stack)

  override def getIconIndex(stack: ItemStack): IIcon = getIconFromStack(stack)

  private def getIconFromStack(stack: ItemStack): IIcon = {
    ItemPowerCrystal.getIcon(ItemPowerCrystal.getType(stack))
    .getOrElse(
                ItemPowerCrystal.getIcon(DEFAULT_ICON_TYPE).orNull
              )
  }

  override def getName(stack: ItemStack) = ItemPowerCrystal.getName(stack)

  override def addInformation(stack: ItemStack, player: EntityPlayer, tooltipList: util.List[_], advTooltips: Boolean): Unit = {
    super.addInformation(stack, player, tooltipList, advTooltips)
    val tlist = tooltipList.asInstanceOf[java.util.List[String]]
    tlist += "Crystal Type:" + getType(stack)
    tlist += "Passive Gen:" + getPassiveGen(stack)
    tlist += "Transfer Range:" + getRange(stack)
    tlist += "Transfer Rate:" + getTransferRate(stack)
    tlist += "Power:" + getStorageCurrent(stack) + "/" + getStorageMax(stack)
  }

  /**
    *
    * @param stack
    * @return Size of the crystal.
    */
  override def getType(stack: ItemStack) = ItemPowerCrystal.getType(stack)

  /**
    *
    * @param stack
    * @return Range to allow connections in.
    */
  override def getRange(stack: ItemStack) = ItemPowerCrystal.getRange(stack)

  /**
    *
    * @param stack
    * @return Amount of power to generate per tick.
    */
  override def getPassiveGen(stack: ItemStack) = ItemPowerCrystal.getPassiveGen(stack)

  /**
    *
    * @param stack
    * @return Maximum amount of power that can flow from this crystal.  This is meant to be per-tick, divided among children.
    */
  override def getTransferRate(stack: ItemStack): Int = ItemPowerCrystal.getTransferRate(stack)

  /**
    *
    * @param stack
    * @return Maximum amount of power crystal can store.
    */
  override def getStorageMax(stack: ItemStack): Long = ItemPowerCrystal.getStorageMax(stack)

  /**
    *
    * @param stack
    * @return Current amount of power crystal is storing.
    */
  override def getStorageCurrent(stack: ItemStack): Long = ItemPowerCrystal.getStorageCurrent(stack)

  /**
    *
    * @param amount Amount of power to attempt to consume.
    * @param doConsume Pass true to actually consume resources.  False simulates the store.
    * @return Amount of @amount successfully removed.
    */
  override def consume(stack: ItemStack, amount: Long, doConsume: Boolean): Long = ItemPowerCrystal.consume(stack, amount, doConsume)

  /**
    *
    * @param amount Amount of power to store.
    * @param doStore Pass true to actually consume resources.  False simulates the store.
    * @return Amount of @amount successfully stored.
    */
  override def store(stack: ItemStack, amount: Long, doStore: Boolean): Long = ItemPowerCrystal.store(stack, amount, doStore)
}
