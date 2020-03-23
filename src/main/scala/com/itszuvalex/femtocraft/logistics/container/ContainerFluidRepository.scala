package com.itszuvalex.femtocraft.logistics.container

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.logistics.tile.TileFluidRepository
import com.itszuvalex.itszulib.api.ItszuLibCapabilities
import com.itszuvalex.itszulib.api.storage.IFluidStorageModifiable
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.container.sync.SyncFluidStorageFluidStack
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack

class ContainerFluidRepository(parPlayer: EntityPlayer, inv: InventoryPlayer, te: TileFluidRepository, shouldSync: Boolean) extends ContainerInv[TileFluidRepository](parPlayer, te, 0, 0, GuiIDs.TileFluidRepositoryGuiID, shouldSync) {
  te.getCapability(ItszuLibCapabilities.FLUID_STORAGE, null).indices.foreach(i => addSync(new SyncFluidStorageFluidStack(GuiID, te.getCapability(ItszuLibCapabilities.FLUID_STORAGE, null).asInstanceOf[IFluidStorageModifiable], i)))

  if (shouldSync) {
    addPlayerInventorySlots(parPlayer.inventory)
  }

  override def eligibleForInput(item: ItemStack): Boolean = true
}
