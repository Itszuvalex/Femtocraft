package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.api.nanite.INaniteTankOLD
import com.itszuvalex.femtocraft.industry.NaniteInfusionRecipeRegistry
import com.itszuvalex.femtocraft.industry.tile.TileNaniteInfuser
import com.itszuvalex.itszulib.api.wrappers.Converter
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.container.sync.{SyncDouble, SyncItemStorageItemStack}
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack

class ContainerNaniteInfuser(player: EntityPlayer, inv: InventoryPlayer, tile: TileNaniteInfuser, shouldSync: Boolean) extends ContainerInv[TileNaniteInfuser](player, tile, 0, 1, GuiIDs.TileNaniteInfuserID, shouldSync) {
  addSync(new SyncDouble(GuiID, () => tile.getModule(ManagerModules.TILE_WIRELESS_POWER_STORAGE_NODE, null).battery.storage, (a: Double) => tile.getModule(ManagerModules.TILE_WIRELESS_POWER_STORAGE_NODE, null).battery.storage = a))
  addSync(new SyncDouble(GuiID, () => tile.getModule(ManagerModules.TILE_WIRELESS_POWER_STORAGE_NODE, null).battery.maxStorage, (a: Double) => tile.getModule(ManagerModules.TILE_WIRELESS_POWER_STORAGE_NODE, null).battery.maxStorage = a))
  addSync(new SyncDouble(GuiID, () => tile.internal.getProgress, (a: Double) => tile.internal.setProgress(a)))
  addSync(new SyncINaniteTank(GuiID, () => tile.naniteTank.copy(), (a: INaniteTankOLD) => tile.naniteTank.deserializeNBT(a.serializeNBT())))

  if (shouldSync) {
    addSync(new SyncItemStorageItemStack(GuiID, tile.storage, 0))
    addSync(new SyncItemStorageItemStack(GuiID, tile.storage, 1))

    addPlayerInventorySlots(inv, 5, 76)
  }

  override def eligibleForInput(item: ItemStack): Boolean = NaniteInfusionRecipeRegistry.getMatchingRecipe(Converter.IItemStackFromItemStack(item)).isDefined
}
