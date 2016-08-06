package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.cyber.GrowthChamberRegistry
import com.itszuvalex.femtocraft.cyber.recipe.GrowthChamberRecipe
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
    registerGrowthChamberRecipes()
  }

  def registerVanillaRecipes() = {
    GameRegistry.addShapedRecipe(new ItemStack(FemtoItems.itemFrame, 4), Array("CIC", "I I", "CIC", 'C', FemtoBlocks.blockCyberweave, 'I', Items.IRON_INGOT): _*)
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(FemtoItems.itemFurnaceAssembly), Array[Any](" C ", "CFC", "III", 'C', "cyberweave", 'F', Blocks.FURNACE, 'I', "ingotIron").box: _*))
    GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(FemtoItems.itemGrinderAssembly), Array[Any](" C ", "CPC", "III", 'C', "cyberweave", 'P', Blocks.PISTON, 'I', "ingotIron").box: _*))
  }

  def registerGrowthChamberRecipes(): Unit = {
    GrowthChamberRegistry.addRecipe(new GrowthChamberRecipe(new ItemStack(Items.WHEAT_SEEDS, 1),
                                                            IndexedSeq(new ItemStack(Items.WHEAT_SEEDS, 2),
                                                                       new ItemStack(Items.WHEAT, 1)
                                                                      ),
                                                            500,
                                                            GrowthChamberRecipe.TYPE_TEXTURE,
                                                            Array(Resources.Texture("recipes/wheat0.png"),
                                                                  Resources.Texture("recipes/wheat1.png"),
                                                                  Resources.Texture("recipes/wheat2.png"),
                                                                  Resources.Texture("recipes/wheat3.png"),
                                                                  Resources.Texture("recipes/wheat4.png"),
                                                                  Resources.Texture("recipes/wheat5.png"),
                                                                  Resources.Texture("recipes/wheat6.png"),
                                                                  Resources.Texture("recipes/wheat7.png"))))
    DustRecipeRegistry.preInit()
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
