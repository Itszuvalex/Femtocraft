package com.itszuvalex.femtocraft.core.Power

import net.minecraft.item.{Item, ItemStack}

/**
 * Created by Christopher on 7/29/2015.
 */
trait IPowerCrystal extends Item {

  def getColor(stack: ItemStack): Int

  def getRange(stack: ItemStack) : Float

  def getPassiveGen(stack: ItemStack) : Float

  def getStorageMultiplier(stack : ItemStack) : Float

  def getSize(stack: ItemStack) : String

}
