package com.itszuvalex.femtocraft.computation

import com.itszuvalex.femtocraft.api.computation.{FLOPS, IComputer}
import com.itszuvalex.femtocraft.api.power.DEPower
import com.itszuvalex.itszulib.api.storage.IBattery

class ComputerBatteryPowered(battery: IBattery, flopsFunc: () => FLOPS, costPerFlopFunc: () => DEPower) extends IComputer {
  override def FLOPSPerTick: FLOPS = math.min(flopsFunc(), costPerFlopFunc() * battery.storage)

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

  override def useFLOPS(flops: FLOPS): Unit = {
    battery.drain(flops * costPerFlopFunc())
  }
}
