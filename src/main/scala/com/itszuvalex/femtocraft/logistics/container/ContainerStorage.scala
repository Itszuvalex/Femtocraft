package com.itszuvalex.femtocraft.logistics.container

import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStoragePlayerInventory}
import com.itszuvalex.itszulib.container.{ContainerBase, IItemStorageSyncBundle}
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack

abstract class ContainerStorage(parPlayer: EntityPlayer, storage: IItemStorage, input: Int, output: Int, gui: Int, registerSyncs: Boolean) extends ContainerBase(gui, registerSyncs) {
  protected final val player      : EntityPlayer = parPlayer
  protected final val INPUT_SLOT  : Int          = input
  protected final val OUTPUT_SLOT : Int          = output
  protected final val INV_SIZE    : Int          = storage.size
  protected final val INV_START   : Int          = INV_SIZE + 1
  protected final val INV_END     : Int          = INV_START + 26
  protected final val HOTBAR_START: Int          = INV_END + 1
  protected final val HOTBAR_END  : Int          = HOTBAR_START + 8

  def canInteractWith(entityplayer: EntityPlayer) = true

  /**
    * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
    */
  override def transferStackInSlot(par1EntityPlayer: EntityPlayer, par2: Int): ItemStack = {
    var itemstack: ItemStack = null
    val slot = this.inventorySlots.get(par2)
    if (slot != null && slot.getHasStack) {
      val itemstack1 = slot.getStack
      itemstack = itemstack1.copy
      if (par2 < INV_START) {
        if (!this.mergeItemStack(itemstack1, INV_START, HOTBAR_END + 1, false)) {
          return null
        }
        slot.onSlotChange(itemstack1, itemstack)
      }
      else {
        if (eligibleForInput(itemstack1)) {
          if (!this.mergeItemStack(itemstack1, INPUT_SLOT, INPUT_SLOT + 1, false)) {
            return null
          }
        }
        else if (par2 >= INV_START && par2 <= INV_END) {
          if (!this.mergeItemStack(itemstack1, HOTBAR_START, HOTBAR_END + 1, false)) {
            return null
          }
        }
        else if (par2 >= HOTBAR_START && par2 <= HOTBAR_END) {
          if (!this.mergeItemStack(itemstack1, INV_START, INV_END + 1, false)) {
            return null
          }
        }
      }
      if (itemstack1.getCount == 0) {
        slot.putStack(null)
      }
      else {
        slot.onSlotChanged()
      }
      if (itemstack1.getCount == itemstack.getCount) {
        return null
      }
      slot.onTake(par1EntityPlayer, itemstack1)
    }
    itemstack
  }

  def eligibleForInput(item: ItemStack): Boolean

  protected def addPlayerInventorySlots(inventoryPlayer: InventoryPlayer) {
    addPlayerInventorySlots(inventoryPlayer, 8, 84)
  }

  protected def addPlayerInventorySlots(inventoryPlayer: InventoryPlayer, inventoryXStart: Int, inventoryYStart: Int) {
    new IItemStorageSyncBundle(GuiID, this, new ItemStoragePlayerInventory(inventoryPlayer), true)
  }
}
