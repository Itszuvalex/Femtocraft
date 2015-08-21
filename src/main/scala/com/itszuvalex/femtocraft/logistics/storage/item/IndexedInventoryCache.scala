package com.itszuvalex.femtocraft.logistics.storage.item

import com.itszuvalex.itszulib.implicits.IDImplicits._
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraftforge.oredict.OreDictionary

import scala.collection.mutable

/**
 * Created by Christopher on 8/21/2015.
 */

class IndexedInventoryCache(private val inventory: IInventory) extends IIndexedInventory {
  protected lazy val idMap   = mutable.HashMap[Int, mutable.HashSet[Int]]()
  protected lazy val oresMap = mutable.HashMap[Int, mutable.HashSet[Int]]()

  private var invalid = false

  rebuildCache()

  override def addItemStack(itemStack: ItemStack, slot: Int): Unit = {
    if (itemStack == null) return
    rebuildCacheIfNecessary()
    idMap.getOrElseUpdate(itemStack.itemID, new mutable.HashSet[Int]()) += slot
    OreDictionary.getOreIDs(itemStack).foreach(oresMap.getOrElseUpdate(_, new mutable.HashSet[Int]()) += slot)
  }

  override def removeItemStack(itemStack: ItemStack, slot: Int): Unit = {
    if (itemStack == null) return
    rebuildCacheIfNecessary()
    val id = itemStack.itemID
    idMap.get(id).map { set =>
      set -= slot
      if (set.isEmpty)
        idMap.remove(id)
                      }
    OreDictionary.getOreIDs(itemStack).foreach { oid => oresMap.get(oid).map { set =>
      set -= slot
      if (set.isEmpty)
        oresMap.remove(oid)
                                                                           }
                                               }
  }

  override def getSlotsByItemID(id: Int): Option[mutable.HashSet[Int]] = {
    rebuildCacheIfNecessary()
    idMap.get(id)
  }

  override def getSlotsByOreID(id: Int): Option[mutable.HashSet[Int]] = {
    rebuildCacheIfNecessary()
    oresMap.get(id)
  }

  override def getSlotsByItemStack(itemStack: ItemStack): Option[mutable.HashSet[Int]] = {
    getSlotsByItemID(itemStack.itemID)
  }

  override def getSlotsByOreName(name: String): Option[mutable.HashSet[Int]] = {
    getSlotsByOreID(OreDictionary.getOreID(name))
  }

  override def containsItemStack(itemStack: ItemStack) = {
    rebuildCacheIfNecessary()
    idMap.contains(itemStack.itemID)
  }

  override def containsOre(ore: String) = {
    rebuildCacheIfNecessary()
    oresMap.contains(OreDictionary.getOreID(ore))
  }


  override def invalidateCache() = {
    invalid = true
    idMap.clear()
    oresMap.clear()
  }

  override def rebuildCache() = {
    invalid = false
    (0 until inventory.getSizeInventory).map(int => (inventory.getStackInSlot(int), int)).foreach(pair => addItemStack(pair._1, pair._2))
  }

  override def rebuildCacheIfNecessary() = {
    if (invalid) rebuildCache()
  }

  override def isValid: Boolean = !invalid
}
