package com.itszuvalex.femtocraft.common

import com.itszuvalex.itszulib.api.storage.IBattery
import com.itszuvalex.itszulib.util.Task

abstract class BatteryPoweredTask(baseGoal: Double, minTicks: Int, bat: IBattery, speed: () => Double, efficiency: () => Double) extends Task(baseGoal, minTicks) {
  def inProgress: Boolean

  def canStart: Boolean

  def start(): Unit

  def tick(): Unit = {
    if (inProgress) {
      bat.storage -= contribute(Math.min(powerPerTick(speed(), efficiency()), bat.storage), speed(), efficiency())
      if (completed(efficiency()))
        onCompleted()
    }
    else if (canStart)
      start()
  }

  def onCompleted(): Unit

}
