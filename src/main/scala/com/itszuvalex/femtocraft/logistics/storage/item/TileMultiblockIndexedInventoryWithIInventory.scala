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

  override def closeInventory(): Unit =
    if (isController) indInventory.closeInventory()
    else
      forwardToController[TileMultiblockIndexedInventoryWithIInventory, Unit](_.closeInventory())

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

  override def getStackInSlotOnClosing(slot: Int): ItemStack =
    if (isController) indInventory.getStackInSlotOnClosing(slot)
    else
      forwardToController[TileMultiblockIndexedInventoryWithIInventory, ItemStack](_.getStackInSlotOnClosing(slot))

  override def openInventory(): Unit =
    if (isController) indInventory.openInventory()
    else
      forwardToController[TileMultiblockIndexedInventoryWithIInventory, Unit](_.openInventory())

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

  override def hasCustomInventoryName: Boolean =
    if (isController) indInventory.hasCustomInventoryName
    else
      forwardToController[TileMultiblockIndexedInventoryWithIInventory, Boolean](_.hasCustomInventoryName())

  override def getInventoryName: String =
    if (isController) indInventory.getInventoryName
    else
      forwardToController[TileMultiblockIndexedInventoryWithIInventory, String](_.getInventoryName())
}
