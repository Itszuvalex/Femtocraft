package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.industry.tile.TileMaterialProcessor
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.gui.OutputSlot
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.inventory.{ICrafting, Slot}
import net.minecraft.item.ItemStack

import scala.collection.JavaConversions._


object ContainerMaterialProcessor {
  val POWER_BIG_INDEX   = 0
  val POWER_SMALL_INDEX = 1
}

class ContainerMaterialProcessor(player: EntityPlayer, inv: InventoryPlayer, tile: TileMaterialProcessor) extends ContainerInv[TileMaterialProcessor](player, tile, 0, 0) {
  private var lastCookTime = 0
  private var lastPower    = 0L

  //Input
  addSlotToContainer(new Slot(tile.indInventory, 0, 35, 8))
  addSlotToContainer(new Slot(tile.indInventory, 1, 53, 8))
  addSlotToContainer(new Slot(tile.indInventory, 2, 35, 26))
  addSlotToContainer(new Slot(tile.indInventory, 3, 53, 26))
  //Assemblies
  addSlotToContainer(new Slot(tile.indInventory, 4, 152, 45))
  addSlotToContainer(new Slot(tile.indInventory, 5, 152, 63))
  //Output
  addSlotToContainer(new OutputSlot(tile.indInventory, 6, 35, 45))
  addSlotToContainer(new OutputSlot(tile.indInventory, 7, 53, 45))
  addSlotToContainer(new OutputSlot(tile.indInventory, 8, 35, 63))
  addSlotToContainer(new OutputSlot(tile.indInventory, 9, 53, 63))
  //Power
  addSlotToContainer(new Slot(tile.indInventory, 10, 10, 64))
  //Nanite
  addSlotToContainer(new Slot(tile.indInventory, 11, 152, 8))

  addPlayerInventorySlots(inv)

  override def addCraftingToCrafters(par1ICrafting: ICrafting) {
    super.addCraftingToCrafters(par1ICrafting)

    updatePower(par1ICrafting)
  }

  def updatePower(par1ICrafting: ICrafting): Unit = {
    sendUpdateToCrafter(this, par1ICrafting, ContainerMaterialProcessor.POWER_BIG_INDEX, (((inventory.getPowerCurrent & 0xFFFFFFFF00000000L) >> 32) & 0xFFFFFFFFL).toInt)
    sendUpdateToCrafter(this, par1ICrafting, ContainerMaterialProcessor.POWER_SMALL_INDEX, (inventory.getPowerCurrent & 0xFFFFFFFFL).toInt)
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
    lastCookTime = 0 /*inventory.furnaceCookTime*/
    lastPower = inventory.getPowerCurrent
  }

  @SideOnly(Side.CLIENT) override def updateProgressBar(par1: Int, par2: Int) = par1 match {
    case ContainerMaterialProcessor.POWER_BIG_INDEX =>
      inventory.setPower(
                          (par2.toLong << 32) | (inventory.getPowerCurrent & 0x00000000FFFFFFFFL)
                        )
    case ContainerMaterialProcessor.POWER_SMALL_INDEX =>
      inventory.setPower(
                          (inventory.getPowerCurrent & 0xFFFFFFFF00000000L) | (par2.toLong & 0xFFFFFFFFL)
                        )
    case _ =>
  }

  override def eligibleForInput(item: ItemStack): Boolean = false
}
