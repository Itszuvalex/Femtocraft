package com.itszuvalex.femtocraft.industry.recipe

import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidStack

import scala.beans.BeanProperty

/**
 * Created by Christopher on 8/24/2015.
 */
case class ArcFurnaceRecipe(@BeanProperty input: ItemStack,
                            @BeanProperty output: FluidStack,
                            @BeanProperty tempMin: Int,
                            @BeanProperty ticks: Int)

