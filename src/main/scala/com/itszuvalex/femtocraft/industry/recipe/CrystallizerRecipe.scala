package com.itszuvalex.femtocraft.industry.recipe

import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidStack

import scala.beans.BeanProperty

/**
  * Created by Christopher on 8/24/2015.
  */
case class CrystallizerRecipe(@BeanProperty input: FluidStack,
                              @BeanProperty output: ItemStack,
                              @BeanProperty ticks: Int)


