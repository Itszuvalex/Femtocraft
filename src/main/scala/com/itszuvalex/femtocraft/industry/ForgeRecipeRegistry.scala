package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.api.nanite.INaniteStack
import com.itszuvalex.itszulib.api.wrappers.IItemStack

import scala.collection.mutable.ArrayBuffer

object ForgeRecipeRegistry {
  private val recipes = new ArrayBuffer[ForgeRecipe]()

  def addRecipe(recipe: ForgeRecipe): Unit = recipes += recipe

  def findMatchingRecipe(items: Seq[IItemStack], nanite: INaniteStack): Option[ForgeRecipe] = recipes.find(_.matches(items, nanite))

}
