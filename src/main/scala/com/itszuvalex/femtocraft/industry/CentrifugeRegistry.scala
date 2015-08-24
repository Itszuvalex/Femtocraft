package com.itszuvalex.femtocraft.industry

import java.util

import com.itszuvalex.femtocraft.core.Initializable
import com.itszuvalex.femtocraft.industry.recipes.CentrifugeRecipe
import com.itszuvalex.itszulib.util.Comparators.FluidStack.IDNBTComparator
import net.minecraftforge.fluids.FluidStack

import scala.collection.JavaConverters._

/**
 * Created by Christopher on 8/24/2015.
 */
object CentrifugeRegistry extends Initializable {
  val recipeInputMap = new util.TreeMap[FluidStack, CentrifugeRecipe](IDNBTComparator).asScala

  def addRecipe(recipe: CentrifugeRecipe): Unit = {
    if (recipe == null || recipe.input == null) return
    recipeInputMap.put(recipe.input, recipe)
  }

  def findMatchingRecipe(input: FluidStack) = recipeInputMap.get(input)

  def getAllRecipes = recipeInputMap.values
}
