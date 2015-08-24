package com.itszuvalex.femtocraft.logistics.storage.item

import net.minecraft.item.ItemStack

import scala.collection.mutable

/**
 * Created by Christopher on 8/21/2015.
 */
trait IIndexedInventory {

  def addItemStack(itemStack: ItemStack, slot: Int): Unit

  def removeItemStack(itemStack: ItemStack, slot: Int): Unit

  def getSlotsByItemID(id: Int): Option[mutable.HashSet[Int]]

  def getSlotsByOreID(id: Int): Option[mutable.HashSet[Int]]

  def getSlotsByItemStack(itemStack: ItemStack): Option[mutable.HashSet[Int]]

  def getSlotsByOreName(name: String): Option[mutable.HashSet[Int]]

  def containsItemStack(itemStack: ItemStack) : Boolean

  def containsOre(ore: String) : Boolean

  def isValid: Boolean

  def invalidateCache()

  def rebuildCache()

  def rebuildCacheIfNecessary()
}
