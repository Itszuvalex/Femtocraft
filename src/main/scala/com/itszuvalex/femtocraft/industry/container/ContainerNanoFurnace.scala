package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.industry.tile.TileNanoFurnace
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.container.sync.{SyncDouble, SyncItemStorageItemStack}
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.FurnaceRecipes

/**
  * Created by Alex on 18.08.2016.
  */
class ContainerNanoFurnace(player: EntityPlayer, inv: InventoryPlayer, tile: TileNanoFurnace, shouldSync: Boolean) extends ContainerInv[TileNanoFurnace](player, tile, 0, 1, GuiIDs.TileFurnaceGuiID, shouldSync) {
  addSync(new SyncDouble(GuiID, () => tile.getModule(ManagerModules.TILE_WIRELESS_POWER_STORAGE_NODE, null).battery.maxStorage, (a: Double) => tile.getModule(ManagerModules.TILE_WIRELESS_POWER_STORAGE_NODE, null).battery.maxStorage = a))
  addSync(new SyncDouble(GuiID, () => tile.getModule(ManagerModules.TILE_WIRELESS_POWER_STORAGE_NODE, null).battery.storage, (a: Double) => tile.getModule(ManagerModules.TILE_WIRELESS_POWER_STORAGE_NODE, null).battery.storage = a))
  addSync(new SyncDouble(GuiID, () => tile.internal.getProgress, (a: Double) => tile.internal.setProgress(a)))

  if (shouldSync) {
    addSync(new SyncItemStorageItemStack(GuiID, tile.storage, 0))
    addSync(new SyncItemStorageItemStack(GuiID, tile.storage, 1))

    addPlayerInventorySlots(inv, 5, 76)
  }


  override def eligibleForInput(item: ItemStack): Boolean = FurnaceRecipes.instance().getSmeltingResult(item) != null
}
