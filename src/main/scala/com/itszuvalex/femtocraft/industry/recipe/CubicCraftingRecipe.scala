package com.itszuvalex.femtocraft.industry.recipe

import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidStack

import scala.beans.BeanProperty

/**
 * Created by Christopher on 8/24/2015.
 */
case class CubicCraftingRecipe(@BeanProperty size: Int,
                               @BeanProperty inputItems: Array[Array[ItemStack]],
                               @BeanProperty inputFluid: FluidStack,
                               @BeanProperty output: ItemStack,
                               @BeanProperty ticks: Int)


