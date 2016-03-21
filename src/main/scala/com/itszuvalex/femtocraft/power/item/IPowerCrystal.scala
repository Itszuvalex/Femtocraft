package com.itszuvalex.femtocraft.power.item

import net.minecraft.item.{Item, ItemStack}

/**
  * Created by Christopher on 7/29/2015.
  */
object IPowerCrystal {
  val TYPE_SMALL  = "small"
  val TYPE_MEDIUM = "medium"
  val TYPE_LARGE  = "large"
}

trait IPowerCrystal extends Item with IPowerStorage {

  /**
    * Used to trigger passive trickle charging.
    */
  def onTick(stack: ItemStack): Unit

  def getName(stack: ItemStack): String

  /**
    *
    * @param stack
    * @return Color of the crystal.
    */
  def getColor(stack: ItemStack): Int

  /**
    *
    * @param stack
    * @return Amount of power to generate per tick.
    */
  def getPassiveGen(stack: ItemStack): Double

  /**
    *
    * @param stack
    * @return Amount of power in crystal that is less than current storage.  Used for passive trickle charging.
    */
  def getStoragePartial(stack: ItemStack): Double

  /**
    *
    * @param stack
    * @return Maximum amount of power that can flow from this crystal.  This is meant to be per-tick, divided among children.
    */
  def getTransferRate(stack: ItemStack): Int

  /**
    *
    * @param stack
    * @return Size of the crystal.
    */
  def getType(stack: ItemStack): String

  def setColor(stack: ItemStack, color: Int): Unit

  def setTransferRate(stack: ItemStack, rate: Int): Unit

  def setType(stack: ItemStack, ctype: String): Unit

  def setPassiveGen(stack: ItemStack, passiveGen: Float): Unit

  def setName(stack: ItemStack, name: String): Unit

  def setStoragePartial(stack: ItemStack, amount: Double): Unit
}