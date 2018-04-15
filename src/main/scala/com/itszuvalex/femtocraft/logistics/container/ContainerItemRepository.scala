package com.itszuvalex.femtocraft.logistics.container

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.logistics.tile.TileItemRepository
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.container.sync.SyncItemStorageItemStack
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack

/**
  * Created by Christopher Harris (Itszuvalex) on 12/20/2015.
  */
object ContainerItemRepository {
  val playerInventoryStartX = 8
  val playerInventoryStartY = 129
}

class ContainerItemRepository(parPlayer: EntityPlayer, inv: InventoryPlayer, te: TileItemRepository, shouldSync: Boolean) extends ContainerInv[TileItemRepository](parPlayer, te, 0, 0, GuiIDs.TileItemRepositoryGuiID, shouldSync) {

  if (shouldSync) {
    te.storage.indices.foreach { i => addSync(new SyncItemStorageItemStack(GuiID, te.storage, i)) }
    addPlayerInventorySlots(parPlayer.inventory, ContainerItemRepository.playerInventoryStartX, ContainerItemRepository.playerInventoryStartY)
  }

  override def eligibleForInput(item: ItemStack): Boolean = true
}
