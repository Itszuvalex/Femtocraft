package com.itszuvalex.femtocraft.power.item

import com.itszuvalex.itszulib.api.wrappers.IBattery

/**
  * Created by Christopher on 7/29/2015.
  */
object IPowerCrystal {
  val TYPE_SMALL  = "small"
  val TYPE_MEDIUM = "medium"
  val TYPE_LARGE  = "large"
}

trait IPowerCrystal {
  /**
    * Used to trigger passive trickle charging.
    */
  def onTick(): Unit

  def getName(): String

  /**
    *
    * @return Color of the crystal.
    */
  def getColor(): Int

  /**
    *
    * @return Amount of power to generate per tick.
    */
  def getPassiveGen(): Double

  /**
    *
    * @return Amount of power in crystal that is less than current storage.  Used for passive trickle charging.
    */
  def getStoragePartial(): Double

  /**
    *
    * @return Maximum amount of power that can flow from this crystal.  This is meant to be per-tick, divided among children.
    */
  def getTransferRate(): Double

  /**
    *
    * @return Size of the crystal.
    */
  def getType(): String

  def setColor(color: Int): Unit

  def setTransferRate(rate: Double): Unit

  def setType(ctype: String): Unit

  def setPassiveGen(passiveGen: Float): Unit

  def setName(name: String): Unit

  def setStoragePartial(amount: Double): Unit

  def battery: IBattery
}