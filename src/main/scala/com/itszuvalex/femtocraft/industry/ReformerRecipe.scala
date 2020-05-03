package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.api.nanite.INaniteStack
import com.itszuvalex.itszulib.api.wrappers.IItemStack

class ReformerRecipe(val item: IItemStack, val nanite: INaniteStack) {
  def matches(inputItem: IItemStack, inputNanite: INaniteStack): Boolean = {
    IItemStack.itemStackEquality.apply.apply(item, inputItem) && INaniteStack.areNaniteStacksSameStrain(nanite, inputNanite)
  }
}
