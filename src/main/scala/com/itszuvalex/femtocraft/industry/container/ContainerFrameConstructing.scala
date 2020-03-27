package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.industry.tile.TileFrame
import com.itszuvalex.itszulib.container.ContainerBase
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.inventory.IContainerListener

import scala.collection.JavaConversions._

/**
  * Created by Christopher Harris (Itszuvalex) on 2/18/2016.
  */
class ContainerFrameConstructing(player: EntityPlayer, inv: InventoryPlayer, tile: TileFrame) extends ContainerBase(GuiIDs.TileFrameConstructingGuiID, true) {
  var lastProgress = 0

  override def canInteractWith(p_75145_1_ : EntityPlayer): Boolean = true

  override def detectAndSendChanges(): Unit = {
    super.detectAndSendChanges()

    if (tile.isInvalid) {
      player.closeScreen()
    }
    else {
      tile.state.get match {
        case None =>
        case Some(s) =>
          listeners.foreach { crafter: IContainerListener =>
            if (s.progress != lastProgress) {
              sendUpdateToListener(this, crafter, 0, s.progress)
            }

            lastProgress = s.progress
          }
      }
    }
  }

  override def updateProgressBar(slot: Int, value: Int): Unit = {
    slot match {
      case 0 =>
        tile.state.get match {
          case None =>
          case Some(s) =>
            s.progress = value
        }
      case _ =>
    }
  }
}
