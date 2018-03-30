package com.itszuvalex.femtocraft.api.power

import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.IBattery

/**
  * Created by Chris on 1/4/2017.
  */
trait IPowerStorageNode {

  def battery: IBattery

  def storageType: PowerStorageNodeType

  def transferRate: Double

  def getStorageLoc: Loc4

  def changeForLastTick: Double
}
