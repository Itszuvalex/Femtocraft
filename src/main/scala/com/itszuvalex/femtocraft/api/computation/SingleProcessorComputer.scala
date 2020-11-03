package com.itszuvalex.femtocraft.api.computation

import com.itszuvalex.itszulib.api.storage.IBattery

class SingleProcessorComputer(val battery: IBattery, val processor: IProcessor) extends IComputer {


  override def generateFLOPS(): FLOPS = {
    if (processor.canTick(battery.storage)) {
      val results = processor.tick(battery.storage, simulate = false)
      battery.drain(results.power)
      results.flops
    }
    else 0
  }

  /**
   *
   * @return FLOPS / Power cost per flop.  Higher efficiency computers are used before lower efficiency computers.
   *         This should be dynamic - so throttled computers should return lower if they use generate fewer flops for the same power.
   */
  override def efficiency: Double = processor.efficiency()

}
