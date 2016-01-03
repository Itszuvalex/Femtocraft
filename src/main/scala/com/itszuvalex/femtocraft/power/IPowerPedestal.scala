package com.itszuvalex.femtocraft.power

import com.itszuvalex.itszulib.api.core.Loc4

/**
  * Created by Christopher Harris (Itszuvalex) on 8/26/15.
  */
trait IPowerPedestal {

  /**
    *
    * @return Location of the mount this is connected to.
    */
  def mountLoc: Loc4

  /**
    *
    * @param loc Location to accept mount connection at.
    * @return True if mount can be added to this location.
    */
  def canSetMount(loc: Loc4): Boolean

  /**
    *
    * @param loc Location of mount.
    */
  def setMount(loc: Loc4): Unit
}
