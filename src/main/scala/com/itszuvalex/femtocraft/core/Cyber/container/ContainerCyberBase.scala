package com.itszuvalex.femtocraft.core.Cyber.container

import com.itszuvalex.femtocraft.core.Cyber.tile.TileCyberBase
import com.itszuvalex.itszulib.container.ContainerInv
import net.minecraft.entity.player.{InventoryPlayer, EntityPlayer}
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack

/**
 * Created by Alex on 03.10.2015.
 */
class ContainerCyberBase(player: EntityPlayer, inv: InventoryPlayer, tile: TileCyberBase) extends ContainerInv[TileCyberBase](player, tile, 0, 0) {
  val bufferSlotSize = tile.size + 1

  for (i <- 0 until 9) addSlotToContainer(new Slot(tile, i, 8 + 18 * (i % 3), 26 + 18 * (i / 3)))
  for (i <- 0 until math.pow(bufferSlotSize, 2).toInt) addSlotToContainer(new Slot(tile, i + 9, 80 + 18 * (i % bufferSlotSize), 8 + 18 * (i / bufferSlotSize)))

  addPlayerInventorySlots(inv)

  override def eligibleForInput(item: ItemStack): Boolean = false

  override def canInteractWith(player: EntityPlayer): Boolean = true
}
