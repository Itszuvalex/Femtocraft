package com.itszuvalex.femtocraft.api.nanite

import net.minecraft.nbt.NBTTagCompound

import scala.collection.mutable.ArrayBuffer

object NaniteTank {
  val SIZE_NBT = "Size"
  val NANITES_NBT = "Nanites"
}

class NaniteTank(private val cap: Int) extends INaniteTank {
  private val nanites = ArrayBuffer[INaniteStack]()

  override def capacity: Int = cap

  override def amount: Int = nanites.map(_.amount).sum

  override def fill(stack: INaniteStack, doFill: Boolean): INaniteStack = {
    val copy      = stack.copy()
    val amtToFill = math.min(stack.amount, room)
    copy.amount -= amtToFill
    if (doFill) {
      findMatchingNanite(stack) match {
        case None => // TODO: Destructive add
          val toFill = stack.copy()
          toFill.amount = amtToFill
          nanites += toFill
        case Some(s) => s.amount += amtToFill
      }
    }
    copy
  }

  override def drain(stack: INaniteStack, doDrain: Boolean): INaniteStack = {
    var copy = INaniteStack.Empty
    findMatchingNanite(stack) match {
      case None =>
      case Some(s) =>
        val amtToDrain = math.min(s.amount, stack.amount)
        copy = s.copy()
        copy.amount = amtToDrain
        if (doDrain) {
          s.amount -= amtToDrain
          if (s.amount <= 0) {
            nanites -= s
          }
        }
    }
    copy
  }

  override def canFill(stack: INaniteStack): Boolean = true

  override def canDrain(stack: INaniteStack): Boolean = findMatchingNanite(stack).nonEmpty

  override def containedNanites: Iterable[INaniteStack] = nanites

  def findMatchingNanite(stack: INaniteStack): Option[INaniteStack] = nanites.find(INaniteStack.areNaniteStacksEqual(_, stack))


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

  override def serializeNBT(): NBTTagCompound = {
    val nbt = new NBTTagCompound
    nbt.setInteger(NaniteTank.SIZE_NBT, nanites.length)
    nanites.zipWithIndex.foreach(x => nbt.setTag(x._2.toString, x._1.serializeNBT()))
    nbt
  }

  override def deserializeNBT(nbt: NBTTagCompound): Unit = {
    nanites.clear()
    val size = nbt.getInteger(NaniteTank.SIZE_NBT)
    (0 until size).foreach(i => nanites += INaniteStack.deserializeNaniteStack(nbt.getCompoundTag(i.toString)))
  }
}
