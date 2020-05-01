package com.itszuvalex.femtocraft.api.nanite

import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

trait INaniteTank extends INBTSerializable[NBTTagCompound] {

  def capacity: Int

  def fill(stack: INaniteStack, doFill: Boolean): INaniteStack

  def drain(stack: INaniteStack, doDrain: Boolean): INaniteStack

  def canFill(stack: INaniteStack): Boolean

  def canDrain(stack: INaniteStack): Boolean

  def containedNanites: Iterable[INaniteStack]

  /**
   *
   * @return True if this tank has specialized handling for nanites that are not equal.
   *         This could be a a tank that can store nanites of different strains but same archetype, or
   *         for example the Nanite Holding Tank that can upgrade nanites of the same archetype and strain, from a lower to higher version
   *         without destructive behavior.
   *
   *         These tanks should be prioritized when automation distributes resources.
   */
  def supportsIntermingling: Boolean
}
