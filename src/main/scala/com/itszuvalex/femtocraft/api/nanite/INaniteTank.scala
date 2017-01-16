package com.itszuvalex.femtocraft.api.nanite

import com.itszuvalex.femtocraft.nanite.{INanite, NaniteStack}

/**
  * Created by Chris on 1/15/2017.
  */
trait INaniteTank {

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
    *
    * @return Stack containing the results of the drain
    */
  def drain(nanite: INanite, vol: Int, doDrain: Boolean): NaniteStack

  /**
    *
    * @param stack  Stack to fill.  This is not modified.
    * @param doFill True to actually modify the tank
    *
    * @return Copy of NaniteStack containing the remainder, or null
    */
  def fill(stack: NaniteStack, doFill: Boolean): NaniteStack
}
