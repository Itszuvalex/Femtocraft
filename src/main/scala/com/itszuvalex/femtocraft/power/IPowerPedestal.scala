package com.itszuvalex.femtocraft.power

import com.itszuvalex.itszulib.api.core.Loc4

/**
 * Created by Christopher Harris (Itszuvalex) on 8/26/15.
 */
trait IPowerPedestal {

  /**
   *
   * @return Amount of power stored.
   */
  def getStored: Long

  /**
   *
   * @return Amount of power capable of being stored.
   */
  def getMax: Long

  /**
   *
   * @param amount Amount of power to store.
   * @param doStore Pass true to actually consume resources.  False simulates the store.
   * @return Amount of @amount successfully stored.
   */
  def store(amount: Long, doStore: Boolean): Long

  /**
   *
   * @param amount Amount of power to attempt to consume.
   * @param doConsume Pass true to actually consume resources.  False simulates the store.
   * @return Amount of @amount successfully removed.
   */
  def consume(amount: Long, doConsume: Boolean): Long


  /**
   *
   * @return Direction of the mount this is connected to.  UNKNOKWN otherwise.
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
