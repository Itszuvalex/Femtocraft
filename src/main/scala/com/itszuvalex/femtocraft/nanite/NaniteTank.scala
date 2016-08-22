package com.itszuvalex.femtocraft.nanite

import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

import scala.collection.mutable.ArrayBuffer

/**
  * Created by Chris on 8/18/2016.
  */
class NaniteTank(private var vol: Int) extends INBTSerializable[NBTTagCompound] {
  private val nanites = ArrayBuffer[NaniteStack]()

  def volume = vol

  def volumeFilled = nanites.map(_.volume).sum

  def nMols = nanites.map(_.nMol).sum

  def canDrain(nanite: INanite, vol: Int): Boolean = nanites.exists(_.nanite == nanite)

  def canFill(nanite: INanite, vol: Int): Boolean = true

  def containsNanite(nanite: INanite): Boolean = findStack(nanite).isDefined

  def volForNanite(nanite: INanite): Int = findStack(nanite).map(ind => nanites(ind).vol).getOrElse(0)

  /**
    *
    * @param nanite  Nanite to drain
    * @param vol     Volume to drain
    * @param doDrain True to actually modify the tank
    *
    * @return Stack containing the results of the drain
    */
  def drain(nanite: INanite, vol: Int, doDrain: Boolean): NaniteStack = {
    if (nanite == null) return null

    findStack(nanite).map { index =>
      val stack = nanites(index)
      val lowest = Math.min(vol, stack.volume)
      if (doDrain) {
        stack.vol -= lowest
        if (stack.volume <= 0)
          nanites(index) = null
      }
      NaniteStack(nanite, lowest)
    }.orNull
  }

  /**
    *
    * @param stack  Stack to fill.  This is not modified.
    * @param doFill True to actually modify the tank
    *
    * @return Copy of NaniteStack containing the remainder, or null
    */
  def fill(stack: NaniteStack, doFill: Boolean): NaniteStack = {
    if (stack.nanite == null) return stack

    val room = volume - volumeFilled
    if (room <= 0) return null
    if (stack.volume <= 0) return null

    val storageStack =
      findStack(stack.nanite).map(nanites(_)).
        getOrElse {
          val add = NaniteStack(stack.nanite, 0)
          if (doFill) {
            nanites += add
          }
          add
        }

    val lowest = Math.min(stack.volume, room)
    if (doFill) {
      storageStack.vol += lowest
    }

    if (stack.volume - lowest <= 0)
      null
    else
      NaniteStack(stack.nanite, stack.volume - lowest)
  }

  private def findStack(nanite: INanite): Option[Int] = {
    nanites.zipWithIndex.foreach { case (stack: NaniteStack, i: Int) =>
      if (stack.nanite == nanite)
        return Some(i)
    }
    None
  }

  override def deserializeNBT(nbt: NBTTagCompound): Unit = {
    nanites.clear()
    vol = nbt.getInteger("vol")
    val size = nbt.getInteger("size")
    nanites.sizeHint(size)
    (0 until size).forall { i =>
      if (nbt.hasKey(i.toString)) {
        nanites += NaniteStack.loadFromNBT(nbt.getCompoundTag(i.toString))
        true
      }
      else false
    }
  }

  override def serializeNBT(): NBTTagCompound = {
    val nbt = new NBTTagCompound()
    nbt.setInteger("vol", vol)
    nbt.setInteger("size", nanites.size)
    nanites.zipWithIndex.view.filterNot { case (a, b) => a == null }.foreach { case (a, b) =>
      nbt.setTag(b.toString, a.serializeNBT())
    }
    nbt
  }
}
