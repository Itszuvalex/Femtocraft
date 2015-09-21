package com.itszuvalex.femtocraft.core.Industry.container

import com.itszuvalex.femtocraft.core.Industry.tile.TileFrame
import com.itszuvalex.itszulib.container.ContainerInv
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack

/**
 * Created by Christopher on 9/21/2015.
 */
class ContainerFrame(player: EntityPlayer, inv: InventoryPlayer, tile: TileFrame) extends ContainerInv[TileFrame](player, tile, 0, 0) {


  (0 until 9).foreach { i =>
    addSlotToContainer(new Slot(tile, i, 7 + 18 * i, 62))
                      }

  addPlayerInventorySlots(inv)

  override def canInteractWith(p_75145_1_ : EntityPlayer): Boolean = true

  override def eligibleForInput(item: ItemStack): Boolean = false
}
