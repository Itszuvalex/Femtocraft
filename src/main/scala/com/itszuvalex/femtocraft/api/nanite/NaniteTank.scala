package com.itszuvalex.femtocraft.api.nanite

import net.minecraft.nbt.NBTTagCompound

import scala.collection.mutable.ArrayBuffer

class NaniteTank(private val cap: Int) extends INaniteTank {
  private val nanite = ArrayBuffer[INaniteStack]()

  override def capacity: Int = cap

  override def fill(stack: INaniteStack, doFill: Boolean): INaniteStack = ???

  override def drain(stack: INaniteStack, doDrain: Boolean): INaniteStack = ???

  override def canFill(stack: INaniteStack): Boolean = ???

  override def canDrain(stack: INaniteStack): Boolean = ???

  override def containedNanites: Iterable[INaniteStack] = ???


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

  override def serializeNBT(): NBTTagCompound = ???

  override def deserializeNBT(nbt: NBTTagCompound): Unit = ???
}
