package com.itszuvalex.femtocraft.computation

import com.itszuvalex.femtocraft.api.computation.IComputer

class DynamicIComputer(dynFunc: () => IComputer) extends IComputer {
  override def FLOPsPerTick: Double = dynFunc().FLOPsPerTick

  override def useFLOPS(flops: Double): Unit = dynFunc().useFLOPS(flops)

  /**
   *
   * @return FLOPS / Energy cost per flop.  Higher efficiency computers are used before lower efficiency computers.
   *         This should be dynamic - so throttled computers should return lower if they use generate fewer flops for the same power.
   */
  override def efficiency: Double = dynFunc().efficiency
}
