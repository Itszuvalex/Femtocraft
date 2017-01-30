package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.industry.DustRecipeRegistry
import com.itszuvalex.femtocraft.industry.tile.TileDemolisher
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.container.sync.{SyncDouble, SyncItemStorageItemStack}
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack

/**
  * Created by Alex on 18.08.2016.
  */
class ContainerDemolisher(player: EntityPlayer, inv: InventoryPlayer, tile: TileDemolisher, shouldSync: Boolean) extends ContainerInv[TileDemolisher](player, tile, 0, 1, GuiIDs.TileDemolisherGuiID, shouldSync) {
  addSync(new SyncDouble(GuiID, () => tile.getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).battery.maxStorage, (a: Double) => tile.getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).battery.maxStorage = a))
  addSync(new SyncDouble(GuiID, () => tile.getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).battery.storage, (a: Double) => tile.getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).battery.storage = a))
  addSync(new SyncDouble(GuiID, () => tile.getProgress, (a: Double) => tile.setProgress(a)))

  if (shouldSync) {
    addSync(new SyncItemStorageItemStack(GuiID, tile.storage, 0))
    addSync(new SyncItemStorageItemStack(GuiID, tile.storage, 1))

    addPlayerInventorySlots(inv, 5, 76)
  }

  override def eligibleForInput(item: ItemStack): Boolean = DustRecipeRegistry.getDust(item).isDefined
}
