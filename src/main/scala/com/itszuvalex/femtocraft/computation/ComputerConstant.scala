package com.itszuvalex.femtocraft.computation

import com.itszuvalex.femtocraft.api.computation.IComputer

class ComputerConstant(flopsFunc: () => Double) extends IComputer {
  override def FLOPsPerTick: Double = flopsFunc()

  override def useFLOPS(flops: Double): Unit = {}

  /**
   *
   * @return FLOPS / Energy cost per flop.  Higher efficiency computers are used before lower efficiency computers.
   *         This should be dynamic - so throttled computers should return lower if they use generate fewer flops for the same power.
   */
  override def efficiency: Double = Double.MaxValue
}
