package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.FemtoFluids
import com.itszuvalex.itszulib.api.wrappers.{Converter, IItemStack}
import com.itszuvalex.itszulib.implicits.ItemStackImplicits.BlockAdditions
import net.minecraft.init.Blocks
import net.minecraftforge.fluids.FluidStack

import scala.collection.mutable.ArrayBuffer

object LiquifierRecipeRegistry {
  val recipes: ArrayBuffer[LiquifierRecipe] = new ArrayBuffer()


  def postInit(): Unit = {
    recipes += LiquifierRecipe(Blocks.COBBLESTONE.newIStack(), Converter.IFluidStackFromFluidStack(new FluidStack(FemtoFluids.slurryGritty, 500)))
  }

  def findMatchingRecipe(stack: IItemStack): Option[LiquifierRecipe] = recipes.find(_.base.isItemEqual(stack))
}
