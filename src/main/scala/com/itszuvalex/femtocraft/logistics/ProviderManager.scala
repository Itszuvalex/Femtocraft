package com.itszuvalex.femtocraft.logistics

import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.logistics.LocationTracker

import scala.collection.mutable

/**
  * Created by Christopher on 7/29/2015.
  */
object ProviderManager {
  private val providerMap = mutable.HashMap[Class[_], LocationTracker]()

  def addProvider(resourceType: Class[_], loc: Loc4) = {
    providerMap.getOrElseUpdate(resourceType, new LocationTracker()).trackLocation(loc)
  }

  def removeProvider(resourceType: Class[_], loc: Loc4) = {
    providerMap.get(resourceType).map(_.removeLocation(loc))
  }

  def clear() = {
    providerMap.clear()
  }

}
