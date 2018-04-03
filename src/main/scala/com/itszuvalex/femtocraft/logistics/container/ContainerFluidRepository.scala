package com.itszuvalex.femtocraft.logistics.container

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.logistics.tile.TileFluidRepository
import com.itszuvalex.itszulib.api.ItszuLibCapabilities
import com.itszuvalex.itszulib.api.storage.IFluidStorage
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.container.sync.SyncIFluidStorage
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack


class ContainerFluidRepository(parPlayer: EntityPlayer, inv: InventoryPlayer, te: TileFluidRepository, shouldSync: Boolean) extends ContainerInv[TileFluidRepository](parPlayer, te, 0, 0, GuiIDs.TileFluidRepositoryGuiID, shouldSync) {
  addSync(new SyncIFluidStorage(te.getCapability(ItszuLibCapabilities.FLUID_STORAGE, null),
    GuiID,
    () => te.getCapability(ItszuLibCapabilities.FLUID_STORAGE, null),
    (storage: IFluidStorage) => {
      te.getCapability(ItszuLibCapabilities.FLUID_STORAGE, null).deserializeNBT(storage.serializeNBT())
      Unit
    }))

  if (shouldSync) {
    addPlayerInventorySlots(parPlayer.inventory)
  }

  override def eligibleForInput(item: ItemStack): Boolean = true
}
