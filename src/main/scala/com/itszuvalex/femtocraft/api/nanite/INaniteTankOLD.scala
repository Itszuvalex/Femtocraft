package com.itszuvalex.femtocraft.api.nanite

import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

/**
  * Created by Chris on 1/15/2017.
  */
object INaniteTankOLD {
  val Empty: INaniteTankOLD = new INaniteTankOLD {
    override def volume: Int = 0

    override def volumeFilled: Int = 0

    override def nMols: Int = 0

    override def canDrain(nanite: INaniteOLD, vol: Int): Boolean = false

    override def canFill(nanite: INaniteOLD, vol: Int): Boolean = false

    override def containsNanite(nanite: INaniteOLD): Boolean = false

    override def volForNanite(nanite: INaniteOLD): Int = 0

    override def nanitesInTank: Iterable[INaniteOLD] = List.empty

    /**
      *
      * @param nanite  Nanite to drain
      * @param vol     Volume to drain
      * @param doDrain True to actually modify the tank
      * @return Stack containing the results of the drain
      */
    override def drain(nanite: INaniteOLD, vol: Int, doDrain: Boolean): NaniteStackOLD = null

    /**
      *
      * @param stack  Stack to fill.  This is not modified.
      * @param doFill True to actually modify the tank
      * @return Copy of NaniteStack containing the remainder, or null
      */
    override def fill(stack: NaniteStackOLD, doFill: Boolean): NaniteStackOLD = null

    override def serializeNBT(): NBTTagCompound = {new NBTTagCompound}

    override def deserializeNBT(nbt: NBTTagCompound): Unit = {}

    override def copy(): INaniteTankOLD = INaniteTankOLD.Empty
  }
}

trait INaniteTankOLD extends INBTSerializable[NBTTagCompound] {

  def volume: Int

  def volumeFilled: Int

  def nMols: Int

  def canDrain(nanite: INaniteOLD, vol: Int): Boolean

  def canFill(nanite: INaniteOLD, vol: Int): Boolean

  def containsNanite(nanite: INaniteOLD): Boolean

  def volForNanite(nanite: INaniteOLD): Int

  def nanitesInTank: Iterable[INaniteOLD]

  /**
    *
    * @param nanite  Nanite to drain
    * @param vol     Volume to drain
    * @param doDrain True to actually modify the tank
    * @return Stack containing the results of the drain
    */
  def drain(nanite: INaniteOLD, vol: Int, doDrain: Boolean): NaniteStackOLD

  /**
    *
    * @param stack  Stack to fill.  This is not modified.
    * @param doFill True to actually modify the tank
    * @return Copy of NaniteStack containing the remainder, or null
    */
  def fill(stack: NaniteStackOLD, doFill: Boolean): NaniteStackOLD

  def copy(): INaniteTankOLD
}
