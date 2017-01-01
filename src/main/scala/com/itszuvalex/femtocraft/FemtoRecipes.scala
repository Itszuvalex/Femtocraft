package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.industry.DustRecipeRegistry
import net.minecraft.init.{Blocks, Items}
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.oredict.ShapedOreRecipe

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
    GameRegistry.addShapedRecipe(new ItemStack(FemtoItems.itemFrame, 4), Array("CIC", "I I", "CIC", 'C', FemtoBlocks.blockCyberweave, 'I', Items.IRON_INGOT): _*)
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(FemtoItems.itemFurnaceAssembly), Array[Any](" C ", "CFC", "III", 'C', "cyberweave", 'F', Blocks.FURNACE, 'I', "ingotIron").box: _*))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(FemtoItems.itemGrinderAssembly), Array[Any](" C ", "CPC", "III", 'C', "cyberweave", 'P', Blocks.PISTON, 'I', "ingotIron").box: _*))
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
