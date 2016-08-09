package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.industry.tile.TileFrame
import com.itszuvalex.itszulib.container.ContainerBase
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.inventory.IContainerListener

import scala.collection.JavaConversions._

/**
  * Created by Christopher Harris (Itszuvalex) on 2/18/2016.
  */
class ContainerFrameConstructing(player: EntityPlayer, inv: InventoryPlayer, tile: TileFrame) extends ContainerBase {
  var lastProgress = 0

  override def canInteractWith(p_75145_1_ : EntityPlayer): Boolean = true

  override def detectAndSendChanges(): Unit = {
    super.detectAndSendChanges()

    if (tile.isInvalid) {
      player.closeScreen()
    }
    else {
      listeners.foreach { crafter: IContainerListener =>
        if (tile.progress != lastProgress) {
          sendUpdateToListener(this, crafter, 0, tile.progress)
        }

        lastProgress = tile.progress
      }
    }
  }

  override def updateProgressBar(slot: Int, value: Int): Unit = {
    slot match {
      case 0 =>
        tile.progress = value
      case _ =>
    }
  }
}
