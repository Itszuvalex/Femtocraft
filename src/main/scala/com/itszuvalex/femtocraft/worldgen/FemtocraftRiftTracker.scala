package com.itszuvalex.femtocraft.worldgen

import com.itszuvalex.itszulib.logistics.LocationTracker

object FemtocraftRiftTracker {
  private val riftLocs = new LocationTracker

  def registerRift(rift: Rift): Unit = {
      riftLocs.trackLocation(rift.loc)
  }

  def deregisterRift(rift: Rift): Unit = {
    riftLocs.removeLocation(rift.loc)
  }
}
