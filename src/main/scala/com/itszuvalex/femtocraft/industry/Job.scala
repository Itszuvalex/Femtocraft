package com.itszuvalex.femtocraft.industry

/**
  * Created by Christopher Harris (Itszuvalex) on 2/25/2016.
  */
abstract class Job {

  /**
    *
    * @return Total power requirement of this job.
    */
  def getTotalRequiredPower: Long

  /**
    *
    * @return Power requirement still to be fulfilled.
    */
  def getRemainingPower: Long

  /**
    *
    * @return Minimum # of ticks required to work this job.  Used to calculate maximum power/tick input.
    */
  def getMinimumRequiredTicks: Long

  /**
    *
    * @return Calculated by getTotalRequiredPower/getMinimumRequiredTicks.  Thus, a job that takes 20,000 power over 200 ticks (10s) will accept 20p/t max.
    */
  def getPowerPerTick: Double = getTotalRequiredPower.toDouble / getMinimumRequiredTicks.toDouble

  /**
    *
    * @param power Amount of power available to be used for this tick.
    * @return Amount of power used this tick.
    */
  def tick(power: Double): Double

}
