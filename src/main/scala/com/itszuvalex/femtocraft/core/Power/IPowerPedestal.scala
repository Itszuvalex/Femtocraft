package com.itszuvalex.femtocraft.core.Power

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

}
