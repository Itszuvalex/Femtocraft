package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.cyber.GrowthChamberRegistry
import com.itszuvalex.femtocraft.cyber.recipe.GrowthChamberRecipe
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.init.Items
import net.minecraft.item.ItemStack

/**
  * Created by Christopher Harris (Itszuvalex) on 1/5/16.
  */
object FemtoRecipes {
    implicit def boxChar(char: Char): Character = {
      new Character(char)
    }

    implicit def boxArray(array: Array[Any]): Array[Object] = array.map { case c: Char => c.asInstanceOf[Character]; case a: Object => a}

    implicit class boxedArray(array: Array[Any]) {
      def box: Array[Object] = array
    }

  def preInit(): Unit = {
    registerVanillaRecipes()
    registerGrowthChamberRecipes()
  }

  def registerVanillaRecipes() = {
    GameRegistry.addShapedRecipe(new ItemStack(FemtoItems.itemFrame), Array("CIC", "I I", "CIC", 'C', FemtoBlocks.blockCyberweave, 'I', Items.iron_ingot):_*)
  }

  def registerGrowthChamberRecipes(): Unit = {
    GrowthChamberRegistry.addRecipe(new GrowthChamberRecipe(new ItemStack(Items.wheat_seeds, 1),
                                                            IndexedSeq(new ItemStack(Items.wheat_seeds, 2),
                                                                       new ItemStack(Items.wheat, 1)
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
  }

  def init(): Unit = {

  }

  def postInit() = {

  }
}
