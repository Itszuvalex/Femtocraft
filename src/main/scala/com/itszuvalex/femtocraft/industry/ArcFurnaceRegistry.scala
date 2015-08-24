package com.itszuvalex.femtocraft.industry

import java.util

import com.itszuvalex.femtocraft.core.Initializable
import com.itszuvalex.femtocraft.industry.recipes.ArcFurnaceRecipe
import com.itszuvalex.itszulib.util.Comparators.ItemStack.IDDamageWildCardComparator
import net.minecraft.item.ItemStack

import scala.collection.JavaConverters._

/**
 * Created by Christopher on 8/24/2015.
 */
object ArcFurnaceRegistry extends Initializable {
  val recipeInputMap = new util.TreeMap[ItemStack, ArcFurnaceRecipe](IDDamageWildCardComparator).asScala

  def addRecipe(recipe: ArcFurnaceRecipe): Unit = {
    if (recipe == null || recipe.input == null) return
    recipeInputMap.put(recipe.input, recipe)
  }

  def findMatchingRecipe(input: ItemStack) = recipeInputMap.get(input)

  def getAllRecipes = recipeInputMap.values
}
