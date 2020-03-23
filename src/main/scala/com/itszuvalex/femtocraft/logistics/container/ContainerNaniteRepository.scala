package com.itszuvalex.femtocraft.logistics.container

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.nanite.INaniteTank
import com.itszuvalex.femtocraft.industry.container.SyncINaniteTank
import com.itszuvalex.femtocraft.logistics.tile.TileNaniteRepository
import com.itszuvalex.itszulib.container.ContainerInv
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack

class ContainerNaniteRepository(parPlayer: EntityPlayer, inv: InventoryPlayer, te: TileNaniteRepository, shouldSync: Boolean) extends ContainerInv[TileNaniteRepository](parPlayer, te, 0, 0, GuiIDs.TileNaniteRepositoryGuiID, shouldSync) {
  addSync(new SyncINaniteTank(GuiID, () => te.storage.copy(), (a: INaniteTank) => te.storage.deserializeNBT(a.serializeNBT())))

  if (shouldSync) {
    addPlayerInventorySlots(parPlayer.inventory)
  }

  override def eligibleForInput(item: ItemStack): Boolean = true
}
