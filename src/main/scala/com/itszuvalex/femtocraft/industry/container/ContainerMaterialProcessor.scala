package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.industry.tile.TileMaterialProcessor
import com.itszuvalex.itszulib.api.wrappers.Converter
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.container.sync.{SyncDouble, SyncItemStorageItemStack}
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack


object ContainerMaterialProcessor {
  val POWER_BIG_INDEX   = 0
  val POWER_SMALL_INDEX = 1
}

class ContainerMaterialProcessor(player: EntityPlayer, inv: InventoryPlayer, tile: TileMaterialProcessor, shouldSync: Boolean) extends ContainerInv[TileMaterialProcessor](player, tile, 0, 0, shouldSync) {

  if(shouldSync) {
    /*
    //Input
    addSlotToContainer(new FilteredSlot(tile, 0, 35, 8))
    addSlotToContainer(new FilteredSlot(tile, 1, 53, 8))
    addSlotToContainer(new FilteredSlot(tile, 2, 35, 26))
    addSlotToContainer(new FilteredSlot(tile, 3, 53, 26))
    //Assemblies
    addSlotToContainer(new FilteredSlot(tile, 4, 152, 45))
    addSlotToContainer(new FilteredSlot(tile, 5, 152, 63))
    //Output
    addSlotToContainer(new OutputSlot(tile, 6, 35, 45))
    addSlotToContainer(new OutputSlot(tile, 7, 53, 45))
    addSlotToContainer(new OutputSlot(tile, 8, 35, 63))
    addSlotToContainer(new OutputSlot(tile, 9, 53, 63))
    //Power
    addSlotToContainer(new FilteredSlot(tile, 10, 10, 64))
    //Nanite
    addSlotToContainer(new FilteredSlot(tile, 11, 152, 8))
    */

    val storage = Converter.IItemStorageFromIInventory(tile.indInventory)
    addSync(new SyncItemStorageItemStack(storage, 0))
    addSync(new SyncItemStorageItemStack(storage, 1))
    addSync(new SyncItemStorageItemStack(storage, 2))
    addSync(new SyncItemStorageItemStack(storage, 3))
    addSync(new SyncItemStorageItemStack(storage, 4))
    addSync(new SyncItemStorageItemStack(storage, 5))
    addSync(new SyncItemStorageItemStack(storage, 6))
    addSync(new SyncItemStorageItemStack(storage, 7))
    addSync(new SyncItemStorageItemStack(storage, 8))
    addSync(new SyncItemStorageItemStack(storage, 9))
    addSync(new SyncItemStorageItemStack(storage, 10))
    addSync(new SyncItemStorageItemStack(storage, 11))

    addPlayerInventorySlots(inv)
  }

  addSync(new SyncDouble(() => inventory.getPowerCurrent, (a: Double) => inventory.setPowerCurrent(a)))

  override def eligibleForInput(item: ItemStack): Boolean = false
}
