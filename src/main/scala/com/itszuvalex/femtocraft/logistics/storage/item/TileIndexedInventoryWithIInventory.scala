package com.itszuvalex.femtocraft.logistics.storage.item

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack

/**
  * Created by Christopher Harris (Itszuvalex) on 3/9/16.
  */
trait TileIndexedInventoryWithIInventory extends TileIndexedInventory with IInventory {
  override def closeInventory(): Unit = indInventory.closeInventory()

  override def decrStackSize(p_70298_1_ : Int, p_70298_2_ : Int): ItemStack = indInventory.decrStackSize(p_70298_1_, p_70298_2_)

  override def getInventoryName: String = indInventory.getInventoryName

  override def getInventoryStackLimit: Int = indInventory.getInventoryStackLimit

  override def getSizeInventory: Int = indInventory.getSizeInventory

  override def getStackInSlot(p_70301_1_ : Int): ItemStack = indInventory.getStackInSlot(p_70301_1_)

  override def getStackInSlotOnClosing(p_70304_1_ : Int): ItemStack = indInventory.getStackInSlotOnClosing(p_70304_1_)

  override def hasCustomInventoryName: Boolean = indInventory.hasCustomInventoryName

  override def isItemValidForSlot(p_94041_1_ : Int, p_94041_2_ : ItemStack): Boolean = indInventory.isItemValidForSlot(p_94041_1_, p_94041_2_)

  override def isUseableByPlayer(p_70300_1_ : EntityPlayer): Boolean = indInventory.isUseableByPlayer(p_70300_1_)

  override def openInventory(): Unit = indInventory.openInventory()

  override def setInventorySlotContents(p_70299_1_ : Int, p_70299_2_ : ItemStack): Unit = indInventory.setInventorySlotContents(p_70299_1_, p_70299_2_)
}
