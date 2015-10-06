package com.itszuvalex.femtocraft.core.Cyber.container

import com.itszuvalex.femtocraft.core.Cyber.tile.TileCyberBase
import com.itszuvalex.itszulib.container.ContainerInv
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.entity.player.{InventoryPlayer, EntityPlayer}
import net.minecraft.inventory.{ICrafting, Slot}
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.{FluidRegistry, FluidStack}

import scala.collection.JavaConversions._

/**
 * Created by Alex on 03.10.2015.
 */
object ContainerCyberBase {
  val CYBERMASS_AMOUNT_ID = 0
  val BUFFER_1_FID_ID = 1
  val BUFFER_1_AMOUNT_ID = 2
  val BUFFER_2_FID_ID = 3
  val BUFFER_2_AMOUNT_ID = 4
}

class ContainerCyberBase(player: EntityPlayer, inv: InventoryPlayer, tile: TileCyberBase) extends ContainerInv[TileCyberBase](player, tile, 0, 0) {
  val bufferSlotSize = tile.size + 1

  var lastCybermassAmount = 0
  var lastB1FluidID = 0
  var lastB1FluidAmount = 0
  var lastB2FluidID = 0
  var lastB2FluidAmount = 0

  for (i <- 0 until 9) addSlotToContainer(new Slot(tile, i, 89 + 18 * (i % 3), 37 + 18 * (i / 3)))
  for (i <- 0 until math.pow(bufferSlotSize, 2).toInt) addSlotToContainer(new Slot(tile, i + 9, 8 + 18 * (i % bufferSlotSize), 19 + 18 * (i / bufferSlotSize)))

  addPlayerInventorySlots(inv, 8, 95)

  override def detectAndSendChanges(): Unit = {
    super.detectAndSendChanges()
    crafters.foreach { case crafter: ICrafting =>
      if (tile.tanks(0).getFluidAmount != lastCybermassAmount) {
        sendUpdateToCrafter(this, crafter, ContainerCyberBase.CYBERMASS_AMOUNT_ID, tile.tanks(0).getFluidAmount)
        lastCybermassAmount = tile.tanks(0).getFluidAmount
      }
      if (tile.tanks(1).getFluid != null) {
        if (tile.tanks(1).getFluid.getFluidID != lastB1FluidID) {
          sendUpdateToCrafter(this, crafter, ContainerCyberBase.BUFFER_1_FID_ID, tile.tanks(1).getFluid.getFluidID)
          lastB1FluidID = tile.tanks(1).getFluid.getFluidID
        }
        if (tile.tanks(1).getFluidAmount != lastB1FluidAmount) {
          sendUpdateToCrafter(this, crafter, ContainerCyberBase.BUFFER_1_AMOUNT_ID, tile.tanks(1).getFluidAmount)
          lastB1FluidAmount = tile.tanks(1).getFluidAmount
        }
      }
      if (tile.size == 3 && tile.tanks(2).getFluid != null) {
        if (tile.tanks(2).getFluid.getFluidID != lastB2FluidID) {
          sendUpdateToCrafter(this, crafter, ContainerCyberBase.BUFFER_2_FID_ID, tile.tanks(2).getFluid.getFluidID)
          lastB2FluidID = tile.tanks(2).getFluid.getFluidID
        }
        if (tile.tanks(2).getFluidAmount != lastB2FluidAmount) {
          sendUpdateToCrafter(this, crafter, ContainerCyberBase.BUFFER_2_AMOUNT_ID, tile.tanks(2).getFluidAmount)
          lastB2FluidAmount = tile.tanks(2).getFluidAmount
        }
      }
    }
  }

  @SideOnly(Side.CLIENT)
  override def updateProgressBar(id: Int, value: Int): Unit = {
    id match {
      case ContainerCyberBase.CYBERMASS_AMOUNT_ID =>
        tile.tanks(0).setFluid(new FluidStack(/* FemtoFluids.cybermass */ FluidRegistry.WATER, value))
      case ContainerCyberBase.BUFFER_1_FID_ID =>
        tile.tanks(1).setFluid(new FluidStack(FluidRegistry.getFluid(value), tile.tanks(1).getFluidAmount))
      case ContainerCyberBase.BUFFER_1_AMOUNT_ID =>
        tile.tanks(1).setFluid(new FluidStack(tile.tanks(1).getFluid, value))
      case ContainerCyberBase.BUFFER_2_FID_ID =>
        if (tile.size == 3) tile.tanks(2).setFluid(new FluidStack(FluidRegistry.getFluid(value), tile.tanks(2).getFluidAmount))
      case ContainerCyberBase.BUFFER_2_AMOUNT_ID =>
        if (tile.size == 3) tile.tanks(2).setFluid(new FluidStack(tile.tanks(2).getFluid, value))
      case _ =>
    }
  }

  override def eligibleForInput(item: ItemStack): Boolean = false

  override def canInteractWith(player: EntityPlayer): Boolean = true
}
