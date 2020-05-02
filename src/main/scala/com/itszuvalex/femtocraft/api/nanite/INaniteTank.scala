package com.itszuvalex.femtocraft.api.nanite

import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

object INaniteTank {
  val Empty = new INaniteTank {
    override def capacity: Int = 0

    override def amount: Int = 0

    override def fill(stack: INaniteStack, doFill: Boolean): INaniteStack = stack

    override def drain(stack: INaniteStack, doDrain: Boolean): INaniteStack = INaniteStack.Empty

    override def canFill(stack: INaniteStack): Boolean = false

    override def canDrain(stack: INaniteStack): Boolean = false

    override def containedNanites: Iterable[INaniteStack] = Set()

    /**
     *
     * @return True if this tank has specialized handling for nanites that are not equal.
     *         This could be a a tank that can store nanites of different strains but same archetype, or
     *         for example the Nanite Holding Tank that can upgrade nanites of the same archetype and strain, from a lower to higher version
     *         without destructive behavior.
     *
     *         These tanks should be prioritized when automation distributes resources.
     */
    override def supportsIntermingling: Boolean = false

    override def serializeNBT(): NBTTagCompound = new NBTTagCompound

    override def deserializeNBT(nbt: NBTTagCompound): Unit = {}
  }
}

trait INaniteTank extends INBTSerializable[NBTTagCompound] {

  def capacity: Int

  def amount: Int

  def room: Int = capacity - amount

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
