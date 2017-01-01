package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.industry.tile.TileNanoFurnace
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.gui.OutputSlot
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.inventory.IContainerListener
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.FurnaceRecipes

import scala.collection.JavaConversions._

/**
  * Created by Alex on 18.08.2016.
  */
class ContainerNanoFurnace(player: EntityPlayer, inv: InventoryPlayer, tile: TileNanoFurnace) extends ContainerInv[TileNanoFurnace](player, tile, 0, 1, true) {
  var powerMaxLast    : Double = _
  var powerCurrentLast: Double = _
  var progressLast    : Double = _

  addSlotToContainer(new FilteredSlot(tile, 0, 45, 24))
  addSlotToContainer(new OutputSlot(tile, 1, 86, 24))

  addPlayerInventorySlots(inv, 5, 76)

  override def eligibleForInput(item: ItemStack): Boolean = FurnaceRecipes.instance().getSmeltingResult(item) != null

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
