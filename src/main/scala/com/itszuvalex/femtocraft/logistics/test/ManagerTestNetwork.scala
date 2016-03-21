package com.itszuvalex.femtocraft.logistics.test

import com.itszuvalex.itszulib.logistics.{LocationTracker, ManagerNetwork}

/**
  * Created by Christopher Harris (Itszuvalex) on 1/30/2016.
  */
object ManagerTestNetwork {
  val tracker = new LocationTracker

  def NewNetwork() = new TestTrackingNetwork(ManagerNetwork.getNextID)
}
