package com.itszuvalex.femtocraft.industry

import java.util
import java.util.Comparator

import com.itszuvalex.femtocraft.core.Initializable
import com.itszuvalex.femtocraft.industry.recipes.CubicCraftingRecipe
import com.itszuvalex.itszulib.util.Comparators.FluidStack.IDNBTComparator
import com.itszuvalex.itszulib.util.Comparators.ItemStack.IDDamageWildCardNBTComparator
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidStack

import scala.collection.JavaConverters._

/**
 * Created by Christopher on 8/24/2015.
 */
object CubicCraftingRegistry extends Initializable {
  private val recipeInputMap = new util.TreeMap[(Array[Array[ItemStack]], FluidStack), CubicCraftingRecipe](new Comparator[(Array[Array[ItemStack]], FluidStack)] {
    override def compare(o1: (Array[Array[ItemStack]], FluidStack), o2: (Array[Array[ItemStack]], FluidStack)): Int = {
      if (o1._1.length < o2._1.length) return -1
      if (o1._1.length > o2._1.length) return 1

      (o1._1 zip o2._1).foreach { pair =>
        if (pair._1.length < pair._2.length) return -1
        if (pair._1.length > pair._2.length) return 1
                                }
      (o1._1 zip o2._1).foreach { pair =>
        (pair._1 zip pair._2).foreach { items =>
          IDDamageWildCardNBTComparator.compare(items._1, items._2) match {
            case 0 =>
            case r => return r
          }
                                      }
                                }
      IDNBTComparator.compare(o1._2, o2._2)
    }
  }).asScala

  def addRecipe(recipe: CubicCraftingRecipe): Unit = {
    if (recipe == null) return
    if (recipe.inputItems == null) return
    val size = recipe.inputItems.length
    if (!recipe.inputItems.forall(a => a != null && a.length == size)) return
    recipeInputMap.put((recipe.inputItems, recipe.inputFluid), recipe)
  }

  def findMatchingRecipe(input: (Array[Array[ItemStack]], FluidStack)): Option[CubicCraftingRecipe] = recipeInputMap.get(input)

  def findMatchingRecipe(inputItems: Array[Array[ItemStack]], inputFluids: FluidStack): Option[CubicCraftingRecipe] = findMatchingRecipe((inputItems, inputFluids))

  def getAllRecipes = recipeInputMap.values
}
