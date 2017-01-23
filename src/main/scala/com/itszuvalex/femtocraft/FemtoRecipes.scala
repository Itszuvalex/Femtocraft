package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.industry.{DustRecipeRegistry, SynthesizerRegistry}
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.ItemStack
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
    GameRegistry.addShapedRecipe(new ItemStack(FemtoItems.itemFrame, 4), Array("CIC", "I I", "CIC", 'C', FemtoBlocks.blockSubstrate, 'I', Items.IRON_INGOT): _*)
    GameRegistry.addShapedRecipe(new ItemStack(FemtoItems.itemNanoweaveSheet, 1), Array("TT", "TT", 'T', FemtoItems.itemNanoweaveThread): _*)
    GameRegistry.addShapedRecipe(new ItemStack(FemtoItems.itemBasicCircuit, 1), Array("NRN", "SIS", 'N', FemtoItems.itemNanoweaveThread, 'R', Items.REDSTONE, 'S', FemtoBlocks.blockSubstrate, 'I', Items.IRON_INGOT): _*)
    GameRegistry.addShapedRecipe(new ItemStack(FemtoBlocks.blockNanoFurnace, 1), Array("SSS", "CFC", "RMR",
      'S', FemtoItems.itemNanoweaveSheet, 'C', FemtoItems.itemBasicCircuit, 'F', Blocks.FURNACE, 'R', FemtoItems.itemRiftironIngotDevoid, 'M', FemtoItems.itemFrame
    ): _*)
    GameRegistry.addShapedRecipe(new ItemStack(FemtoBlocks.blockCrystalMount, 1), Array(" S ", "SFS", " S ", 'S', FemtoItems.itemNanoweaveSheet, 'F', FemtoItems.itemFrame): _ *)
    //    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(FemtoItems.itemFurnaceAssembly), Array[Any](" C ", "CFC", "III", 'C', "substrate", 'F', Blocks.FURNACE, 'I', "ingotIron").box: _*))
    //    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(FemtoItems.itemGrinderAssembly), Array[Any](" C ", "CPC", "III", 'C', "substrate", 'P', Blocks.PISTON, 'I', "ingotIron").box: _*))
  }

  def addSmeltingRecipes(): Unit = {
    FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(FemtoBlocks.blockRiftiron), new ItemStack(FemtoItems.itemRiftironIngotDevoid), .1f)
    FurnaceRecipes.instance().addSmelting(FemtoItems.itemRiftironDust, new ItemStack(FemtoItems.itemRiftironIngotDevoid), .1f)
    FurnaceRecipes.instance().addSmeltingRecipe(new ItemStack(FemtoBlocks.blockPhasemetal), new ItemStack(FemtoItems.itemPhasemetalIngotDevoid), .1f)
    FurnaceRecipes.instance().addSmelting(FemtoItems.itemPhasemetalDust, new ItemStack(FemtoItems.itemPhasemetalIngotDevoid), .1f)
    FurnaceRecipes.instance().addSmelting(FemtoItems.itemIronDust, new ItemStack(Items.IRON_INGOT), .1f)
    FurnaceRecipes.instance().addSmelting(FemtoItems.itemGoldDust, new ItemStack(Items.GOLD_INGOT), .1f)
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
