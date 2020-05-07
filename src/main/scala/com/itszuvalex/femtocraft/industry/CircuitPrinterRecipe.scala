package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.api.nanite.INaniteStack
import com.itszuvalex.itszulib.api.wrappers.IItemStack

class CircuitPrinterRecipe(val items: Seq[IItemStack], val nanite: INaniteStack, val output: IItemStack, val FLOPsRequired: Double) {
  def matches(inputItems: Seq[IItemStack], inputNanite: INaniteStack): Boolean = {
    //TODO make better
    items.length == inputItems.length && items.indices.forall(i => IItemStack.itemStackEquality.apply.apply(items(i), inputItems(i))) && INaniteStack.areNaniteStacksSameStrain(nanite, inputNanite)
  }
}
