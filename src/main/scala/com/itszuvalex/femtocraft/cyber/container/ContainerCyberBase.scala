package com.itszuvalex.femtocraft.cyber.container

import com.itszuvalex.femtocraft.core.Cyber.tile.TileCyberBase
import com.itszuvalex.itszulib.container.ContainerInv
import net.minecraft.entity.player.{InventoryPlayer, EntityPlayer}
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack

/**
 * Created by Alex on 03.10.2015.
 */
object ContainerCyberBase {
  val CYBERMASS_AMOUNT_ID = 0
  val BUFFER_1_FID_ID = 1
  val BUFFER_1_AMOUNT_ID = 2
  val BUFFER_2_FID_ID = 3
  val BUFFER_2_AMOUNT_ID = 4
}

class ContainerCyberBase(player: EntityPlayer, inv: InventoryPlayer, tile: TileCyberBase) extends ContainerInv[TileCyberBase](player, tile, 0, 0) {
  val bufferSlotSize = tile.size + 1

  for (i <- 0 until 9) addSlotToContainer(new Slot(tile, i, 89 + 18 * (i % 3), 37 + 18 * (i / 3)))
  for (i <- 0 until math.pow(bufferSlotSize, 2).toInt) addSlotToContainer(new Slot(tile, i + 9, 8 + 18 * (i % bufferSlotSize), 19 + 18 * (i / bufferSlotSize)))

  addPlayerInventorySlots(inv, 8, 95)

  override def eligibleForInput(item: ItemStack): Boolean = false

  override def canInteractWith(player: EntityPlayer): Boolean = true
}
