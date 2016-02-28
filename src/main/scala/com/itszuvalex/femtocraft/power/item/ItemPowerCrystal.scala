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
  val STORAGE_CURRENT_KEY = "Storage_Current"
  val STORAGE_MAX_KEY     = "Storage_Max"
  val STORAGE_PARTIAL_KEY = "Storage_Partial"
  val PASSIVE_GEN_KEY     = "Passive"
  val TRANSFER_KEY        = "Transfer"
  val NAME_KEY            = "Name"

  val DEFAULT_ICON_TYPE = TYPE_LARGE

  private val typeIconMap = mutable.HashMap[String, IIcon]()

  def addMapping(ctype: String, icon: IIcon) = typeIconMap.put(ctype, icon)

  def onTick(stack: ItemStack): Unit = {
    if (stack == null) return
    stack.getItem match {
      case null =>
      case crystal: IPowerCrystal =>
        crystal.setStoragePartial(stack, crystal.getStoragePartial(stack) + crystal.getPassiveGen(stack))
        if (crystal.getStoragePartial(stack) > 1) {
          crystal.setStoragePartial(stack, crystal.getStoragePartial(stack) - 1)
          crystal.store(stack, 1, doStore = true)
        }
      case _ =>
    }

  }

  def addInformation(stack: ItemStack, tooltipList: util.List[_]): Unit = {
    val tlist = tooltipList.asInstanceOf[util.List[String]]
    stack.getItem match {
      case null =>
      case crystal: IPowerCrystal =>
        tlist += "Crystal Type:" + crystal.getType(stack)
        tlist += "Passive Gen:" + crystal.getPassiveGen(stack).formatted("%.2f")
        tlist += "Transfer Rate:" + crystal.getTransferRate(stack)
        tlist += "Power:" + crystal.getStorageCurrent(stack) + "/" + crystal.getStorageMax(stack)
      //        tlist += "Partial Power:" + crystal.getStoragePartial(stack)
      case _ =>
    }
  }

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

  def store(stack: ItemStack, amount: Double, doStore: Boolean): Double = {
    var ret = 0d
    if (stack != null) {
      stack.getItem match {
        case null =>
        case crystal: IPowerCrystal =>
          ret = Math.min(amount, crystal.getStorageMax(stack) - crystal.getStorageCurrent(stack))
          if (doStore)
            crystal.setStorageCurrent(stack, crystal.getStorageCurrent(stack) + ret)
        case _ =>
      }
    }
    ret
  }

  def getStorageMax(stack: ItemStack): Double = {
    if (stack != null)
      stack.getTagCompound.NBTCompound(NBT_COMPOUND_KEY) { comp =>
        return comp.Double(STORAGE_MAX_KEY)
                                                         }
    0
  }

  def getStoragePartial(stack: ItemStack): Double = {
    if (stack != null)
      stack.getTagCompound.NBTCompound(NBT_COMPOUND_KEY) { comp =>
        return comp.Double(STORAGE_PARTIAL_KEY)
                                                         }
    0
  }

  def consume(stack: ItemStack, amount: Double, doConsume: Boolean): Double = {
    var ret = 0d
    if (stack != null) {
      stack.getItem match {
        case null =>
        case crystal: IPowerCrystal =>
          ret = Math.min(amount, crystal.getStorageCurrent(stack))
          if (doConsume)
            crystal.setStorageCurrent(stack, crystal.getStorageCurrent(stack) - ret)
        case _ =>
      }
    }
    ret
  }

  def getStorageCurrent(stack: ItemStack): Double = {
    if (stack != null)
      stack.getTagCompound.NBTCompound(NBT_COMPOUND_KEY) { comp =>
        return comp.Double(STORAGE_CURRENT_KEY)
                                                         }
    0
  }

  def initialize(stack: ItemStack,
                 name: String,
                 rtype: String,
                 color: Int,
                 storage: Double,
                 passiveGen: Float,
                 transfer: Int) = {
    stack match {
      case null =>
      case s =>
        s.getItem match {
          case null =>
          case crystal: IPowerCrystal =>
            crystal.setName(stack, name)
            crystal.setType(stack, rtype)
            crystal.setColor(stack, color)
            crystal.setStorageMax(stack, storage)
            crystal.setStorageCurrent(stack, storage / 2)
            crystal.setPassiveGen(stack, passiveGen)
            crystal.setTransferRate(stack, transfer)
            crystal.setStoragePartial(stack, 0)
          case _ =>
        }
    }
    stack
  }

  def setStorageCurrent(stack: ItemStack, amount: Double): Unit = {
    if (stack != null)
      stack.forceTag.merge(NBT_COMPOUND_KEY ->
                           NBTCompound(
                                        STORAGE_CURRENT_KEY -> amount
                                      )
                          )
  }

  def setColor(stack: ItemStack, color: Int): Unit = {
    if (stack != null)
      stack.forceTag.merge(NBT_COMPOUND_KEY ->
                           NBTCompound(
                                        COLOR_KEY -> color
                                      )
                          )
  }

  def setTransferRate(stack: ItemStack, rate: Int): Unit = {
    if (stack != null)
      stack.forceTag.merge(NBT_COMPOUND_KEY ->
                           NBTCompound(
                                        TRANSFER_KEY -> rate
                                      )
                          )
  }

  def setType(stack: ItemStack, ctype: String): Unit = {
    if (stack != null)
      stack.forceTag.merge(NBT_COMPOUND_KEY ->
                           NBTCompound(
                                        TYPE_KEY -> ctype
                                      )
                          )
  }

  def setPassiveGen(stack: ItemStack, passiveGen: Float): Unit = {
    if (stack != null)
      stack.forceTag.merge(NBT_COMPOUND_KEY ->
                           NBTCompound(
                                        PASSIVE_GEN_KEY -> passiveGen)
                          )
  }

  def setName(stack: ItemStack, name: String): Unit = {
    if (stack != null)
      stack.forceTag.merge(NBT_COMPOUND_KEY ->
                           NBTCompound(
                                        NAME_KEY -> name
                                      )
                          )
  }

  def setStorageMax(stack: ItemStack, amount: Double): Unit = {
    if (stack != null)
      stack.forceTag.merge(NBT_COMPOUND_KEY ->
                           NBTCompound(
                                        STORAGE_MAX_KEY -> amount
                                      )
                          )
  }

  def setStoragePartial(stack: ItemStack, amount: Double): Unit = {
    if (stack != null)
      stack.forceTag.merge(NBT_COMPOUND_KEY ->
                           NBTCompound(
                                        STORAGE_PARTIAL_KEY -> amount
                                      )
                          )
  }
}

class ItemPowerCrystal extends Item with IPowerCrystal {
  setNoRepair()
  setMaxDamage(100)

  override def onTick(stack: ItemStack) = ItemPowerCrystal.onTick(stack)

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

  private def getIconFromStack(stack: ItemStack): IIcon = {
    ItemPowerCrystal.getIcon(ItemPowerCrystal.getType(stack))
    .getOrElse(
                ItemPowerCrystal.getIcon(DEFAULT_ICON_TYPE).orNull
              )
  }

  override def getIconIndex(stack: ItemStack): IIcon = getIconFromStack(stack)

  override def getName(stack: ItemStack) = ItemPowerCrystal.getName(stack)

  override def addInformation(stack: ItemStack, player: EntityPlayer, tooltipList: util.List[_], advTooltips: Boolean): Unit = {
    super.addInformation(stack, player, tooltipList, advTooltips)
    ItemPowerCrystal.addInformation(stack, tooltipList)
  }

  override def getStoragePartial(stack: ItemStack) = ItemPowerCrystal.getStoragePartial(stack)

  /**
    *
    * @param stack
    * @return Size of the crystal.
    */
  override def getType(stack: ItemStack) = ItemPowerCrystal.getType(stack)

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
  override def getTransferRate(stack: ItemStack) = ItemPowerCrystal.getTransferRate(stack)

  /**
    *
    * @param stack
    * @return Maximum amount of power crystal can store.
    */
  override def getStorageMax(stack: ItemStack) = ItemPowerCrystal.getStorageMax(stack)

  /**
    *
    * @param stack
    * @return Current amount of power crystal is storing.
    */
  override def getStorageCurrent(stack: ItemStack) = ItemPowerCrystal.getStorageCurrent(stack)

  /**
    *
    * @param amount Amount of power to attempt to consume.
    * @param doConsume Pass true to actually consume resources.  False simulates the store.
    * @return Amount of @amount successfully removed.
    */
  override def consume(stack: ItemStack, amount: Double, doConsume: Boolean) = ItemPowerCrystal.consume(stack, amount, doConsume)

  /**
    *
    * @param amount Amount of power to store.
    * @param doStore Pass true to actually consume resources.  False simulates the store.
    * @return Amount of @amount successfully stored.
    */
  override def store(stack: ItemStack, amount: Double, doStore: Boolean) = ItemPowerCrystal.store(stack, amount, doStore)

  override def setStorageCurrent(stack: ItemStack, amount: Double): Unit = {
    ItemPowerCrystal.setStorageCurrent(stack, amount)
    updateDamage(stack)
  }

  def updateDamage(stack: ItemStack): Unit = {
    val max = ItemPowerCrystal.getStorageMax(stack)
    if (max > 0)
      stack.setItemDamage(stack.getMaxDamage - ((ItemPowerCrystal.getStorageCurrent(stack) / max) * stack.getMaxDamage).toInt)
  }

  override def setStoragePartial(stack: ItemStack, amount: Double) = ItemPowerCrystal.setStoragePartial(stack, amount)

  override def setPassiveGen(stack: ItemStack, passiveGen: Float) = ItemPowerCrystal.setPassiveGen(stack, passiveGen)

  override def setType(stack: ItemStack, ctype: String) = ItemPowerCrystal.setType(stack, ctype)

  override def setTransferRate(stack: ItemStack, rate: Int) = ItemPowerCrystal.setTransferRate(stack, rate)

  override def setName(stack: ItemStack, name: String) = ItemPowerCrystal.setName(stack, name)

  override def setColor(stack: ItemStack, color: Int) = ItemPowerCrystal.setColor(stack, color)

  override def setStorageMax(stack: ItemStack, amount: Double): Unit = {
    ItemPowerCrystal.setStorageMax(stack, amount)
    updateDamage(stack)
  }
}
