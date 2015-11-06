package com.itszuvalex.femtocraft.cyber.container

import com.itszuvalex.femtocraft.cyber.GrowthChamberRegistry
import com.itszuvalex.femtocraft.cyber.tile.TileGrowthChamber
import com.itszuvalex.itszulib.container.ContainerInv
import net.minecraft.entity.player.{InventoryPlayer, EntityPlayer}
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
  for (i <- 0 to 8) { addSlotToContainer(new Slot(te.indInventory, i + 1, 80 + 18 * (i % 3), 21 + 18 * math.floor(i / 3d).toInt )) }

  addPlayerInventorySlots(inv)

  override def eligibleForInput(item: ItemStack): Boolean = GrowthChamberRegistry.findMatchingRecipe(item).isDefined
}
