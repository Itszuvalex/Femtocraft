package com.itszuvalex.femtocraft.logistics.storage.item

import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.MultiBlockComponent
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack

/**
  * Created by Christopher on 8/29/2015.
  */
trait TileMultiblockIndexedInventoryWithIInventory extends TileEntityBase with IInventory {
  self: MultiBlockComponent with TileMultiblockIndexedInventory =>

  override def closeInventory(player: EntityPlayer): Unit =
    if (isController) indInventory.closeInventory(player)
    else
      forwardToController[TileMultiblockIndexedInventoryWithIInventory, Unit](_.closeInventory(player))

  override def decrStackSize(slot: Int, amount: Int): ItemStack =
    if (isController) indInventory.decrStackSize(slot, amount)
    else
      forwardToController[TileMultiblockIndexedInventoryWithIInventory, ItemStack](_.decrStackSize(slot, amount))

  override def getSizeInventory: Int =
    if (isController) indInventory.getSizeInventory
    else
      forwardToController[TileMultiblockIndexedInventoryWithIInventory, Int](_.getSizeInventory())

  override def getInventoryStackLimit: Int =
    if (isController) indInventory.getInventoryStackLimit
    else
      forwardToController[TileMultiblockIndexedInventoryWithIInventory, Int](_.getInventoryStackLimit())

  override def isItemValidForSlot(slot: Int, item: ItemStack): Boolean =
    if (isController) indInventory.isItemValidForSlot(slot, item)
    else
      forwardToController[TileMultiblockIndexedInventoryWithIInventory, Boolean](_.isItemValidForSlot(slot, item))

  override def openInventory(player: EntityPlayer): Unit =
    if (isController) indInventory.openInventory(player)
    else
      forwardToController[TileMultiblockIndexedInventoryWithIInventory, Unit](_.openInventory(player))

  override def setInventorySlotContents(slot: Int, item: ItemStack): Unit =
    if (isController) indInventory.setInventorySlotContents(slot, item)
    else
      forwardToController[TileMultiblockIndexedInventoryWithIInventory, Unit](_.setInventorySlotContents(slot, item))

  override def markDirty(): Unit =
    if (isController) indInventory.markDirty()
    else
      forwardToController[TileMultiblockIndexedInventoryWithIInventory, Unit](_.markDirty())

  override def isUseableByPlayer(player: EntityPlayer): Boolean =
    if (isController) indInventory.isUseableByPlayer(player)
    else
      forwardToController[TileMultiblockIndexedInventoryWithIInventory, Boolean](_.isUseableByPlayer(player))

  override def getStackInSlot(slot: Int): ItemStack =
    if (isController) indInventory.getStackInSlot(slot)
    else
      forwardToController[TileMultiblockIndexedInventoryWithIInventory, ItemStack](_.getStackInSlot(slot))

  override def clear(): Unit =
    if (isController) indInventory.clear()
    else
      forwardToController[TileMultiblockIndexedInventoryWithIInventory](_.clear())

  override def getFieldCount: Int =
    if (isController) indInventory.getFieldCount
    else
      forwardToController[TileMultiblockIndexedInventoryWithIInventory, Int](_.getFieldCount)

  override def getField(id: Int): Int =
    if (isController) indInventory.getField(id)
    else
      forwardToController[TileMultiblockIndexedInventoryWithIInventory, Int](_.getField(id))

  override def removeStackFromSlot(index: Int): ItemStack =
    if (isController) indInventory.removeStackFromSlot(index)
    else
      forwardToController[TileMultiblockIndexedInventoryWithIInventory, ItemStack](_.removeStackFromSlot(index))

  override def setField(id: Int, value: Int): Unit =
    if (isController) indInventory.setField(id, value)
    else
      forwardToController[TileMultiblockIndexedInventoryWithIInventory](_.setField(id, value))

  override def getName: String =
    if (isController) indInventory.getName
    else
      forwardToController[TileMultiblockIndexedInventoryWithIInventory, String](_.getName)

  override def hasCustomName: Boolean =
    if (isController) indInventory.hasCustomName
    else
      forwardToController[TileMultiblockIndexedInventoryWithIInventory, Boolean](_.hasCustomName)

  override def hasDescription: Boolean =
    if (isController) hasDescription
    else
      forwardToController[TileMultiblockIndexedInventoryWithIInventory, Boolean](_.hasDescription)
}
