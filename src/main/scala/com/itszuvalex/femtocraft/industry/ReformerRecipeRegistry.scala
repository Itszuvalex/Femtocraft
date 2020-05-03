package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.api.nanite.INaniteStack
import com.itszuvalex.itszulib.api.wrappers.IItemStack

import scala.collection.mutable.ArrayBuffer

object ReformerRecipeRegistry {
  private val recipes = new ArrayBuffer[ReformerRecipe]()

  def addRecipe(recipe: ReformerRecipe): Unit = recipes += recipe

  def findMatchingRecipe(item: IItemStack, nanite: INaniteStack): Option[ReformerRecipe] = recipes.find(_.matches(item, nanite))

}
