package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.industry.LiquifierRecipeRegistry
import com.itszuvalex.femtocraft.industry.tile.TileCrystalLiquifier
import com.itszuvalex.itszulib.api.wrappers.Converter
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.container.sync.{SyncDouble, SyncFluidStorageFluidStack, SyncItemStorageItemStack}
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack

/**
 * Created by Alex on 18.08.2016.
 */
class ContainerCrystalLiquifier(player: EntityPlayer, inv: InventoryPlayer, tile: TileCrystalLiquifier, shouldSync: Boolean) extends ContainerInv[TileCrystalLiquifier](player, tile, 0, 0, GuiIDs.TileCrystalLiquifierID, shouldSync) {
  addSync(new SyncDouble(GuiID, () => tile.internal.getProgress, (a: Double) => tile.internal.setProgress(a)))
  addSync(new SyncFluidStorageFluidStack(GuiID, tile.outputStorage, 0))

  if (shouldSync) {
    addSync(new SyncItemStorageItemStack(GuiID, tile.storage, 0))
    addSync(new SyncItemStorageItemStack(GuiID, tile.storage, 1))

    addPlayerInventorySlots(inv, 5, 76)
  }

  override def eligibleForInput(item: ItemStack): Boolean = LiquifierRecipeRegistry.findMatchingRecipe(Converter.IItemStackFromItemStack(item)).isDefined
}
