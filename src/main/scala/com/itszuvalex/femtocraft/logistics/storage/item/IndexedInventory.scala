/*
 * ******************************************************************************
 *  * Copyright (C) 2013  Christopher Harris (Itszuvalex)
 *  * Itszuvalex@gmail.com
 *  *
 *  * This program is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU General Public License
 *  * as published by the Free Software Foundation; either version 2
 *  * of the License, or (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *  *****************************************************************************
 */
package com.itszuvalex.femtocraft.logistics.storage.item

import java.util

import com.itszuvalex.itszulib.api.core.{NBTSerializable, Saveable}
import com.itszuvalex.itszulib.util.DataUtils
import com.itszuvalex.itszulib.util.DataUtils.EnumSaveType
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.{Container, IInventory}
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

import scala.collection.Set

/**
  *
  * @param size Utility class for storing and saving/loading ItemStack[]s with ease.
  */
class IndexedInventory(size: Int) extends IInventory with IIndexedInventory with NBTSerializable {
  private lazy      val inventoryCache = new IndexedInventoryCache(this)
  @Saveable private var inventory      = new Array[ItemStack](size)

  def this() = this(0)

  /**
    * @return ItemStack[] that backs this inventory class. Modifications to it modify this.
    */
  def getInventory: Array[ItemStack] = inventory

  override def getSizeInventory = inventory.length

  override def getStackInSlot(i: Int) = inventory(i)

  override def decrStackSize(i: Int, amount: Int): ItemStack = {
    var itemstack: ItemStack = null
    if (inventory(i) != null) {
      if (inventory(i).stackSize <= amount) {
        itemstack = inventory(i)
        removeItemStack(i)
      } else {
        itemstack = inventory(i).splitStack(amount)
        if (inventory(i).stackSize == 0) {
          removeItemStack(i)
        }
      }
    }
    itemstack
  }

  override def getStackInSlotOnClosing(i: Int) = inventory(i)

  override def setInventorySlotContents(i: Int, itemstack: ItemStack) {
    addItemStack(itemstack, i)
  }

  override def addItemStack(itemStack: ItemStack, slot: Int): Unit = {
    if(inventory(slot) != null)
      removeItemStack(slot)

    inventory(slot) = itemStack
    inventoryCache.addItemStack(itemStack, slot)
  }

  override def removeItemStack(slot: Int): Unit = {
    inventory(slot) = null
    inventoryCache.removeItemStack(slot)
  }

  override def getInventoryName = "femto.IndexedInventory"

  override def hasCustomInventoryName = false

  override def getInventoryStackLimit = 64

  override def markDirty() {
  }

  override def isUseableByPlayer(entityplayer: EntityPlayer) = true

  override def openInventory() {
  }

  override def closeInventory() {
  }

  override def isItemValidForSlot(i: Int, itemstack: ItemStack): Boolean = true

  override def saveToNBT(compound: NBTTagCompound) = DataUtils
                                                     .saveObjectToNBT(compound, this, EnumSaveType.WORLD)

  override def loadFromNBT(compound: NBTTagCompound) = {
    DataUtils.loadObjectFromNBT(compound, this, EnumSaveType.WORLD)
    invalidateCache()
  }

  /**
    * Changes size of the inventory to be equal to size.  Keeps current inventory from slots 0 -> (size-1), and will
    * drop extra itemstacks.
    *
    * @param size new size of inventory
    */
  def setInventorySize(size: Int) = {
    inventory = util.Arrays.copyOfRange(inventory, 0, size)
    invalidateCache()
  }

  override def invalidateCache() = inventoryCache.invalidateCache()

  def getComparatorInputOverride = Container.calcRedstoneFromInventory(this)

  override def getSlotsByOreID(id: Int) = inventoryCache.getSlotsByOreID(id)

  override def containsItemStack(itemStack: ItemStack) = inventoryCache.containsItemStack(itemStack)

  override def getSlotsByItemStack(itemStack: ItemStack) = inventoryCache.getSlotsByItemStack(itemStack)

  override def getSlotsByOreName(name: String) = inventoryCache.getSlotsByOreName(name)

  override def containsOre(ore: String) = inventoryCache.containsOre(ore)

  override def rebuildCache() = inventoryCache.rebuildCache()

  override def getSlotsByItemID(id: Int) = inventoryCache.getSlotsByItemID(id)

  override def isCacheValid = inventoryCache.isCacheValid

  override def rebuildCacheIfNecessary() = inventoryCache.rebuildCacheIfNecessary()

  override def getContainedOres: Set[String] = inventoryCache.getContainedOres

  override def getContainedIDs: Set[Int] = inventoryCache.getContainedIDs
}
