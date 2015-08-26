package com.itszuvalex.femtocraft.core.Power

import net.minecraft.item.{Item, ItemStack}

/**
 * Created by Christopher on 7/29/2015.
 */
object IPowerCrystal {
  val TYPE_SMALL  = "small"
  val TYPE_MEDIUM = "medium"
  val TYPE_LARGE  = "large"
}

trait IPowerCrystal extends Item {

  def getName(stack: ItemStack): String

  /**
   *
   * @param stack
   * @return Color of the crystal.
   */
  def getColor(stack: ItemStack): Int

  /**
   *
   * @param stack
   * @return Range to allow connections in.
   */
  def getRange(stack: ItemStack): Float

  /**
   *
   * @param stack
   * @return Amount of power to generate per tick.
   */
  def getPassiveGen(stack: ItemStack): Float

  /**
   *
   * @param stack
   * @return Amount to multiply storage amount by.
   */
  def getStorageMultiplier(stack: ItemStack): Float

  /**
   *
   * @param stack
   * @return Size of the crystal.
   */
  def getType(stack: ItemStack): String

}
