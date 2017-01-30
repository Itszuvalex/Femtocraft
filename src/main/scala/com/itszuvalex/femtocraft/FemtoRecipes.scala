package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.industry.{DustRecipeRegistry, SynthesizerRegistry}
import com.itszuvalex.itszulib.implicits.ItemStackImplicits._
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.crafting.FurnaceRecipes
import net.minecraftforge.fml.common.registry.GameRegistry

/**
  * Created by Christopher Harris (Itszuvalex) on 1/5/16.
  */
object FemtoRecipes {
  implicit def boxArray(array: Array[Any]): Array[Object] = array.map { case c: Char => c.asInstanceOf[Character]; case a: Object => a }

  def preInit(): Unit = {
    registerVanillaRecipes()
    DustRecipeRegistry.preInit()
    SynthesizerRegistry.preInit()
    addSmeltingRecipes()
  }

  def registerVanillaRecipes() = {
    GameRegistry.addShapedRecipe(FemtoItems.itemFrame.newStack(), Array("CIC", "I I", "CIC", 'C', FemtoBlocks.blockSubstrate, 'I', Items.IRON_INGOT): _*)
    GameRegistry.addShapedRecipe(FemtoItems.itemNanoweaveSheet.newStack(), Array("TT", "TT", 'T', FemtoItems.itemNanoweaveThread): _*)
    GameRegistry.addShapedRecipe(FemtoItems.itemBasicCircuit.newStack(), Array("NRN", "SIS", 'N', FemtoItems.itemNanoweaveThread, 'R', Items.REDSTONE, 'S', FemtoBlocks.blockSubstrate, 'I', Items.IRON_INGOT): _*)
    GameRegistry.addShapedRecipe(FemtoBlocks.blockNanoFurnace.newStack(), Array("SSS", "CFC", "RMR",
      'S', FemtoItems.itemNanoweaveSheet, 'C', FemtoItems.itemBasicCircuit, 'F', Blocks.FURNACE, 'R', FemtoItems.itemRiftironIngotDevoid, 'M', FemtoItems.itemFrame
    ): _*)
    GameRegistry.addShapedRecipe(FemtoBlocks.blockCrystalMount.newStack(), Array("CSC", "SFS", "CSC", 'S', FemtoItems.itemNanoweaveSheet, 'F', FemtoItems.itemFrame, 'C', FemtoItems.itemBasicCircuit): _ *)
  }

  def addSmeltingRecipes(): Unit = {
    FurnaceRecipes.instance().addSmeltingRecipe(FemtoBlocks.blockRiftiron.newStack(), FemtoItems.itemRiftironIngotDevoid.newStack(), .1f)
    FurnaceRecipes.instance().addSmelting(FemtoItems.itemRiftironDust, FemtoItems.itemRiftironIngotDevoid.newStack(), .1f)
    FurnaceRecipes.instance().addSmeltingRecipe(FemtoBlocks.blockPhasemetal.newStack(), FemtoItems.itemPhasemetalIngotDevoid.newStack(), .1f)
    FurnaceRecipes.instance().addSmelting(FemtoItems.itemPhasemetalDust, FemtoItems.itemPhasemetalIngotDevoid.newStack(), .1f)
    FurnaceRecipes.instance().addSmelting(FemtoItems.itemIronDust, Items.IRON_INGOT.newStack(), .1f)
    FurnaceRecipes.instance().addSmelting(FemtoItems.itemGoldDust, Items.GOLD_INGOT.newStack(), .1f)
  }

  def init(): Unit = {
    DustRecipeRegistry.init()
    SynthesizerRegistry.init()
  }

  def postInit() = {
    DustRecipeRegistry.postInit()
    SynthesizerRegistry.postInit()
  }
}
