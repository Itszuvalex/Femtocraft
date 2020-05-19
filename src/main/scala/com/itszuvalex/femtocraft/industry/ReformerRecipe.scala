package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.api.computation.FLOPS
import com.itszuvalex.femtocraft.api.nanite.INaniteStack
import com.itszuvalex.itszulib.api.wrappers.IItemStack

class ReformerRecipe(val item: IItemStack, val nanite: INaniteStack, val output: IItemStack, val FLOPsRequired: FLOPS) {
  def matches(inputItem: IItemStack, inputNanite: INaniteStack): Boolean = {
    IItemStack.itemStackEquality.apply.apply(item, inputItem) && INaniteStack.areNaniteStacksSameStrain(nanite, inputNanite)
  }
}
