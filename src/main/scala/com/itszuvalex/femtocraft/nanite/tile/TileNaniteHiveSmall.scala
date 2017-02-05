package com.itszuvalex.femtocraft.nanite.tile

import com.itszuvalex.femtocraft.api.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.logistics.storage.item.{IndexedInventory, TileIndexedInventory}
import com.itszuvalex.femtocraft.power.node.PowerLeafNode
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.Configurable
import com.itszuvalex.itszulib.api.wrappers.{IBattery, PowerBattery}
import com.itszuvalex.itszulib.core.TileEntityBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

/**
  * Created by Christopher Harris (Itszuvalex) on 8/25/15.
  */
@Configurable object TileNaniteHiveSmall {
  @Configurable val HIVE_RADIUS    = 20f
                val INVENTORY_SIZE = 30
}

@Configurable class TileNaniteHiveSmall extends TileEntityBase with TileIndexedInventory with PowerLeafNode with IInventory {

  override def defaultBattery: IBattery = new PowerBattery(5000)

  override def storageType: PowerStorageNodeType = PowerStorageNodeType.STORAGE

  override def leafTransferRate: Double = 50d

  override def connectionRadius: Float = 8f

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    setRenderUpdate()
  }

  override def getMod: AnyRef = Femtocraft

  override def getGuiID: Int = GuiIDs.TileNaniteHiveGuiID

  override def hasGUI: Boolean = true


  override def hasDescription = true

  override def defaultInventory: IndexedInventory = new IndexedInventory(TileNaniteHiveSmall.INVENTORY_SIZE)

  override def closeInventory(player: EntityPlayer): Unit = indInventory.closeInventory(player)

  override def decrStackSize(p_70298_1_ : Int, p_70298_2_ : Int): ItemStack = {
    markDirty()
    indInventory.decrStackSize(p_70298_1_, p_70298_2_)
  }

  override def getSizeInventory: Int = indInventory.getSizeInventory

  override def getInventoryStackLimit: Int = indInventory.getInventoryStackLimit

  override def isItemValidForSlot(p_94041_1_ : Int, p_94041_2_ : ItemStack): Boolean = indInventory.isItemValidForSlot(p_94041_1_, p_94041_2_)


  override def openInventory(player: EntityPlayer): Unit = indInventory.openInventory(player)

  override def setInventorySlotContents(p_70299_1_ : Int, p_70299_2_ : ItemStack): Unit = {
    markDirty()
    indInventory.setInventorySlotContents(p_70299_1_, p_70299_2_)
  }

  override def isUsableByPlayer(p_70300_1_ : EntityPlayer): Boolean = indInventory.isUsableByPlayer(p_70300_1_)

  override def getStackInSlot(p_70301_1_ : Int): ItemStack = indInventory.getStackInSlot(p_70301_1_)

  override def clear(): Unit = indInventory.clear()

  override def getFieldCount: Int = indInventory.getFieldCount

  override def getField(id: Int): Int = indInventory.getField(id)

  override def removeStackFromSlot(index: Int): ItemStack = indInventory.removeStackFromSlot(index)

  override def setField(id: Int, value: Int): Unit = indInventory.setField(id, value)

  override def getName: String = indInventory.getName

  override def hasCustomName: Boolean = indInventory.hasCustomName

  override def isEmpty: Boolean = indInventory.isEmpty
}
