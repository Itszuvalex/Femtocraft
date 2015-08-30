package com.itszuvalex.femtocraft.logistics.storage.item

import com.itszuvalex.itszulib.api.core.Saveable
import com.itszuvalex.itszulib.core.TileEntityBase
import net.minecraft.item.ItemStack

import scala.collection.Set

/**
 * Created by Christopher on 8/29/2015.
 */
trait TileIndexedInventory extends TileEntityBase with IIndexedInventory {
  @Saveable val indInventory = defaultInventory

  def defaultInventory: IndexedInventory

  override def addItemStack(itemStack: ItemStack, slot: Int) = {
    indInventory.addItemStack(itemStack, slot)
    markDirty()
    setModified()
  }

  override def removeItemStack(itemStack: ItemStack, slot: Int) = {
    indInventory.removeItemStack(itemStack, slot)
    markDirty()
    setModified()
  }

  override def getSlotsByOreID(id: Int) = indInventory.getSlotsByOreID(id)

  override def containsItemStack(itemStack: ItemStack) = indInventory.containsItemStack(itemStack)

  override def containsOre(ore: String) = indInventory.containsOre(ore)

  override def getSlotsByOreName(name: String) = indInventory.getSlotsByOreName(name)

  override def getSlotsByItemStack(itemStack: ItemStack) = indInventory.getSlotsByItemStack(itemStack)

  override def rebuildCache() = indInventory.rebuildCache()

  override def getSlotsByItemID(id: Int) = indInventory.getSlotsByItemID(id)

  override def isValid = indInventory.isValid

  override def invalidateCache() = indInventory.invalidateCache()

  override def rebuildCacheIfNecessary(): Unit = indInventory.rebuildCacheIfNecessary()

  override def getContainedOres: Set[String] = indInventory.getContainedOres

  override def getContainedIDs: Set[Int] = indInventory.getContainedIDs
}
