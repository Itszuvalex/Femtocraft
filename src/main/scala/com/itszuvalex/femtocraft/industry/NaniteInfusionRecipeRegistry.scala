package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.FemtoItems
import com.itszuvalex.femtocraft.api.nanite.{NaniteRegistry, NaniteStack}
import com.itszuvalex.itszulib.api.wrappers.IItemStack
import com.itszuvalex.itszulib.implicits.ItemStackImplicits._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object NaniteInfusionRecipeRegistry {
  val recipes: ArrayBuffer[NaniteInfusionRecipe] = mutable.ArrayBuffer[NaniteInfusionRecipe]()

  def postInit(): Unit = {
    recipes += NaniteInfusionRecipe(FemtoItems.itemRiftironIngotDevoid.newIStack(), new NaniteStack(NaniteRegistry.NANITE_DUMB, 1), FemtoItems.itemRiftironIngotActivated.newIStack())
    recipes += NaniteInfusionRecipe(FemtoItems.itemPhasemetalIngotDevoid.newIStack(), new NaniteStack(NaniteRegistry.NANITE_DUMB, 1), FemtoItems.itemPhasemetalIngotActivated.newIStack())
  }

  def getMatchingRecipe(itemstack: IItemStack): Option[NaniteInfusionRecipe] = recipes.find(_.input.isItemEqual(itemstack))

}
