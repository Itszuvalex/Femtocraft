package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.FemtoItems
import com.itszuvalex.itszulib.api.wrappers.{IItemStack, WrapperVanillaItemStack}
import net.minecraft.item.ItemStack

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

/**
  * Created by Chris on 1/21/2017.
  */
object SynthesizerRegistry {
  val recipes: ArrayBuffer[SynthesizerRecipe] = new ArrayBuffer[SynthesizerRecipe]()

  def addRecipe(recipe: SynthesizerRecipe): Unit = recipes += recipe

  def getMatchingRecipesForOutput(item: IItemStack): java.util.Collection[SynthesizerRecipe] = recipes.filter(_.output.isItemEqual(item))

  def preInit(): Unit = {

  }

  def init(): Unit = {
    addRecipe(SynthesizerRecipe(WrapperVanillaItemStack(new ItemStack(FemtoItems.itemNanoweaveSheet, 4)), ArrayBuffer[ItemStackCraftingComponent](
      WrapperVanillaItemStack(new ItemStack(FemtoItems.itemNanoweaveThread, 9))), 400
    ))
  }

  def postInit(): Unit = {

  }
}
