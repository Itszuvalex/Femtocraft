package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.cyber.CybermaterialRegistry
import com.itszuvalex.femtocraft.nanite.tile.TileNaniteExtractor
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.container.sync.{SyncDouble, SyncItemStorageItemStack}
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack

/**
  * Created by Alex on 18.08.2016.
  */
class ContainerNaniteExtractor(player: EntityPlayer, inv: InventoryPlayer, tile: TileNaniteExtractor, shouldSync: Boolean) extends ContainerInv[TileNaniteExtractor](player, tile, 0, 0, GuiIDs.TileNaniteExtractorID, shouldSync) {
  addSync(new SyncDouble(GuiID, () => tile.getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).battery.storage, (a: Double) => tile.getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).battery.storage = a))
  addSync(new SyncDouble(GuiID, () => tile.getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).battery.maxStorage, (a: Double) => tile.getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).battery.maxStorage = a))
  addSync(new SyncDouble(GuiID, () => tile.getProgress, (a: Double) => tile.setProgress(a)))

  if (shouldSync) {
    addSync(new SyncItemStorageItemStack(GuiID, tile.storage, 0))
    //    addSlotToContainer(new FilteredSlot(tile, 0, 45, 24))

    addPlayerInventorySlots(inv, 5, 76)
  }

  override def eligibleForInput(item: ItemStack): Boolean = CybermaterialRegistry.getNaniteFromItem(item.getItem, item.getItemDamage).isDefined
}
