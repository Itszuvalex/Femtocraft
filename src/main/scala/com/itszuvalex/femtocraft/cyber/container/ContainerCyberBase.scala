package com.itszuvalex.femtocraft.cyber.container

import com.itszuvalex.femtocraft.cyber.tile.TileCyberBase
import com.itszuvalex.itszulib.container.ContainerInv
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.inventory.{ICrafting, Slot}
import net.minecraft.item.ItemStack

import scala.collection.JavaConversions._

/**
  * Created by Itszuvalex on 1.12.2015
  */
object ContainerCyberBase {
  val CYBERMASS_AMOUNT_ID = 0
  val BUFFER_1_FID_ID     = 1
  val BUFFER_1_AMOUNT_ID  = 2
  val BUFFER_2_FID_ID     = 3
  val BUFFER_2_AMOUNT_ID  = 4
}

class ContainerCyberBase(player: EntityPlayer, inv: InventoryPlayer, tile: TileCyberBase) extends ContainerInv[TileCyberBase](player, tile, 0, 0) {
  val bufferSlotSize = tile.size + 1

  var slots = bufferSlotSize

  for (i <- 0 until 9) addSlotToContainer(new Slot(tile, i, 89 + 18 * (i % 3), 37 + 18 * (i / 3)))
  for (i <- 0 until math.pow(bufferSlotSize, 2).toInt) addSlotToContainer(new Slot(tile, i + 9, 8 + 18 * (i % bufferSlotSize), 19 + 18 * (i / bufferSlotSize)))

  addPlayerInventorySlots(inv, 8, 95)

  override def detectAndSendChanges(): Unit = {
    super.detectAndSendChanges()
    crafters.foreach { case crafter: ICrafting =>
      if (tile.remainingSlots != slots) crafter.sendProgressBarUpdate(this, 0, tile.remainingSlots)
      slots = tile.remainingSlots
                     }
  }

  override def updateProgressBar(id: Int, value: Int): Unit = {
    super.updateProgressBar(id, value)
    id match {
      case 0 => slots = value
      case _ =>
    }
  }

  override def eligibleForInput(item: ItemStack): Boolean = false

  override def canInteractWith(player: EntityPlayer): Boolean = true
}
