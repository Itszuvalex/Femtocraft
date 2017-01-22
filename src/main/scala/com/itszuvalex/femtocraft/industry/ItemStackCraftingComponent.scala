package com.itszuvalex.femtocraft.industry

import com.itszuvalex.itszulib.api.wrappers.IItemStack

/**
  * Created by Chris on 1/21/2017.
  */
object ItemStackCraftingComponent {
  implicit def StrictCraftingComponent(req: IItemStack): ItemStackCraftingComponent = {
    ItemStackCraftingComponent(req, (b) => IItemStack.itemStackEquality.apply(req, b))
  }
}

case class ItemStackCraftingComponent(req: IItemStack, equivalency: (IItemStack) => Boolean)

