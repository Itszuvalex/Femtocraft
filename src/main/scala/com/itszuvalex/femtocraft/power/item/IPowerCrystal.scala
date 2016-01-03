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

trait IPowerCrystal extends Item {

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
    * @return Range to allow connections in.
    */
  def getRange(stack: ItemStack): Float

  /**
    *
    * @param stack
    * @return Amount of power to generate per tick.
    */
  def getPassiveGen(stack: ItemStack): Float

  /**
    *
    * @param stack
    * @return Maximum amount of power crystal can store.
    */
  def getStorageMax(stack: ItemStack): Long

  /**
    *
    * @param stack
    * @return Current amount of power crystal is storing.
    */
  def getStorageCurrent(stack : ItemStack): Long

  /**
    *
    * @param amount Amount of power to store.
    * @param doStore Pass true to actually consume resources.  False simulates the store.
    * @return Amount of @amount successfully stored.
    */
  def store(stack: ItemStack, amount: Long, doStore: Boolean): Long

  /**
    *
    * @param amount Amount of power to attempt to consume.
    * @param doConsume Pass true to actually consume resources.  False simulates the store.
    * @return Amount of @amount successfully removed.
    */
  def consume(stack: ItemStack, amount: Long, doConsume: Boolean): Long

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

}