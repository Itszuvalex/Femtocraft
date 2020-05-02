package com.itszuvalex.femtocraft.api.nanite

import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

object INaniteStack {
  val Empty: INaniteStack = new INaniteStack {

    override def archetype: NaniteArchetype = NaniteArchetype.Empty

    override def strain: NaniteStrain = NaniteStrain.Empty

    override def amount: Int = 0

    override def amount_=(amt: Int): Unit = {}

    override def amountMax: Int = 0

    override def version: NaniteStrainVersion = NaniteStrainVersion(0, 0)

    override def copy(): INaniteStack = Empty

    override def serializeNBT(): NBTTagCompound = new NBTTagCompound

    override def deserializeNBT(nbt: NBTTagCompound): Unit = {}
  }

  def areNaniteStacksEqual(stack1: INaniteStack, stack2: INaniteStack): Boolean = {
    String.CASE_INSENSITIVE_ORDER.compare(stack1.archetype.name, stack2.archetype.name) match {
      case 0 =>
        String.CASE_INSENSITIVE_ORDER.compare(stack1.strain.name, stack2.strain.name) match {
          case 0 =>
            stack1.version.compareTo(stack2.version) == 0
          case _ => false
        }
      case _ => false
    }
  }

  def areNaniteStacksSameStrain(stack1: INaniteStack, stack2: INaniteStack): Boolean = {
    String.CASE_INSENSITIVE_ORDER.compare(stack1.archetype.name, stack2.archetype.name) match {
      case 0 =>
        String.CASE_INSENSITIVE_ORDER.compare(stack1.strain.name, stack2.strain.name) == 0
      case _ => false
    }
  }

  def deserializeNaniteStack(tag: NBTTagCompound): INaniteStack = NaniteStack.deserializeNaniteStack(tag)
}

trait INaniteStack extends INBTSerializable[NBTTagCompound] {
  def archetype: NaniteArchetype

  def strain: NaniteStrain

  def amount: Int

  def amount_=(amt: Int): Unit

  def amountMax: Int

  def version: NaniteStrainVersion

  def copy(): INaniteStack
}
