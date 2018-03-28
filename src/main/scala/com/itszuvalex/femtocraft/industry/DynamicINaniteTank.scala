package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.api.nanite.{INanite, INaniteTank, NaniteStack}

class DynamicINaniteTank(val getter: () => INaniteTank) extends INaniteTank {
  override def volume: Int = getter().volume

  override def volumeFilled: Int = getter().volumeFilled

  override def nMols: Int = getter().nMols

  override def canDrain(nanite: INanite, vol: Int): Boolean = getter().canDrain(nanite, vol)

  override def canFill(nanite: INanite, vol: Int): Boolean = getter().canFill(nanite, vol)

  override def containsNanite(nanite: INanite): Boolean = getter().containsNanite(nanite)

  override def volForNanite(nanite: INanite): Int = getter().volForNanite(nanite)

  override def nanitesInTank: Iterable[INanite] = getter().nanitesInTank

  /**
    *
    * @param nanite  Nanite to drain
    * @param vol     Volume to drain
    * @param doDrain True to actually modify the tank
    *
    * @return Stack containing the results of the drain
    */
  override def drain(nanite: INanite, vol: Int, doDrain: Boolean): NaniteStack = getter().drain(nanite, vol, doDrain)

  /**
    *
    * @param stack  Stack to fill.  This is not modified.
    * @param doFill True to actually modify the tank
    *
    * @return Copy of NaniteStack containing the remainder, or null
    */
  override def fill(stack: NaniteStack, doFill: Boolean): NaniteStack = getter().fill(stack, doFill)
}
