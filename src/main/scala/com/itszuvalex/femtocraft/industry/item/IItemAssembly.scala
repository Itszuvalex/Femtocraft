package com.itszuvalex.femtocraft.industry.item

import com.itszuvalex.femtocraft.industry.tile.ITileAssemblyArray
import net.minecraft.item.ItemStack

/**
  * Created by Christopher Harris (Itszuvalex) on 2/23/2016.
  */
trait IItemAssembly {

  def getType(item: ItemStack): String

  def onTick(item: ItemStack, tile: ITileAssemblyArray): Unit

}
