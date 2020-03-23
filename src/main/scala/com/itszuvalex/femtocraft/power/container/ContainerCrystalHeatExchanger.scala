package com.itszuvalex.femtocraft.power.container

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.power.tile.TileCrystalHeatExchanger
import com.itszuvalex.itszulib.api.ItszuLibModules
import com.itszuvalex.itszulib.api.storage.IItemStorage
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.container.sync.{SyncDouble, SyncInt, SyncItemStorageItemStack}
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack

/**
  * Created by Christopher Harris (Itszuvalex) on 12/20/2015.
  */
class ContainerCrystalHeatExchanger(parPlayer: EntityPlayer, inv: InventoryPlayer, te: TileCrystalHeatExchanger, addSyncs: Boolean = true) extends ContainerInv[TileCrystalHeatExchanger](parPlayer, te, 0, 0, GuiIDs.TileCrystalHeatExchangerID, addSyncs) {
  addSync(new SyncDouble(GuiID, () => te.battery.maxStorage, (max) => te.battery.maxStorage = max))
  addSync(new SyncDouble(GuiID, () => te.battery.storage, (storage) => te.battery.storage = storage))
  addSync(new SyncInt(GuiID, () => te.internal.getBurnTime, (i: Int) => te.internal.setBurnTime(i)))
  addSync(new SyncInt(GuiID, () => te.internal.getBurnMax, (i: Int) => te.internal.setBurnMax(i)))

  if (addSyncs) {
    val storage: IItemStorage = te.getModule(ItszuLibModules.ITEM_STORAGE, null)
    storage.indices.foreach(i => addSync(new SyncItemStorageItemStack(GuiID, storage, i)))
    addPlayerInventorySlots(inv)
  }

  override def eligibleForInput(item: ItemStack): Boolean = {
    item != null && item.getItem != null && item.hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)
  }
}
