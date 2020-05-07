package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.api.nanite.INaniteStack
import com.itszuvalex.itszulib.api.wrappers.IItemStack

import scala.collection.mutable.ArrayBuffer

object FabricatorRecipeRegistry {
  private val recipes = new ArrayBuffer[FabricatorRecipe]()

  def addRecipe(recipe: FabricatorRecipe): Unit = recipes += recipe

  def findMatchingRecipe(items: Seq[IItemStack], nanite: INaniteStack): Option[FabricatorRecipe] = recipes.find(_.matches(items, nanite))

}
