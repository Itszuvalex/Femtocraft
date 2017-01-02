package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.cyber.CybermaterialRegistry
import com.itszuvalex.femtocraft.nanite.tile.TileNaniteExtractor
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.container.sync.{SyncDouble, SyncItemStorageItemStack}
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing

/**
  * Created by Alex on 18.08.2016.
  */
class ContainerNaniteExtractor(player: EntityPlayer, inv: InventoryPlayer, tile: TileNaniteExtractor, shouldSync: Boolean) extends ContainerInv[TileNaniteExtractor](player, tile, 0, 0, shouldSync) {
  addSync(new SyncDouble(() => tile.getCapability(Capabilities.POWER_STORAGE, EnumFacing.UP).storage, (a: Double) => tile.getCapability(Capabilities.POWER_STORAGE, EnumFacing.UP).storage = a))
  addSync(new SyncDouble(() => tile.getCapability(Capabilities.POWER_STORAGE, EnumFacing.UP).maxStorage, (a: Double) => tile.getCapability(Capabilities.POWER_STORAGE, EnumFacing.UP).maxStorage = a))
  addSync(new SyncDouble(() => tile.getProgress, (a: Double) => tile.setProgress(a)))

  if (shouldSync) {
    addSync(new SyncItemStorageItemStack(tile.storage, 0))
    //    addSlotToContainer(new FilteredSlot(tile, 0, 45, 24))

    addPlayerInventorySlots(inv, 5, 76)
  }

  override def eligibleForInput(item: ItemStack): Boolean = CybermaterialRegistry.getNaniteFromItem(item.getItem, item.getItemDamage).isDefined
}
