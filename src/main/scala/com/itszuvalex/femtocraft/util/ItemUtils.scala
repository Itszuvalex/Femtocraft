package com.itszuvalex.femtocraft.util

import com.itszuvalex.femtocraft.FemtoItems
import com.itszuvalex.femtocraft.industry.item.ItemMultiblock
import net.minecraft.item.ItemStack

/**
  * Created by Christopher Harris (Itszuvalex) on 2/18/2016.
  */
object ItemUtils {
  def makeMultiblockItem(multiblock: String): ItemStack = {
    var ret = false
    val itemStack = new ItemStack(FemtoItems.itemMultiblock)
    itemStack match {
      case null =>
      case i =>
        i.getItem match {
          case item: ItemMultiblock =>
            ret = true
            item.setMultiblock(itemStack, multiblock)
          case _ =>
        }
    }
    if (ret) itemStack
    else null
  }
}
