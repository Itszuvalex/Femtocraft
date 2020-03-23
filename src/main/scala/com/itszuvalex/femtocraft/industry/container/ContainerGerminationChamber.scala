package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.industry.tile.TileGerminationChamber
import com.itszuvalex.itszulib.api.ItszuLibCapabilities
import com.itszuvalex.itszulib.api.storage.IFluidStorageModifiable
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.container.sync.{SyncDouble, SyncFluidStorageFluidStack, SyncInt, SyncItemStorageItemStack}
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.FurnaceRecipes

/**
  * Created by Alex on 18.08.2016.
  */
class ContainerGerminationChamber(player: EntityPlayer, inv: InventoryPlayer, tile: TileGerminationChamber, shouldSync: Boolean) extends ContainerInv[TileGerminationChamber](player, tile, 0, 1, GuiIDs.TileGerminationChamberID, shouldSync) {
  addSync(new SyncDouble(GuiID, () => tile.getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).battery.maxStorage, (a: Double) => tile.getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).battery.maxStorage = a))
  addSync(new SyncDouble(GuiID, () => tile.getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).battery.storage, (a: Double) => tile.getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null).battery.storage = a))
  addSync(new SyncDouble(GuiID, () => tile.getProgress, (a: Double) => tile.setProgress(a)))
  addSync(new SyncDouble(GuiID, () => tile.getBaseGoal, (a: Double) => tile.setBaseGoal(a)))
  addSync(new SyncInt(GuiID, () => tile.getTicksMax, (a: Int) => tile.setTicksMax(a)))
  tile.getCapability(ItszuLibCapabilities.FLUID_STORAGE, null).indices.foreach(i => addSync(new SyncFluidStorageFluidStack(GuiID, tile.getCapability(ItszuLibCapabilities.FLUID_STORAGE, null).asInstanceOf[IFluidStorageModifiable], i)))

  if (shouldSync) {
    addSync(new SyncItemStorageItemStack(GuiID, tile.storage, 0))
    addSync(new SyncItemStorageItemStack(GuiID, tile.storage, 1))
    addSync(new SyncItemStorageItemStack(GuiID, tile.storage, 2))
    addSync(new SyncItemStorageItemStack(GuiID, tile.storage, 3))

    addPlayerInventorySlots(inv)
  }


  override def eligibleForInput(item: ItemStack): Boolean = FurnaceRecipes.instance().getSmeltingResult(item) != null
}
