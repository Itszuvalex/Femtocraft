package com.itszuvalex.femtocraft.computation

import com.itszuvalex.femtocraft.api.computation.IComputer

class ComputerConstant(flopsFunc: () => Double) extends IComputer {
  override def FLOPsPerTick: Double = flopsFunc()

  override def useFLOPS(flops: Double): Unit = {}
}
