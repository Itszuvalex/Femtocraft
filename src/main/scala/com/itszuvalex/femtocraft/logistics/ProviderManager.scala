package com.itszuvalex.femtocraft.logistics

import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.logistics.LocationTracker

import scala.collection.mutable

/**
 * Created by Christopher on 7/29/2015.
 */
object ProviderManager {
  private val providerMap = mutable.HashMap[Class, LocationTracker]()

  def addProvider(resourceType: Class, loc: Loc4) = {
    providerMap.getOrElseUpdate(resourceType, new LocationTracker()).trackLocation(loc)
  }

  def removeProvider(resourceType: Class, loc: Loc4) = {
    providerMap.get(resourceType).map(_.removeLocation(loc))
  }

  def clear() = {
    providerMap.clear()
  }

}
