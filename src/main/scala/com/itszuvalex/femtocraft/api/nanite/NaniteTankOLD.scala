package com.itszuvalex.femtocraft.api.nanite

import net.minecraft.nbt.NBTTagCompound

import scala.collection.mutable.ArrayBuffer

/**
  * Created by Chris on 8/18/2016.
  */
class NaniteTankOLD(private var vol: Int) extends INaniteTankOLD {
  private val nanites = ArrayBuffer[NaniteStackOLD]()

  override def nMols: Int = nanites.map(_.nMol).sum

  override def canDrain(nanite: INaniteOLD, vol: Int): Boolean = nanites.exists(_.nanite == nanite)

  override def canFill(nanite: INaniteOLD, vol: Int): Boolean = true

  override def containsNanite(nanite: INaniteOLD): Boolean = findStack(nanite).isDefined

  override def volForNanite(nanite: INaniteOLD): Int = findStack(nanite).map(ind => nanites(ind).vol).getOrElse(0)

  override def nanitesInTank: Iterable[INaniteOLD] = nanites.map(_.nanite)

  def copy(): NaniteTankOLD = {
    val ret = new NaniteTankOLD(vol)
    ret.deserializeNBT(serializeNBT())
    ret
  }

  override def deserializeNBT(nbt: NBTTagCompound): Unit = {
    nanites.clear()
    vol = nbt.getInteger("vol")
    val size = nbt.getInteger("size")
    nanites.sizeHint(size)
    (0 until size).forall { i =>
      if (nbt.hasKey(i.toString)) {
        nanites += NaniteStackOLD.loadFromNBT(nbt.getCompoundTag(i.toString))
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

  /**
    *
    * @param nanite  Nanite to drain
    * @param vol     Volume to drain
    * @param doDrain True to actually modify the tank
    * @return Stack containing the results of the drain
    */
  override def drain(nanite: INaniteOLD, vol: Int, doDrain: Boolean): NaniteStackOLD = {
    if (nanite == null) return null

    findStack(nanite).map { index =>
      val stack  = nanites(index)
      val lowest = Math.min(vol, stack.volume)
      if (doDrain) {
        stack.vol -= lowest
        if (stack.volume <= 0)
          nanites.remove(index)
      }
      NaniteStackOLD(nanite, lowest)
    }.orNull
  }

  /**
    *
    * @param stack  Stack to fill.  This is not modified.
    * @param doFill True to actually modify the tank
    * @return Copy of NaniteStack containing the remainder, or null
    */
  override def fill(stack: NaniteStackOLD, doFill: Boolean): NaniteStackOLD = {
    if (stack.nanite == null) return stack

    val room = volume - volumeFilled
    if (room <= 0) return stack
    if (stack.volume <= 0) return null

    val storageStack =
      findStack(stack.nanite).map(nanites(_)).
                             getOrElse {
                               val add = NaniteStackOLD(stack.nanite, 0)
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
      NaniteStackOLD(stack.nanite, stack.volume - lowest)
  }

  override def volume: Int = vol

  override def volumeFilled: Int = nanites.map(_.volume).sum

  private def findStack(nanite: INaniteOLD): Option[Int] = {
    nanites.zipWithIndex.foreach { case (stack: NaniteStackOLD, i: Int) =>
      if (stack.nanite == nanite)
        return Some(i)
    }
    None
  }
}
