package com.itszuvalex.femtocraft.cyber.container

import com.itszuvalex.femtocraft.cyber.GrowthChamberRegistry
import com.itszuvalex.femtocraft.cyber.tile.TileGrowthChamber
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.gui.OutputSlot
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.inventory.{ICrafting, Slot}
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.{FluidRegistry, FluidStack}

import scala.collection.JavaConversions._

/**
  * Created by Alex on 18.10.2015.
  */
class ContainerGrowthChamber(player: EntityPlayer, inv: InventoryPlayer, te: TileGrowthChamber) extends ContainerInv[TileGrowthChamber](player, te, 0, 9) {

  var prevProgress = 0
  var prevWaterAmt = 0

  addSlotToContainer(new Slot(te.indInventory, 0, 8, 21) {
    override def isItemValid(stack: ItemStack): Boolean = GrowthChamberRegistry.findMatchingRecipe(stack).isDefined
  })
  for (i <- 0 to 8) {addSlotToContainer(new OutputSlot(te.indInventory, i + 1, 80 + 18 * (i % 3), 21 + 18 * math.floor(i / 3d).toInt))}

  addPlayerInventorySlots(inv)

  override def detectAndSendChanges(): Unit = {
    super.detectAndSendChanges()
    crafters.foreach { case crafter: ICrafting =>
      if (te.progress != prevProgress) crafter.sendProgressBarUpdate(this, 0, te.progress);
      prevProgress = te.progress
      if (te.tank.getFluidAmount != prevWaterAmt) crafter.sendProgressBarUpdate(this, 1, te.tank.getFluidAmount);
      prevWaterAmt = te.tank.getFluidAmount
                     }
  }

  override def updateProgressBar(id: Int, value: Int): Unit = {
    super.updateProgressBar(id, value)
    id match {
      case 0 => te.progress = value
      case 1 => te.tank.setFluid(if (value == 0) null else new FluidStack(FluidRegistry.WATER, value))
    }
  }

  override def eligibleForInput(item: ItemStack): Boolean = GrowthChamberRegistry.findMatchingRecipe(item).isDefined
}
