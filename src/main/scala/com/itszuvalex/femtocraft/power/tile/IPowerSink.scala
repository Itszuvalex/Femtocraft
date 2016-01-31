package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.logistics.distributed.ITaskProvider

/**
  * Created by Christopher Harris (Itszuvalex) on 1/30/2016.
  */
trait IPowerSink extends ITaskProvider {

  def getCurrentPower: Long

  def getMaximumPower: Long

  /**
    *
    * @param amt      Amount to attempt to charge
    * @param doCharge False to simulate, true to actually do
    * @return Amount of amt used to actually charge.
    */
  def charge(amt: Long, doCharge: Boolean): Long

}
