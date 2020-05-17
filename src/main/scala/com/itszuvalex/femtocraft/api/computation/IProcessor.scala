package com.itszuvalex.femtocraft.api.computation

trait IProcessor {

  def FLOPSperTick(temperature: Double): Double

  def EnergyPerTick(temperature: Double): Double

}
