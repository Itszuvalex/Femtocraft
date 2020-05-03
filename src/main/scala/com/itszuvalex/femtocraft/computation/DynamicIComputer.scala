package com.itszuvalex.femtocraft.computation

import com.itszuvalex.femtocraft.api.computation.IComputer

class DynamicIComputer(dynFunc: () => IComputer) extends IComputer {
  override def FLOPsPerTick: Double = dynFunc().FLOPsPerTick

  override def useFLOPS(flops: Double): Unit = dynFunc().useFLOPS(flops)
}
