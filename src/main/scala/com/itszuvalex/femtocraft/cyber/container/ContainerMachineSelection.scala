package com.itszuvalex.femtocraft.cyber.container

import com.itszuvalex.femtocraft.cyber.tile.TileCyberBase
import com.itszuvalex.itszulib.container.ContainerBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.ICrafting

import scala.collection.JavaConversions._

/**
  * Created by Alex on 15.10.2015.
  */
class ContainerMachineSelection(val tile: TileCyberBase) extends ContainerBase {
  var slots = -1

  override def detectAndSendChanges(): Unit = {
    super.detectAndSendChanges()
    crafters.foreach { case crafter: ICrafting =>
      if (tile.remainingSlots != slots) sendUpdateToCrafter(this, crafter, 0, tile.remainingSlots)
                     }
    slots = tile.remainingSlots
  }

  override def updateProgressBar(id: Int, value: Int): Unit = {
    super.updateProgressBar(id, value)
    id match {
      case 0 => slots = value
      case _ =>
    }
  }

  override def canInteractWith(player: EntityPlayer): Boolean = true
}
