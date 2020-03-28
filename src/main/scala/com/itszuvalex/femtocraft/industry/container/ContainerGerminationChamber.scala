package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.industry.tile.TileGerminationChamber
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.container.sync.{SyncDouble, SyncFluidStorageFluidStack, SyncInt, SyncItemStorageItemStack}
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.FurnaceRecipes

/**
  * Created by Alex on 18.08.2016.
  */
class ContainerGerminationChamber(player: EntityPlayer, inv: InventoryPlayer, tile: TileGerminationChamber, shouldSync: Boolean) extends ContainerInv[TileGerminationChamber](player, tile, 0, 1, GuiIDs.TileGerminationChamberID, shouldSync) {
  addSync(new SyncDouble(GuiID, () => tile.multiblockBatteryModule.battery.maxStorage, (a: Double) => tile.multiblockBatteryModule.battery.maxStorage = a))
  addSync(new SyncDouble(GuiID, () => tile.multiblockBatteryModule.battery.storage, (a: Double) => tile.multiblockBatteryModule.battery.storage = a))
  addSync(new SyncDouble(GuiID, () => tile.state.get.map(_.getProgress).getOrElse(0d), (a: Double) => tile.state.get.foreach(_.setProgress(a))))
  addSync(new SyncDouble(GuiID, () => tile.state.get.map(_.getBaseGoal).getOrElse(0d), (a: Double) => tile.state.get.foreach(_.setBaseGoal(a))))
  addSync(new SyncInt(GuiID, () => tile.state.get.map(_.getTicksMax).getOrElse(0), (a: Int) => tile.state.get.foreach(_.setTicksMax(a))))
  tile.multiblockFluidModule.storage.indices.foreach(i => addSync(new SyncFluidStorageFluidStack(GuiID, tile.multiblockFluidModule.storage, i)))

  if (shouldSync) {
    addSync(new SyncItemStorageItemStack(GuiID, tile.multiblockStorageModule.storage, 0))
    addSync(new SyncItemStorageItemStack(GuiID, tile.multiblockStorageModule.storage, 1))
    addSync(new SyncItemStorageItemStack(GuiID, tile.multiblockStorageModule.storage, 2))
    addSync(new SyncItemStorageItemStack(GuiID, tile.multiblockStorageModule.storage, 3))

    addPlayerInventorySlots(inv)
  }


  override def eligibleForInput(item: ItemStack): Boolean = FurnaceRecipes.instance().getSmeltingResult(item) != null
}
