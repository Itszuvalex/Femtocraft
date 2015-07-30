package com.itszuvalex.femtocraft.core.Power

import net.minecraft.item.{Item, ItemStack}

/**
 * Created by Christopher on 7/29/2015.
 */
trait IPowerCrystal extends Item {

  def getColor(stack: ItemStack): Int

}
