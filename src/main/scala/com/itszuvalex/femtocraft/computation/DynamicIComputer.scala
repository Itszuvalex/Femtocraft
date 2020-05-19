package com.itszuvalex.femtocraft.computation

import com.itszuvalex.femtocraft.api.computation.{FLOPS, IComputer}

class DynamicIComputer(dynFunc: () => IComputer) extends IComputer {
  override def FLOPSPerTick: FLOPS = dynFunc().FLOPSPerTick

  override def useFLOPS(flops: FLOPS): Unit = dynFunc().useFLOPS(flops)

  /**
   *
   * @return FLOPS / Energy cost per flop.  Higher efficiency computers are used before lower efficiency computers.
   *         This should be dynamic - so throttled computers should return lower if they use generate fewer flops for the same power.
   */
  override def efficiency: Double = dynFunc().efficiency
}
