package com.itszuvalex.femtocraft.computation

import com.itszuvalex.femtocraft.api.computation.IComputer
import com.itszuvalex.itszulib.api.storage.IBattery

class ComputerBatteryPowered(battery: IBattery, flopsFunc: () => Double, costPerFlopFunc: () => Double) extends IComputer {
  override def FLOPsPerTick: Double = math.min(flopsFunc(), costPerFlopFunc() * battery.storage)

  /**
   *
   * @return FLOPS / Energy cost per flop.  Higher efficiency computers are used before lower efficiency computers.
   *         This should be dynamic - so throttled computers should return lower if they use generate fewer flops for the same power.
   */
  override def efficiency: Double = {
    val costPerFlop = costPerFlopFunc()
    if (costPerFlop > 0) {
      1 / costPerFlop
    }
    else Double.MaxValue
  }

  override def useFLOPS(flops: Double): Unit = {
    battery.drain(flops * costPerFlopFunc())

  }
}
