package com.itszuvalex.femtocraft.api

import com.itszuvalex.itszulib.api.storage.IBattery

case class DistributableBattery(battery: IBattery, transMax: () => Double) extends DistributableResource[Double] {
  override def max: Double = battery.maxStorage

  override def amt: Double = battery.storage

  override def transferMax: Double = transMax()

  override def add(amt: Double): Double = battery.fill(amt)

  override def remove(amt: Double): Double = battery.drain(amt)
}
