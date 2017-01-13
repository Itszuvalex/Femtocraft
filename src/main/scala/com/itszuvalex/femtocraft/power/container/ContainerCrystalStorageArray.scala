package com.itszuvalex.femtocraft.power.container

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.power.tile.TileCrystalStorageArray
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.container.sync.{SyncDouble, SyncItemStorageItemStack}
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack

/**
  * Created by Christopher Harris (Itszuvalex) on 12/20/2015.
  */
class ContainerCrystalStorageArray(parPlayer: EntityPlayer, inv: InventoryPlayer, te: TileCrystalStorageArray, addSyncs: Boolean = true) extends ContainerInv[TileCrystalStorageArray](parPlayer, te, 0, 0, addSyncs) {
  addSync(new SyncDouble(() => te.battery.maxStorage, (max) => te.battery.maxStorage = max))
  addSync(new SyncDouble(() => te.battery.storage, (storage) => te.battery.storage = storage))

  if (addSyncs) {
    te.storage.indices.foreach(i => addSync(new SyncItemStorageItemStack(te.storage, i)))
    addPlayerInventorySlots(inv)
  }

  override def eligibleForInput(item: ItemStack): Boolean = {
    item != null && item.getItem != null && item.hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)
  }
}
