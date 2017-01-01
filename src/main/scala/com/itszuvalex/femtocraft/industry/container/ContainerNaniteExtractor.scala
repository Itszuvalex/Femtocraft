package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.cyber.CybermaterialRegistry
import com.itszuvalex.femtocraft.nanite.tile.TileNaniteExtractor
import com.itszuvalex.itszulib.container.ContainerInv
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.inventory.IContainerListener
import net.minecraft.item.ItemStack

import scala.collection.JavaConversions._

/**
  * Created by Alex on 18.08.2016.
  */
class ContainerNaniteExtractor(player: EntityPlayer, inv: InventoryPlayer, tile: TileNaniteExtractor) extends ContainerInv[TileNaniteExtractor](player, tile, 0, 0, true) {
  var powerMaxLast    : Double = _
  var powerCurrentLast: Double = _
  var progressLast    : Double = _

  addSlotToContainer(new FilteredSlot(tile, 0, 45, 24))

  addPlayerInventorySlots(inv, 5, 76)

  override def eligibleForInput(item: ItemStack): Boolean = CybermaterialRegistry.getNaniteFromItem(item.getItem, item.getItemDamage).isDefined

  override def detectAndSendChanges(): Unit = {
    super.detectAndSendChanges()

    listeners.foreach { listener =>
      if (powerMaxLast != tile.getPowerMax)
        sendUpdateToListener(this, listener, 0, tile.getPowerMax.toInt)

      if (powerCurrentLast != tile.getPowerCurrent)
        sendUpdateToListener(this, listener, 1, tile.getPowerCurrent.toInt)

      if (progressLast != tile.getProgress)
        sendUpdateToListener(this, listener, 2, tile.getProgress.toInt)
    }

    powerMaxLast = tile.getPowerMax
    powerCurrentLast = tile.getPowerCurrent
    progressLast = tile.getProgress
  }

  override def addListener(listener: IContainerListener) {
    super.addListener(listener)
    listener.sendAllWindowProperties(this, tile)
  }

  override def updateProgressBar(id: Int, data: Int): Unit = {
    id match {
      case 0 => tile.powerMax = data.toDouble
      case 1 => tile.setPower(data.toDouble)
      case 2 => tile.setProgress(data.toDouble)
      case _ =>
    }
  }
}
