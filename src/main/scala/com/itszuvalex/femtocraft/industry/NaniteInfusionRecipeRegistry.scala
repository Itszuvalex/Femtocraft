package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.FemtoItems
import com.itszuvalex.femtocraft.api.nanite.{NaniteRegistry, NaniteStack}
import com.itszuvalex.itszulib.api.wrappers.IItemStack
import com.itszuvalex.itszulib.implicits.ItemStackImplicits._
import net.minecraft.init.Items

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object NaniteInfusionRecipeRegistry {
  val recipes: ArrayBuffer[NaniteInfusionRecipe] = mutable.ArrayBuffer[NaniteInfusionRecipe]()

  def postInit(): Unit = {
    recipes += NaniteInfusionRecipe(FemtoItems.itemRiftironIngotDevoid.newIStack(), new NaniteStack(NaniteRegistry.NANITE_DUMB, 1), FemtoItems.itemRiftironIngotActivated.newIStack())
    recipes += NaniteInfusionRecipe(FemtoItems.itemPhasemetalIngotDevoid.newIStack(), new NaniteStack(NaniteRegistry.NANITE_DUMB, 1), FemtoItems.itemPhasemetalIngotActivated.newIStack())
    recipes += NaniteInfusionRecipe(Items.REDSTONE.newIStack(), new NaniteStack(NaniteRegistry.NANITE_DUMB, 1), FemtoItems.itemRedstonereplacementDust.newIStack())
    recipes += NaniteInfusionRecipe(Items.DYE.newIStack(1, 4), new NaniteStack(NaniteRegistry.NANITE_DUMB, 1), FemtoItems.itemLapisreplacementDust.newIStack())
    recipes += NaniteInfusionRecipe(FemtoItems.itemDiamondDust.newIStack(), new NaniteStack(NaniteRegistry.NANITE_DUMB, 1), FemtoItems.itemDiamondreplacementDust.newIStack())
  }

  def getMatchingRecipe(itemstack: IItemStack): Option[NaniteInfusionRecipe] = recipes.find(_.input.isItemEqual(itemstack))

}
