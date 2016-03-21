package com.itszuvalex.femtocraft.logistics.container

import com.itszuvalex.femtocraft.logistics.container.ContainerItemRepository._
import com.itszuvalex.femtocraft.logistics.tile.TileItemRepository
import com.itszuvalex.itszulib.container.ContainerInv
import net.minecraft.entity.player.{InventoryPlayer, EntityPlayer}
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack

/**
  * Created by Christopher Harris (Itszuvalex) on 12/20/2015.
  */
object ContainerItemRepository {
  val inventoryStartX = 8
  val inventoryStartY = 12

  val playerInventoryStartX = 8
  val playerInventoryStartY = 129
}

class ContainerItemRepository(parPlayer: EntityPlayer, inv: InventoryPlayer, te: TileItemRepository) extends ContainerInv[TileItemRepository](parPlayer, te, 0, 0) {

  (0 until TileItemRepository.INVENTORY_SIZE).
  foreach { i =>
    addSlotToContainer(new Slot(te, i, inventoryStartX + (i % 9) * 18, inventoryStartY + (i / 9) * 18))
          }

  addPlayerInventorySlots(parPlayer.inventory, playerInventoryStartX, playerInventoryStartY)


  override def eligibleForInput(item: ItemStack): Boolean = false
}
