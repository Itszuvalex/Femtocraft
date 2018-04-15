//package com.itszuvalex.femtocraft.logistics.storage.item
//
//import net.minecraft.entity.player.EntityPlayer
//import net.minecraft.inventory.IInventory
//import net.minecraft.item.ItemStack
//
///**
//  * Created by Christopher Harris (Itszuvalex) on 3/9/16.
//  */
//trait TileIndexedInventoryWithIInventory extends TileIndexedInventory with IInventory {
//  override def closeInventory(player: EntityPlayer): Unit = indInventory.closeInventory(player)
//
//  override def decrStackSize(p_70298_1_ : Int, p_70298_2_ : Int): ItemStack = indInventory.decrStackSize(p_70298_1_, p_70298_2_)
//
//  override def getName: String = indInventory.getName
//
//  override def getInventoryStackLimit: Int = indInventory.getInventoryStackLimit
//
//  override def getSizeInventory: Int = indInventory.getSizeInventory
//
//  override def getStackInSlot(p_70301_1_ : Int): ItemStack = indInventory.getStackInSlot(p_70301_1_)
//
//  override def hasCustomName: Boolean = indInventory.hasCustomName
//
//  override def isItemValidForSlot(p_94041_1_ : Int, p_94041_2_ : ItemStack): Boolean = indInventory.isItemValidForSlot(p_94041_1_, p_94041_2_)
//
//  override def isUsableByPlayer(p_70300_1_ : EntityPlayer): Boolean = indInventory.isUsableByPlayer(p_70300_1_)
//
//  override def openInventory(player: EntityPlayer): Unit = indInventory.openInventory(player)
//
//  override def setInventorySlotContents(p_70299_1_ : Int, p_70299_2_ : ItemStack): Unit = indInventory.setInventorySlotContents(p_70299_1_, p_70299_2_)
//
//  override def clear(): Unit = indInventory.clear()
//
//  override def getFieldCount: Int = indInventory.getFieldCount
//
//  override def getField(id: Int): Int = indInventory.getField(id)
//
//  override def removeStackFromSlot(index: Int): ItemStack = indInventory.removeStackFromSlot(index)
//
//  override def setField(id: Int, value: Int): Unit = indInventory.setField(id, value)
//}
