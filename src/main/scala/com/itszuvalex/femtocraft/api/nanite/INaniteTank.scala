package com.itszuvalex.femtocraft.api.nanite

import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

/**
  * Created by Chris on 1/15/2017.
  */
object INaniteTank {
  val Empty: INaniteTank = new INaniteTank {
    override def volume: Int = 0

    override def volumeFilled: Int = 0

    override def nMols: Int = 0

    override def canDrain(nanite: INanite, vol: Int): Boolean = false

    override def canFill(nanite: INanite, vol: Int): Boolean = false

    override def containsNanite(nanite: INanite): Boolean = false

    override def volForNanite(nanite: INanite): Int = 0

    override def nanitesInTank: Iterable[INanite] = List.empty

    /**
      *
      * @param nanite  Nanite to drain
      * @param vol     Volume to drain
      * @param doDrain True to actually modify the tank
      * @return Stack containing the results of the drain
      */
    override def drain(nanite: INanite, vol: Int, doDrain: Boolean): NaniteStack = null

    /**
      *
      * @param stack  Stack to fill.  This is not modified.
      * @param doFill True to actually modify the tank
      * @return Copy of NaniteStack containing the remainder, or null
      */
    override def fill(stack: NaniteStack, doFill: Boolean): NaniteStack = null

    override def serializeNBT(): NBTTagCompound = {new NBTTagCompound}

    override def deserializeNBT(nbt: NBTTagCompound): Unit = {}

    override def copy(): INaniteTank = INaniteTank.Empty
  }
}

trait INaniteTank extends INBTSerializable[NBTTagCompound] {

  def volume: Int

  def volumeFilled: Int

  def nMols: Int

  def canDrain(nanite: INanite, vol: Int): Boolean

  def canFill(nanite: INanite, vol: Int): Boolean

  def containsNanite(nanite: INanite): Boolean

  def volForNanite(nanite: INanite): Int

  def nanitesInTank: Iterable[INanite]

  /**
    *
    * @param nanite  Nanite to drain
    * @param vol     Volume to drain
    * @param doDrain True to actually modify the tank
    * @return Stack containing the results of the drain
    */
  def drain(nanite: INanite, vol: Int, doDrain: Boolean): NaniteStack

  /**
    *
    * @param stack  Stack to fill.  This is not modified.
    * @param doFill True to actually modify the tank
    * @return Copy of NaniteStack containing the remainder, or null
    */
  def fill(stack: NaniteStack, doFill: Boolean): NaniteStack

  def copy(): INaniteTank
}
