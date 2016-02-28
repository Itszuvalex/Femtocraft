package com.itszuvalex.femtocraft.power.tile

/**
  * Created by Christopher Harris (Itszuvalex) on 2/23/2016.
  */
trait ITilePower {
  def getCurrentPower: Double

  def getMaximumPower: Double

  /**
    *
    * @param amt     Amount of power to drain
    * @param doDrain False to simulate, true to actually remove power.
    * @return Amount of amt that was successfully drained.
    */
  def drain(amt: Double, doDrain: Boolean): Double

  /**
    *
    * @param amt      Amount to attempt to charge
    * @param doCharge False to simulate, true to actually do
    * @return Amount of amt used to actually charge.
    */
  def charge(amt: Double, doCharge: Boolean): Double
}
