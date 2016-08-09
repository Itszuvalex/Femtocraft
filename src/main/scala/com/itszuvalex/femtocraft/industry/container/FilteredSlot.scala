package com.itszuvalex.femtocraft.industry.container

import net.minecraft.inventory.{IInventory, Slot}
import net.minecraft.item.ItemStack

/**
  * Created by Christopher Harris (Itszuvalex) on 2/27/2016.
  */
class FilteredSlot(inv: IInventory, slot: Int, x: Int, y: Int) extends Slot(inv, slot, x, y) {

  override def isItemValid(stack: ItemStack): Boolean = inv.isItemValidForSlot(slot, stack)
}
