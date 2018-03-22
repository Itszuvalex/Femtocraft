package com.itszuvalex.femtocraft.industry

import com.itszuvalex.itszulib.api.wrappers.IItemStack
import net.minecraftforge.fluids.Fluid

/**
  *
  * @param base
  * @param fluid
  * @param fluidRequired
  * @param ticks
  * @param fluidPerTick
  * @param powerPerTick
  * @param results ItemStack, (min, max)
  */
case class GerminationChamberRecipe(base: IItemStack, fluid: Fluid, fluidRequired: Boolean, ticks: Int, fluidPerTick: Int, powerPerTick: Int, results: Iterable[(IItemStack, (Int, Int))])
