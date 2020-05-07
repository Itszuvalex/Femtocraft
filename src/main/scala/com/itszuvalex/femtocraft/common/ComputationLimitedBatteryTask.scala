package com.itszuvalex.femtocraft.common

import com.itszuvalex.femtocraft.api.computation.IComputationJob
import com.itszuvalex.itszulib.api.storage.IBattery

abstract class ComputationLimitedBatteryTask(baseGoal: Double, minTicks: Int, bat: IBattery, comp: IComputationJob, speed: () => Double, efficiency: () => Double) extends BatteryPoweredTask(baseGoal, minTicks, bat, speed, efficiency) {

  /**
   *
   * @param power Power to contribute
   * @return Power used out of power
   */
  override def contribute(power: Double, speed: Double, efficiency: Double): Double = {
    val take      = Math.min(progressRemaining(efficiency), powerPerTick(speed, efficiency))
    val compPerc  = if (comp.FLOPsRequired > 0) comp.FLOPs / comp.FLOPsRequired else 1d
    val progPerc  = progress / adjustedMax(efficiency)
    val roomToCap   = (compPerc - progPerc) * adjustedMax(efficiency)
    val ret       = Math.min(power, math.min(take, roomToCap))
    progress += ret
    ret
  }
}
