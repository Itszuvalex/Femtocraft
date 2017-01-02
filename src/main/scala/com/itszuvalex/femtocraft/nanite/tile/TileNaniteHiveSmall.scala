package com.itszuvalex.femtocraft.nanite.tile

import com.itszuvalex.femtocraft.api.power.{PowerConnectionNodeType, PowerStorageNodeType}
import com.itszuvalex.femtocraft.logistics.storage.item.{IndexedInventory, TileIndexedInventory}
import com.itszuvalex.femtocraft.power.PowerManager
import com.itszuvalex.femtocraft.power.node.PowerNode
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.Configurable
import com.itszuvalex.itszulib.api.wrappers.{IBattery, PowerBattery}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.TileDescriptionPacket
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

@Configurable class TileNaniteHiveSmall extends TileEntityBase with TileIndexedInventory with PowerNode with TileDescriptionPacket with IInventory {

  override def defaultBattery: IBattery = new PowerBattery(5000)

  override def powerStorageType: PowerStorageNodeType = PowerStorageNodeType.STORAGE

  override def powerConnectionType: PowerConnectionNodeType = PowerConnectionNodeType.LEAF

  override def powerRadius: Float = 8f

  override def powerTransfer: Double = 50d

  override def rendersPower: Boolean = false

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

  /* Tile Entity */
  override def validate(): Unit = {
    super.validate()
    if (!worldObj.isRemote) {
      PowerManager.addNode(powerDelegate)
    }
  }

  override def invalidate(): Unit = {
    super.invalidate()
    if (!worldObj.isRemote) {
      PowerManager.removeNode(powerDelegate)
    }
  }

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

  override def isUseableByPlayer(p_70300_1_ : EntityPlayer): Boolean = indInventory.isUseableByPlayer(p_70300_1_)

  override def getStackInSlot(p_70301_1_ : Int): ItemStack = indInventory.getStackInSlot(p_70301_1_)

  override def clear(): Unit = indInventory.clear()

  override def getFieldCount: Int = indInventory.getFieldCount

  override def getField(id: Int): Int = indInventory.getField(id)

  override def removeStackFromSlot(index: Int): ItemStack = indInventory.removeStackFromSlot(index)

  override def setField(id: Int, value: Int): Unit = indInventory.setField(id, value)

  override def getName: String = indInventory.getName

  override def hasCustomName: Boolean = indInventory.hasCustomName

  override def func_191420_l(): Boolean = false
}
