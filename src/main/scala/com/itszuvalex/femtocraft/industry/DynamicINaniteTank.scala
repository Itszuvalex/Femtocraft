package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.api.nanite.{INaniteStack, INaniteTank}
import net.minecraft.nbt.NBTTagCompound

class DynamicINaniteTank(val getter: () => INaniteTank) extends INaniteTank {
  override def capacity: Int = getter().capacity

  override def amount: Int = getter().amount

  override def fill(stack: INaniteStack, doFill: Boolean): INaniteStack = getter().fill(stack, doFill)

  override def drain(stack: INaniteStack, doDrain: Boolean): INaniteStack = getter().drain(stack, doDrain)

  override def canFill(stack: INaniteStack): Boolean = getter().canFill(stack)

  override def canDrain(stack: INaniteStack): Boolean = getter().canDrain((stack))

  override def containedNanites: Iterable[INaniteStack] = getter().containedNanites

  /**
   *
   * @return True if this tank has specialized handling for nanites that are not equal.
   *         This could be a a tank that can store nanites of different strains but same archetype, or
   *         for example the Nanite Holding Tank that can upgrade nanites of the same archetype and strain, from a lower to higher version
   *         without destructive behavior.
   *
   *         These tanks should be prioritized when automation distributes resources.
   */
  override def supportsIntermingling: Boolean = getter().supportsIntermingling

  override def serializeNBT(): NBTTagCompound = getter().serializeNBT()

  override def deserializeNBT(nbt: NBTTagCompound): Unit = getter().deserializeNBT(nbt)
}
