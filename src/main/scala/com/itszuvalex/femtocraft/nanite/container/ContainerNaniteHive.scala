package com.itszuvalex.femtocraft.nanite.container

import com.itszuvalex.femtocraft.nanite.tile.TileNaniteHiveSmall
import com.itszuvalex.itszulib.container.ContainerInv
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.inventory.{ICrafting, Slot}
import net.minecraft.item.ItemStack

import scala.collection.JavaConversions._

/**
 * Created by Christopher on 9/1/2015.
 */
object ContainerNaniteHive {
  private val COOK_INDEX        = 0
  private val POWER_BIG_INDEX   = 1
  private val POWER_SMALL_INDEX = 2
  private val inventoryXStart   = 33
  private val inventoryYStart   = 21
}

class ContainerNaniteHive(player: EntityPlayer, inv: InventoryPlayer, tile: TileNaniteHiveSmall) extends ContainerInv[TileNaniteHiveSmall](player, tile, 0, 0) {
  private var lastPower = 0L

  for (i <- 0 until 3) {
    for (j <- 0 until 9) {
      addSlotToContainer(new Slot(tile, j + i * 9, ContainerNaniteHive.inventoryXStart + j * 18, ContainerNaniteHive.inventoryYStart + i * 18))
    }
  }

  addSlotToContainer(new Slot(tile, 27, 205, 21))
  addSlotToContainer(new Slot(tile, 28, 205, 39))
  addSlotToContainer(new Slot(tile, 29, 205, 57))
  addPlayerInventorySlots(inv, 33, 84)

  override def addCraftingToCrafters(par1ICrafting: ICrafting) {
    super.addCraftingToCrafters(par1ICrafting)

    updatePower(par1ICrafting)
  }

  def updatePower(par1ICrafting: ICrafting): Unit = {
    sendUpdateToCrafter(this, par1ICrafting, ContainerNaniteHive.POWER_BIG_INDEX, (((inventory.getPowerCurrent & 0xFFFFFFFF00000000L) >> 32) & 0xFFFFFFFFL).toInt)
    sendUpdateToCrafter(this, par1ICrafting, ContainerNaniteHive.POWER_SMALL_INDEX, (inventory.getPowerCurrent & 0xFFFFFFFFL).toInt)
  }

  /**
   * Looks for changes made in the container, sends them to every listener.
   */
  override def detectAndSendChanges() {
    super.detectAndSendChanges()
    crafters.foreach { case icrafting: ICrafting =>
      if (lastPower != inventory.getPowerCurrent) {
        updatePower(icrafting)
      }
                     }
    lastPower = inventory.getPowerCurrent
  }


  @SideOnly(Side.CLIENT) override def updateProgressBar(par1: Int, par2: Int) = par1 match {
    case ContainerNaniteHive.POWER_BIG_INDEX =>
      inventory.setPower(
                          (par2.toLong << 32) | (inventory.getPowerCurrent & 0x00000000FFFFFFFFL)
                        )
    case ContainerNaniteHive.POWER_SMALL_INDEX =>
      inventory.setPower(
                          (inventory.getPowerCurrent & 0xFFFFFFFF00000000L) | (par2.toLong & 0xFFFFFFFFL)
                        )
    case _ =>
  }

  override def eligibleForInput(item: ItemStack): Boolean = false
}
