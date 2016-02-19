package com.itszuvalex.femtocraft.power.item

import net.minecraft.item.{ItemStack, Item}

/**
  * Created by Christopher Harris (Itszuvalex) on 2/18/2016.
  */
trait IPowerStorage extends Item {

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

  def setStorageCurrent(stack: ItemStack, amount: Long): Unit

  def setStorageMax(stack: ItemStack, amount: Long): Unit

}
