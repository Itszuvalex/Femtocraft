package com.itszuvalex.femtocraft.industry.item

import com.itszuvalex.femtocraft.power.tile.ITilePower
import net.minecraft.item.ItemStack

/**
  * Created by Christopher Harris (Itszuvalex) on 2/23/2016.
  */
trait IItemAssembly {

  def getType(item: ItemStack): String

  def onTick(item: ItemStack, tile: ITilePower): Unit

}
