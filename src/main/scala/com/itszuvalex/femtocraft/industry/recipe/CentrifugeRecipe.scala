package com.itszuvalex.femtocraft.industry.recipe

import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidStack

import scala.beans.BeanProperty

/**
 * Created by Christopher on 8/24/2015.
 */
case class CentrifugeRecipe(@BeanProperty input: FluidStack,
                            @BeanProperty particulates: ItemStack,
                            @BeanProperty partChance: Float,
                            @BeanProperty output: FluidStack,
                            @BeanProperty ticks: Int)


