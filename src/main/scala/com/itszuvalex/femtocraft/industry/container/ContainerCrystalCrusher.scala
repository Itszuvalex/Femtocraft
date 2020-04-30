package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.industry.DustRecipeRegistry
import com.itszuvalex.femtocraft.industry.tile.TileCrystalCrusher
import com.itszuvalex.itszulib.api.wrappers.Converter
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.container.sync.{SyncDouble, SyncItemStorageItemStack}
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack

/**
  * Created by Alex on 18.08.2016.
  */
class ContainerCrystalCrusher(player: EntityPlayer, inv: InventoryPlayer, tile: TileCrystalCrusher, shouldSync: Boolean) extends ContainerInv[TileCrystalCrusher](player, tile, 0, 1, GuiIDs.TileCrystalCrusherID, shouldSync) {
  addSync(new SyncDouble(GuiID, () => tile.internal.getProgress, (a: Double) => tile.internal.setProgress(a)))

  if (shouldSync) {
    addSync(new SyncItemStorageItemStack(GuiID, tile.storage, 0))
    addSync(new SyncItemStorageItemStack(GuiID, tile.storage, 1))
    addSync(new SyncItemStorageItemStack(GuiID, tile.storage, 2))

    addPlayerInventorySlots(inv, 5, 76)
  }

  override def eligibleForInput(item: ItemStack): Boolean = DustRecipeRegistry.getDust(Converter.IItemStackFromItemStack(item)).isDefined
}
