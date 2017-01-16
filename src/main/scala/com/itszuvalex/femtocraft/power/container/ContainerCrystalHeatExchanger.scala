package com.itszuvalex.femtocraft.power.container

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.power.tile.TileCrystalHeatExchanger
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.container.sync.{SyncDouble, SyncInt, SyncItemStorageItemStack}
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack

/**
  * Created by Christopher Harris (Itszuvalex) on 12/20/2015.
  */
class ContainerCrystalHeatExchanger(parPlayer: EntityPlayer, inv: InventoryPlayer, te: TileCrystalHeatExchanger, addSyncs: Boolean = true) extends ContainerInv[TileCrystalHeatExchanger](parPlayer, te, 0, 0, addSyncs) {
  addSync(new SyncDouble(() => te.battery.maxStorage, (max) => te.battery.maxStorage = max))
  addSync(new SyncDouble(() => te.battery.storage, (storage) => te.battery.storage = storage))
  addSync(new SyncInt(() => te.getBurnTime, (i: Int) => te.setBurnTime(i)))
  addSync(new SyncInt(() => te.getBurnMax, (i: Int) => te.setBurnMax(i)))

  if (addSyncs) {
    te.storage.indices.foreach(i => addSync(new SyncItemStorageItemStack(te.storage, i)))
    addPlayerInventorySlots(inv)
  }

  override def eligibleForInput(item: ItemStack): Boolean = {
    item != null && item.getItem != null && item.hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)
  }
}
