package com.itszuvalex.femtocraft.logistics.storage.item

import net.minecraft.item.ItemStack

import scala.collection.mutable

/**
  * Created by Christopher on 8/21/2015.
  */
trait IIndexedInventory {

  /**
    * These methods are not checked for validity.  You are responsible for ensuring that the inventory slot is empty, or it will be overwritten.
    * If the slot contains an item that is 'equal' to itemStack, it will overwrite it.  It will not join them together.
    *
    * Why?
    *
    * Because the cache that goes alongside this code identifies itemstacks by their internal itemID only.  It does not care about damage values,
    * nbt values, or any other values.
    *
    * All values you get out of the IIndexedInventory are rough estimates and you will still have to perform manual validation that these itemstacks
    * are in fact the itemstacks you are looking for.  Perform Jedi Mindtricks at your own peril.
    *
    * As such, ultimately you are reponsible for assuring correct behavior inside the inventory as well.
    * Because I don't do much more than rough estimates, it is up to you to increment stack counts for the returned itemstacks correctly.
    * This is for when you are inserting a new itemstack in the inventory (i.e. you could or decided not to join itemstacks).
    * This updates the internal slot references and performs OreDictionary and ItemID lookups.
    *
    * @param itemStack Itemstack to set slot contents to.
    * @param slot Index of slot
    */
  def addItemStack(itemStack: ItemStack, slot: Int): Unit

  def removeItemStack(slot: Int): Unit

  def getSlotsByItemID(id: Int): Option[mutable.HashSet[Int]]

  def getSlotsByOreID(id: Int): Option[mutable.HashSet[Int]]

  /**
    * Returns all slot indexes in this inventory of items which share an itemID value with this itemStack.
    * No other comparison is performed, and the items could actually be different, such as damage values or nbt.
    *
    * It is up to you to perform the correct validation and decide which (if any) of the items in the return slots you want to use.
    *
    * Aka. this maps directly to getSlotsByItemID(itemStack.itemID)
    *
    * @param itemStack
    * @return
    */
  def getSlotsByItemStack(itemStack: ItemStack): Option[mutable.HashSet[Int]]

  /**
    * Returns all slot indexes that contain items which are registered in the OreDictionary to this specific name.
    *
    * This maps directly to getSlotsByOreID(OreDictionary.getOreID(name))
    *
    * @param name OreName
    * @return
    */
  def getSlotsByOreName(name: String): Option[mutable.HashSet[Int]]

  def containsItemStack(itemStack: ItemStack): Boolean

  def containsOre(ore: String): Boolean

  def getContainedOres: scala.collection.Set[String]

  def getContainedIDs: scala.collection.Set[Int]

  def isCacheValid: Boolean

  def invalidateCache()

  def rebuildCache()

  def rebuildCacheIfNecessary()
}
