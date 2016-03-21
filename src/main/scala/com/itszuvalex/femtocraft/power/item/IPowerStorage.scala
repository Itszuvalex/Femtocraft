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
  def getStorageMax(stack: ItemStack): Double

  /**
    *
    * @param stack
    * @return Current amount of power crystal is storing.
    */
  def getStorageCurrent(stack : ItemStack): Double

  /**
    *
    * @param amount Amount of power to store.
    * @param doStore Pass true to actually consume resources.  False simulates the store.
    * @return Amount of @amount successfully stored.
    */
  def store(stack: ItemStack, amount: Double, doStore: Boolean): Double

  /**
    *
    * @param amount Amount of power to attempt to consume.
    * @param doConsume Pass true to actually consume resources.  False simulates the store.
    * @return Amount of @amount successfully removed.
    */
  def consume(stack: ItemStack, amount: Double, doConsume: Boolean): Double

  def setStorageCurrent(stack: ItemStack, amount: Double): Unit

  def setStorageMax(stack: ItemStack, amount: Double): Unit

}
