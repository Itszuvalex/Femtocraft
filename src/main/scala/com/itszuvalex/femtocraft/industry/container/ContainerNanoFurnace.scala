package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.industry.tile.TileNanoFurnace
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.container.sync.{SyncDouble, SyncItemStorageItemStack}
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.FurnaceRecipes
import net.minecraft.util.EnumFacing

/**
  * Created by Alex on 18.08.2016.
  */
class ContainerNanoFurnace(player: EntityPlayer, inv: InventoryPlayer, tile: TileNanoFurnace, shouldSync: Boolean) extends ContainerInv[TileNanoFurnace](player, tile, 0, 1, shouldSync) {
  addSync(new SyncDouble(() => tile.getCapability(Capabilities.POWER_STORAGE, EnumFacing.UP).maxStorage, (a: Double) => tile.getCapability(Capabilities.POWER_STORAGE, EnumFacing.UP).maxStorage = a))
  addSync(new SyncDouble(() => tile.getCapability(Capabilities.POWER_STORAGE, EnumFacing.UP).storage, (a: Double) => tile.getCapability(Capabilities.POWER_STORAGE, EnumFacing.UP).storage = a))
  addSync(new SyncDouble(() => tile.getProgress, (a: Double) => tile.setProgress(a)))

  if (shouldSync) {
    addSync(new SyncItemStorageItemStack(tile.storage, 0))
    addSync(new SyncItemStorageItemStack(tile.storage, 1))

    addPlayerInventorySlots(inv, 5, 76)
  }


  override def eligibleForInput(item: ItemStack): Boolean = FurnaceRecipes.instance().getSmeltingResult(item) != null
}
