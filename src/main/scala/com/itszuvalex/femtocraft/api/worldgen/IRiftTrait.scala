package com.itszuvalex.femtocraft.api.worldgen

import net.minecraft.item.ItemStack

trait IRiftTrait {

  def name: String

  def harvests: Map[ItemStack, Int]

  def genChance: Float

  def stabilityModifier: Int
}
