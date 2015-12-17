package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.industry.ArcFurnaceRegistry
import com.itszuvalex.femtocraft.industry.tile.TileArcFurnace
import com.itszuvalex.itszulib.container.ContainerInv
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.inventory.{ICrafting, Slot}
import net.minecraft.item.ItemStack

import scala.collection.JavaConversions._

/**
  * Created by Christopher on 9/1/2015.
  */
object ContainerArcFurnace {
  private val COOK_INDEX        = 0
  private val POWER_BIG_INDEX   = 1
  private val POWER_SMALL_INDEX = 2
}

class ContainerArcFurnace(player: EntityPlayer, inv: InventoryPlayer, tile: TileArcFurnace) extends ContainerInv[TileArcFurnace](player, tile, 0, 0) {
  private var lastCookTime = 0
  private var lastPower    = 0L

  addSlotToContainer(new Slot(tile.indInventory, 0, 0, 0))
  addPlayerInventorySlots(inv)

  override def addCraftingToCrafters(par1ICrafting: ICrafting) {
    super.addCraftingToCrafters(par1ICrafting)

    sendUpdateToCrafter(this, par1ICrafting, ContainerArcFurnace.COOK_INDEX, 0 /*inventory.furnaceCookTime*/)
    updatePower(par1ICrafting)
  }

  /**
    * Looks for changes made in the container, sends them to every listener.
    */
  override def detectAndSendChanges() {
    super.detectAndSendChanges()
    crafters.foreach { case icrafting: ICrafting =>
      if (lastCookTime != 0 /*inventory.furnaceCookTime*/ ) {
        sendUpdateToCrafter(this, icrafting, ContainerArcFurnace.COOK_INDEX, 0 /*inventory.furnaceCookTime*/)
      }
      if (lastPower != inventory.getPowerCurrent) {
        updatePower(icrafting)
      }
                     }
    lastCookTime = 0 /*inventory.furnaceCookTime*/
    lastPower = inventory.getPowerCurrent
  }

  def updatePower(par1ICrafting: ICrafting): Unit = {
    sendUpdateToCrafter(this, par1ICrafting, ContainerArcFurnace.POWER_BIG_INDEX, (((inventory.getPowerCurrent & 0xFFFFFFFF00000000L) >> 32) & 0xFFFFFFFFL).toInt)
    sendUpdateToCrafter(this, par1ICrafting, ContainerArcFurnace.POWER_SMALL_INDEX, (inventory.getPowerCurrent & 0xFFFFFFFFL).toInt)
  }

  @SideOnly(Side.CLIENT) override def updateProgressBar(par1: Int, par2: Int) = par1 match {
    case ContainerArcFurnace.COOK_INDEX =>
    case ContainerArcFurnace.POWER_BIG_INDEX =>
      inventory.setPower(
                          (par2.toLong << 32) | (inventory.getPowerCurrent & 0x00000000FFFFFFFFL)
                        )
    case ContainerArcFurnace.POWER_SMALL_INDEX =>
      inventory.setPower(
                          (inventory.getPowerCurrent & 0xFFFFFFFF00000000L) | (par2.toLong & 0xFFFFFFFFL)
                        )
    case _ =>
  }

  override def eligibleForInput(item: ItemStack): Boolean = ArcFurnaceRegistry.findMatchingRecipe(item).isDefined
}
