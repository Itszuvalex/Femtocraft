package com.itszuvalex.femtocraft.util

import net.minecraft.item.ItemStack

/**
  * Created by Christopher Harris (Itszuvalex) on 2/1/2016.
  */
trait IItemFilter {

  def filterItem(item: ItemStack): Boolean

  def getFilterRules: scala.collection.mutable.Buffer[IItemFilterRule]

}
