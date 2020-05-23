package com.itszuvalex.femtocraft.api.computation

import com.itszuvalex.itszulib.api.storage.IBattery

class MultiProcessorComputer(val battery: IBattery, val heat: IHeatStorage, val processors: Seq[IProcessor]) extends IComputer {


  override def generateFLOPS(): FLOPS = {
    // Do not cache power as they can run out of power mid-tick.
    val temp  = heat.temperature // Cache temperature just because it's easier and I don't want processors to throttle mid-tick
    var flops = 0d

    // Always use most efficient processor first.
    processors.sortWith(_.efficiency(temp) < _.efficiency(temp)).foreach { p =>
      if (p.canTick(temp, battery.storage)) {
        val result = p.tick(temp, battery.storage, simulate = false)
        battery.drain(result.power)
        heat.addEnergy(result.energy)
        flops += result.flops
      }
    }
    flops
  }

  /**
   *
   * @return FLOPS / Power cost per flop.  Higher efficiency computers are used before lower efficiency computers.
   *         This should be dynamic - so throttled computers should return lower if they use generate fewer flops for the same power.
   */
  override def efficiency: Double = {
    // Do not cache power as they can run out of power mid-tick.
    val temp    = heat.temperature // Cache temperature just because it's easier and I don't want processors to throttle mid-tick
    var storage = battery.storage

    // Always use most efficient processor first.
    val flopefficiencymap = processors.sortWith(_.efficiency(temp) < _.efficiency(temp)).flatMap { p =>
      if (p.canTick(temp, storage)) {
        val result = p.tick(temp, storage, simulate = true)
        storage -= result.power
        Some((result.flops, p.efficiency(temp)))
      }
      else None
    }
    val totalFlops        = flopefficiencymap.map(_._1).sum
    // Multiply the efficiency by the slice flops potentially generated to get the slice of total efficiency for each processor, then sum
    flopefficiencymap.map(t => t._2 * (t._1 / totalFlops)).sum
  }
}
