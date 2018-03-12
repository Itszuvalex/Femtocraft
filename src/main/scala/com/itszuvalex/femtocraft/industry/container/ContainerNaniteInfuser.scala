package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.nanite.NaniteTank
import com.itszuvalex.femtocraft.industry.NaniteInfusionRecipeRegistry
import com.itszuvalex.femtocraft.industry.tile.TileNaniteInfuser
import com.itszuvalex.itszulib.api.wrappers.Converter
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.container.sync.{SyncDouble, SyncItemStorageItemStack}
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack

class ContainerNaniteInfuser(player: EntityPlayer, inv: InventoryPlayer, tile: TileNaniteInfuser, shouldSync: Boolean) extends ContainerInv[TileNaniteInfuser](player, tile, 0, 1, GuiIDs.TileNaniteInfuserID, shouldSync) {
  addSync(new SyncDouble(GuiID, () => tile.getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).battery.storage, (a: Double) => tile.getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).battery.storage = a))
  addSync(new SyncDouble(GuiID, () => tile.getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).battery.maxStorage, (a: Double) => tile.getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).battery.maxStorage = a))
  addSync(new SyncDouble(GuiID, () => tile.getProgress, (a: Double) => tile.setProgress(a)))
  addSync(new SyncNaniteTank(GuiID, () => tile.naniteStorageTank.copy(), (a: NaniteTank) => tile.naniteStorageTank.deserializeNBT(a.serializeNBT())))

  if (shouldSync) {
    addSync(new SyncItemStorageItemStack(GuiID, tile.storage, 0))
    addSync(new SyncItemStorageItemStack(GuiID, tile.storage, 1))
    //    addSlotToContainer(new FilteredSlot(tile, 0, 45, 24))

    addPlayerInventorySlots(inv, 5, 76)
  }

  override def eligibleForInput(item: ItemStack): Boolean = NaniteInfusionRecipeRegistry.getMatchingRecipe(Converter.IItemStackFromItemStack(item)).isDefined
}
