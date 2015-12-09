package com.itszuvalex.femtocraft.nanite

import com.itszuvalex.itszulib.api.core.Loc4

/**
  * Created by Christopher Harris (Itszuvalex) on 8/24/15.
  */
trait INaniteNode {
  def getNodeLoc: Loc4

  def getHiveLoc: Loc4

  def getHive: INaniteHive

  def setHive(hive: INaniteHive): Boolean

  def canSetHive(hive: INaniteHive): Boolean

  def hiveConnectionRadius: Float

}
