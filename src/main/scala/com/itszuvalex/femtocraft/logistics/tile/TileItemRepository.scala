package com.itszuvalex.femtocraft.logistics.tile

import com.itszuvalex.femtocraft.logistics.storage.item.{IIndexedInventory, IndexedInventory}
import com.itszuvalex.femtocraft.nanite.{NaniteManager, NaniteNode}
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.Saveable
import com.itszuvalex.itszulib.core.TileEntityBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack

import scala.collection.mutable

/**
  * Created by Christopher Harris (Itszuvalex) on 12/20/15.
  */
object TileItemRepository {
  val INVENTORY_SIZE         = 9 * 6
  val HIVE_CONNECTION_RADIUS = 32f
}

class TileItemRepository extends TileEntityBase with IIndexedInventory with IInventory with NaniteNode {
  @Saveable val indInventory: IndexedInventory = new IndexedInventory(TileItemRepository.INVENTORY_SIZE)

  override def getMod = Femtocraft

  override def hasGUI: Boolean = true

  override def getGuiID: Int = GuiIDs.TileItemRepositoryGuiID

  /**
    * @return ItemStack[] that backs this inventory class. Modifications to it modify this.
    */
  def getInventory: Array[ItemStack] = indInventory.getInventory

  override def getSizeInventory: Int = indInventory.getSizeInventory

  override def getStackInSlot(i: Int): ItemStack = indInventory.getStackInSlot(i)

  override def decrStackSize(i: Int, amount: Int): ItemStack = {
    setModified()
    indInventory.decrStackSize(i, amount)
  }

  override def getStackInSlotOnClosing(i: Int): ItemStack = indInventory.getStackInSlotOnClosing(i)

  override def setInventorySlotContents(i: Int, itemstack: ItemStack): Unit = {
    setModified()
    indInventory.setInventorySlotContents(i, itemstack)
  }

  override def addItemStack(itemStack: ItemStack, slot: Int): Unit = {
    setModified()
    indInventory.addItemStack(itemStack, slot)
  }

  override def removeItemStack(slot: Int): Unit = {
    setModified()
    indInventory.removeItemStack(slot)
  }


  override def invalidate(): Unit = {
    super.invalidate()
    if (!worldObj.isRemote) {
      NaniteManager.removeNode(this)
    }
  }

  override def validate(): Unit = {
    super.validate()
    if (!worldObj.isRemote) {
      NaniteManager.addNode(this)
    }
  }

  override def getInventoryName: String = indInventory.getInventoryName

  override def hasCustomInventoryName: Boolean = indInventory.hasCustomInventoryName

  override def getInventoryStackLimit: Int = indInventory.getInventoryStackLimit

  override def markDirty(): Unit = indInventory.markDirty()

  override def isUseableByPlayer(entityplayer: EntityPlayer): Boolean = indInventory.isUseableByPlayer(entityplayer)

  override def openInventory(): Unit = indInventory.openInventory()

  override def closeInventory(): Unit = indInventory.closeInventory()

  override def isItemValidForSlot(i: Int, itemstack: ItemStack): Boolean = indInventory.isItemValidForSlot(i, itemstack)

  override def invalidateCache(): Unit = indInventory.invalidateCache()

  override def getSlotsByOreID(id: Int): Option[mutable.HashSet[Int]] = indInventory.getSlotsByOreID(id)

  override def containsItemStack(itemStack: ItemStack): Boolean = indInventory.containsItemStack(itemStack)

  override def getSlotsByItemStack(itemStack: ItemStack): Option[mutable.HashSet[Int]] = indInventory.getSlotsByItemStack(itemStack)

  override def getSlotsByOreName(name: String): Option[mutable.HashSet[Int]] = indInventory.getSlotsByOreName(name)

  override def containsOre(ore: String): Boolean = indInventory.containsOre(ore)

  override def rebuildCache(): Unit = indInventory.rebuildCache()

  override def getSlotsByItemID(id: Int): Option[mutable.HashSet[Int]] = indInventory.getSlotsByItemID(id)

  override def isCacheValid: Boolean = indInventory.isCacheValid

  override def rebuildCacheIfNecessary(): Unit = indInventory.rebuildCacheIfNecessary()

  override def getContainedOres: collection.Set[String] = indInventory.getContainedOres

  override def getContainedIDs: collection.Set[Int] = indInventory.getContainedIDs

  override def hasDescription: Boolean = false

  override def hiveConnectionRadius: Float = TileItemRepository.HIVE_CONNECTION_RADIUS
}
