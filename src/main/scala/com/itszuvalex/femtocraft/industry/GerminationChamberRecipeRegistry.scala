package com.itszuvalex.femtocraft.industry

import com.itszuvalex.itszulib.api.wrappers.IItemStack
import com.itszuvalex.itszulib.implicits.ItemStackImplicits._
import net.minecraft.init.{Blocks, Items}
import net.minecraftforge.fluids.FluidRegistry

import scala.collection.mutable.ArrayBuffer

object GerminationChamberRecipeRegistry {
  val recipes: ArrayBuffer[GerminationChamberRecipe] = ArrayBuffer[GerminationChamberRecipe]()

  def postInit(): Unit = {
    recipes += GerminationChamberRecipe(Items.WHEAT_SEEDS.newIStack(), FluidRegistry.WATER, true, 20 * 60, 5, 20, Array((Items.WHEAT_SEEDS.newIStack(), (1, 2)), (Items.WHEAT.newIStack(), (2, 4))))
    recipes += GerminationChamberRecipe(Items.MELON_SEEDS.newIStack(), FluidRegistry.WATER, true, 20 * 60, 5, 20, Array((Items.MELON_SEEDS.newIStack(), (1, 2)), (Blocks.MELON_BLOCK.newIStack(), (2, 4))))
    recipes += GerminationChamberRecipe(Items.PUMPKIN_SEEDS.newIStack(), FluidRegistry.WATER, true, 20 * 60, 5, 20, Array((Items.PUMPKIN_SEEDS.newIStack(), (1, 2)), (Blocks.PUMPKIN.newIStack(), (2, 4))))
    recipes += GerminationChamberRecipe(Items.BEETROOT_SEEDS.newIStack(), FluidRegistry.WATER, true, 20 * 60, 5, 20, Array((Items.BEETROOT_SEEDS.newIStack(), (1, 2)), (Items.BEETROOT.newIStack(), (2, 4))))
    recipes += GerminationChamberRecipe(Blocks.CACTUS.newIStack(), FluidRegistry.WATER, true, 20 * 60, 5, 20, Array((Blocks.CACTUS.newIStack(), (2, 4))))
    recipes += GerminationChamberRecipe(Items.SUGAR.newIStack(), FluidRegistry.WATER, true, 20 * 60, 5, 20, Array((Items.SUGAR.newIStack(), (2, 4))))
    recipes += GerminationChamberRecipe(Blocks.SAPLING.newIStack(), FluidRegistry.WATER, true, 20 * 120, 5, 20, Array((Blocks.LOG.newIStack(), (8, 32)), (Blocks.LEAVES.newIStack(), (16, 32))))
  }

  def findMatchingRecipe(stack: IItemStack): Option[GerminationChamberRecipe] = recipes.find(_.base.isItemEqual(stack))

}
