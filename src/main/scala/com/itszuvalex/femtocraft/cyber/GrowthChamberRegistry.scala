package com.itszuvalex.femtocraft.cyber

import java.util

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.cyber.recipe.GrowthChamberRecipe
import com.itszuvalex.itszulib.util.Comparators.ItemStack.IDDamageWildCardComparator
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation

import scala.collection.JavaConverters._

/**
 * Created by Alex on 19.10.2015.
 */
object GrowthChamberRegistry {
  val recipeInputMap = new util.TreeMap[ItemStack, GrowthChamberRecipe](IDDamageWildCardComparator).asScala

  def addRecipe(recipe: GrowthChamberRecipe): Unit = {
    if (recipe == null || recipe.input == null) return
    if (recipe.renderType == 1 && !recipe.renderObj.isInstanceOf[ResourceLocation] && !recipe.renderObj.isInstanceOf[Array[ResourceLocation]]) {
      Femtocraft.logger.error("Tried to register Growth Chamber recipe with texture render type, but renderObj is not a ResourceLocation or an Array[ResourceLocation]!")
      return
    }
    if (recipe.renderType == 2 && !recipe.renderObj.isInstanceOf[IRecipeRenderer]) {
      Femtocraft.logger.error("Tried to register Growth Chamber recipe with custom render type, but renderObj is not an IRecipeRenderer!")
      return
    }
    recipeInputMap.put(recipe.input, recipe)
  }

  def findMatchingRecipe(input: ItemStack) = recipeInputMap.get(input)

  def getAllRecipes = recipeInputMap.values
}
