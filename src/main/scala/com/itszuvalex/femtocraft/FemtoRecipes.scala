package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.industry.DustRecipeRegistry
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.registry.GameRegistry

/**
  * Created by Christopher Harris (Itszuvalex) on 1/5/16.
  */
object FemtoRecipes {
  implicit def boxChar(char: Char): Character = {
    new Character(char)
  }

  implicit def boxArray(array: Array[Any]): Array[Object] = array.map { case c: Char => c.asInstanceOf[Character]; case a: Object => a }

  def preInit(): Unit = {
    registerVanillaRecipes()
    DustRecipeRegistry.preInit()
  }

  def registerVanillaRecipes() = {
    GameRegistry.addShapedRecipe(new ItemStack(FemtoItems.itemFrame, 4), Array("CIC", "I I", "CIC", 'C', FemtoBlocks.blockSubstrate, 'I', Items.IRON_INGOT): _*)
    GameRegistry.addShapedRecipe(new ItemStack(FemtoItems.itemNanoweaveSheet, 1), Array("TT", "TT", 'T', FemtoItems.itemNanoweaveThread): _*)
    //    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(FemtoItems.itemFurnaceAssembly), Array[Any](" C ", "CFC", "III", 'C', "substrate", 'F', Blocks.FURNACE, 'I', "ingotIron").box: _*))
    //    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(FemtoItems.itemGrinderAssembly), Array[Any](" C ", "CPC", "III", 'C', "substrate", 'P', Blocks.PISTON, 'I', "ingotIron").box: _*))
  }

  def init(): Unit = {
    DustRecipeRegistry.init()
  }

  def postInit() = {
    DustRecipeRegistry.postInit()
  }

  implicit class boxedArray(array: Array[Any]) {
    def box: Array[Object] = array
  }

}
