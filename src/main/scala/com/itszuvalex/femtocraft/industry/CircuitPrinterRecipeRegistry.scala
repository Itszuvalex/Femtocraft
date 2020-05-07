package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.api.nanite.INaniteStack
import com.itszuvalex.itszulib.api.wrappers.IItemStack

import scala.collection.mutable.ArrayBuffer

object CircuitPrinterRecipeRegistry {
  private val recipes = new ArrayBuffer[CircuitPrinterRecipe]()

  def addRecipe(recipe: CircuitPrinterRecipe): Unit = recipes += recipe

  def findMatchingRecipe(items: Seq[IItemStack], nanite: INaniteStack): Option[CircuitPrinterRecipe] = recipes.find(_.matches(items, nanite))

}
