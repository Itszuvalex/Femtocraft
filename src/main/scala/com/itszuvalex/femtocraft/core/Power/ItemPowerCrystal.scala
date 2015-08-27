package com.itszuvalex.femtocraft.core.Power

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.core.Power.IPowerCrystal._
import com.itszuvalex.femtocraft.core.Power.ItemPowerCrystal._
import com.itszuvalex.itszulib.implicits.ItemStackImplicits._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.IIcon

import scala.collection._

/**
 * Created by Christopher on 8/26/2015.
 */
object ItemPowerCrystal {
  val TEXTURE_PREFIX   = "ItemCrystal"
  val NBT_COMPOUND_KEY = "PowerCrystal"
  val COLOR_KEY        = "Color"
  val TYPE_KEY         = "Type"
  val RANGE_KEY        = "Range"
  val STORAGE_KEY      = "Storage"
  val PASSIVE_GEN_KEY  = "Passive"
  val NAME_KEY         = "Name"

  val DEFAULT_ICON_TYPE = IPowerCrystal.TYPE_LARGE

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

  def setColor(stack: ItemStack, color: Int): ItemStack = {
    if (stack != null)
      stack.forceTag.merge(NBT_COMPOUND_KEY ->
                     NBTCompound(
                                  COLOR_KEY -> color
                                )
                    )
    stack
  }

  def getType(stack: ItemStack): String = {
    if (stack != null)
      stack.getTagCompound.NBTCompound(NBT_COMPOUND_KEY) { comp =>
        return comp.String(TYPE_KEY)
                                                         }
    DEFAULT_ICON_TYPE
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

  def getRange(stack: ItemStack): Float = {
    if (stack != null)
      stack.getTagCompound.NBTCompound(NBT_COMPOUND_KEY) { comp =>
        return comp.Float(RANGE_KEY)
                                                         }
    0
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

  def getStorageMultiplier(stack: ItemStack): Float = {
    if (stack != null)
      stack.getTagCompound.NBTCompound(NBT_COMPOUND_KEY) { comp =>
        return comp.Float(STORAGE_KEY)
                                                         }
    0
  }

  def setStorageMultiplier(stack: ItemStack, storage: Float): ItemStack = {
    if (stack != null)
      stack.forceTag.merge(NBT_COMPOUND_KEY ->
                     NBTCompound(
                                  STORAGE_KEY -> storage
                                )
                    )
    stack

  }

  def getPassiveGen(stack: ItemStack): Float = {
    if (stack != null)
      stack.getTagCompound.NBTCompound(NBT_COMPOUND_KEY) { comp =>
        return comp.Float(PASSIVE_GEN_KEY)
                                                         }
    0
  }

  def setPassiveGen(stack: ItemStack, passiveGen: Float): ItemStack = {
    if (stack != null)
      stack.forceTag.merge(NBT_COMPOUND_KEY ->
                     NBTCompound(
                                  PASSIVE_GEN_KEY -> passiveGen)
                    )
    stack
  }

  def getName(stack: ItemStack): String = {
    if (stack != null)
      stack.getTagCompound.NBTCompound(NBT_COMPOUND_KEY) { comp =>
        return comp.String(NAME_KEY)
                                                         }
    ""
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

  def getFullName(stack: ItemStack) = getType(stack) + " " + getName(stack)

  def initialize(stack: ItemStack,
                 name: String,
                 rtype: String,
                 color: Int,
                 range: Float,
                 storage: Float,
                 passiveGen: Float) =
    setName(setType(setColor(setRange(setStorageMultiplier(setPassiveGen(stack, passiveGen), storage), range), color), rtype), name)
}

class ItemPowerCrystal extends Item with IPowerCrystal {
  override def getColorFromItemStack(stack: ItemStack, renderPass: Int): Int = if (renderPass > 0) super.getColorFromItemStack(stack, renderPass) else getColor(stack)

  /**
   *
   * @param stack
   * @return Color of the crystal.
   */
  override def getColor(stack: ItemStack) = ItemPowerCrystal.getColor(stack)

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
   * @return Amount to multiply storage amount by.
   */
  override def getStorageMultiplier(stack: ItemStack) = ItemPowerCrystal.getStorageMultiplier(stack)

  override def registerIcons(ir: IIconRegister): Unit = {
    ItemPowerCrystal.addMapping(TYPE_SMALL, ir.registerIcon(Femtocraft.ID + ":" + TEXTURE_PREFIX + "_" + TYPE_SMALL))
    ItemPowerCrystal.addMapping(TYPE_MEDIUM, ir.registerIcon(Femtocraft.ID + ":" + TEXTURE_PREFIX + "_" + TYPE_MEDIUM))
    ItemPowerCrystal.addMapping(TYPE_LARGE, ir.registerIcon(Femtocraft.ID + ":" + TEXTURE_PREFIX + "_" + TYPE_LARGE))
  }

  override def getIcon(stack: ItemStack, pass: Int): IIcon = getIconFromStack(stack)

  override def getIconIndex(stack: ItemStack): IIcon = getIconFromStack(stack)

  private def getIconFromStack(stack: ItemStack): IIcon = {
    ItemPowerCrystal.getIcon(ItemPowerCrystal.getType(stack))
    .getOrElse(
        ItemPowerCrystal.getIcon(ItemPowerCrystal.DEFAULT_ICON_TYPE).orNull
              )
  }

  /**
   *
   * @param stack
   * @return Amount of power to generate per tick.
   */
  override def getPassiveGen(stack: ItemStack) = ItemPowerCrystal.getPassiveGen(stack)

  override def getName(stack: ItemStack) = ItemPowerCrystal.getName(stack)
}
