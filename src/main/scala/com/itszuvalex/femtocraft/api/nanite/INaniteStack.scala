package com.itszuvalex.femtocraft.api.nanite

object INaniteStack {
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
}

trait INaniteStack {
  def archetype: NaniteArchetype

  def strain: NaniteStrain

  def amount: Int

  def amount_=(amt: Int): Unit

  def amountMax: Int

  def version: NaniteStrainVersion

  def copy(): INaniteStack
}
