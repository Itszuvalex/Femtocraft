package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.industry.ArcFurnaceRegistry
import com.itszuvalex.femtocraft.industry.tile.TileArcFurnace
import com.itszuvalex.itszulib.container.ContainerInv
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.inventory.{IContainerListener, Slot}
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

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


  override def addListener(listener: IContainerListener): Unit = {
    super.addListener(listener)

    sendUpdateToListener(this, listener, ContainerArcFurnace.COOK_INDEX, 0 /*inventory.furnaceCookTime*/)
    updatePower(listener)
  }

  /**
    * Looks for changes made in the container, sends them to every listener.
    */
  override def detectAndSendChanges() {
    super.detectAndSendChanges()
    listeners.foreach { case icrafting: IContainerListener =>
      if (lastCookTime != 0 /*inventory.furnaceCookTime*/ ) {
        sendUpdateToListener(this, icrafting, ContainerArcFurnace.COOK_INDEX, 0 /*inventory.furnaceCookTime*/)
      }
      if (lastPower != inventory.getPowerCurrent) {
        updatePower(icrafting)
      }
                      }
    lastCookTime = 0 /*inventory.furnaceCookTime*/
    lastPower = inventory.getPowerCurrent.toLong
  }

  def updatePower(par1ICrafting: IContainerListener): Unit = {
    sendUpdateToListener(this, par1ICrafting, ContainerArcFurnace.POWER_BIG_INDEX, (((inventory.getPowerCurrent.toLong & 0xFFFFFFFF00000000L) >> 32) & 0xFFFFFFFFL).toInt)
    sendUpdateToListener(this, par1ICrafting, ContainerArcFurnace.POWER_SMALL_INDEX, (inventory.getPowerCurrent.toLong & 0xFFFFFFFFL).toInt)
  }

  @SideOnly(Side.CLIENT) override def updateProgressBar(par1: Int, par2: Int) = par1 match {
    case ContainerArcFurnace.COOK_INDEX =>
    case ContainerArcFurnace.POWER_BIG_INDEX =>
      inventory.setPower(
                          (par2.toLong << 32) | (inventory.getPowerCurrent.toLong & 0x00000000FFFFFFFFL)
                        )
    case ContainerArcFurnace.POWER_SMALL_INDEX =>
      inventory.setPower(
                          (inventory.getPowerCurrent.toLong & 0xFFFFFFFF00000000L) | (par2.toLong & 0xFFFFFFFFL)
                        )
    case _ =>
  }

  override def eligibleForInput(item: ItemStack): Boolean = ArcFurnaceRegistry.findMatchingRecipe(item).isDefined
}
