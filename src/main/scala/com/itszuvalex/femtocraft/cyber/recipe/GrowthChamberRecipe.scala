package com.itszuvalex.femtocraft.cyber.recipe

import net.minecraft.item.ItemStack

import scala.beans.BeanProperty

/**
  * Created by Alex on 19.10.2015.
  */
object GrowthChamberRecipe {
  val TYPE_NO_RENDER = 0
  val TYPE_TEXTURE   = 1
  val TYPE_CUSTOM    = 2
}

/**
  * @param input
  * @param outputs
  * @param ticks Time requirement for processing. Minimum 100.
  * @param renderType Type of render in the machine, valid types are listed in the GrowthChamberRecipe object.
  * @param renderObj Ignored if renderType is 0.
  *                  If renderType is 1, either a ResourceLocation or an Array[ResourceLocation].
  *                  In case of an array, textures are used in order, equally distributed over the crafting process.
  *                  If renderType is 2, an IRecipeRenderer.
  */
case class GrowthChamberRecipe(@BeanProperty input: ItemStack,
                               @BeanProperty outputs: IndexedSeq[ItemStack],
                               @BeanProperty ticks: Int,
                               @BeanProperty renderType: Int,
                               @BeanProperty renderObj: AnyRef)
