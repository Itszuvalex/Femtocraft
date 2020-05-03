package com.itszuvalex.femtocraft.computation

import com.itszuvalex.femtocraft.api.computation.IComputer
import com.itszuvalex.itszulib.api.storage.IBattery

class ComputerBatteryPowered(battery: IBattery, flopsFunc: () => Double, costPerFlopFunc: () => Double) extends IComputer {
  override def FLOPsPerTick: Double = math.min(flopsFunc(), costPerFlopFunc() * battery.storage)

  override def useFLOPS(flops: Double): Unit = {
    battery.drain(flops * costPerFlopFunc())
  }
}
