package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.industry.tile.TileCrystalFurnace
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.container.sync.{SyncDouble, SyncItemStorageItemStack}
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.FurnaceRecipes

/**
 * Created by Alex on 18.08.2016.
 */
class ContainerCrystalFurnace(player: EntityPlayer, inv: InventoryPlayer, tile: TileCrystalFurnace, shouldSync: Boolean) extends ContainerInv[TileCrystalFurnace](player, tile, 0, 1, GuiIDs.TileCrystalFurnaceID, shouldSync) {
  addSync(new SyncDouble(GuiID, () => tile.internal.getProgress, (a: Double) => tile.internal.setProgress(a)))

  if (shouldSync) {
    addSync(new SyncItemStorageItemStack(GuiID, tile.storage, 0))
    addSync(new SyncItemStorageItemStack(GuiID, tile.storage, 1))
    addSync(new SyncItemStorageItemStack(GuiID, tile.storage, 2))

    addPlayerInventorySlots(inv, 5, 76)
  }


  override def eligibleForInput(item: ItemStack): Boolean = FurnaceRecipes.instance().getSmeltingResult(item) != null
}
