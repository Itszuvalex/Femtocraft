package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.industry._
import com.itszuvalex.itszulib.implicits.ItemStackImplicits._
import net.minecraft.init.Items
import net.minecraft.item.crafting.FurnaceRecipes

/**
  * Created by Christopher Harris (Itszuvalex) on 1/5/16.
  */
object FemtoRecipes {
  implicit def boxArray(array: Array[Any]): Array[Object] = array.map { case c: Char => c.asInstanceOf[Character]; case a: Object => a }

  def preInit(): Unit = {
    DustRecipeRegistry.preInit()
    SynthesizerRegistry.preInit()
  }

  def init(): Unit = {
    DustRecipeRegistry.init()
    SynthesizerRegistry.init()
    addSmeltingRecipes()
  }

  def addSmeltingRecipes(): Unit = {
    FurnaceRecipes.instance().addSmeltingRecipe(FemtoBlocks.blockRiftiron.newStack(), FemtoItems.itemRiftironIngotDevoid.newStack(), .1f)
    FurnaceRecipes.instance().addSmelting(FemtoItems.itemRiftironDust, FemtoItems.itemRiftironIngotDevoid.newStack(), .1f)
    FurnaceRecipes.instance().addSmeltingRecipe(FemtoBlocks.blockPhasemetal.newStack(), FemtoItems.itemPhasemetalIngotDevoid.newStack(), .1f)
    FurnaceRecipes.instance().addSmelting(FemtoItems.itemPhasemetalDust, FemtoItems.itemPhasemetalIngotDevoid.newStack(), .1f)
    FurnaceRecipes.instance().addSmelting(FemtoItems.itemIronDust, Items.IRON_INGOT.newStack(), .1f)
    FurnaceRecipes.instance().addSmelting(FemtoItems.itemGoldDust, Items.GOLD_INGOT.newStack(), .1f)
  }

  def postInit() = {
    DustRecipeRegistry.postInit()
    SynthesizerRegistry.postInit()
    NaniteInfusionRecipeRegistry.postInit()
    GerminationChamberRecipeRegistry.postInit()
    LiquifierRecipeRegistry.postInit()
  }
}
