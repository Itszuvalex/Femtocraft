package com.itszuvalex.femtocraft.computation

import com.itszuvalex.femtocraft.api.computation.{FLOPS, IComputer}

class ComputerConstant(flopsFunc: () => FLOPS) extends IComputer {
  override def FLOPSPerTick: FLOPS = flopsFunc()

  override def useFLOPS(flops: FLOPS): Unit = {}

  /**
   *
   * @return FLOPS / Energy cost per flop.  Higher efficiency computers are used before lower efficiency computers.
   *         This should be dynamic - so throttled computers should return lower if they use generate fewer flops for the same power.
   */
  override def efficiency: Double = Double.MaxValue
}
