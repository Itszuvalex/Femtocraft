package com.itszuvalex.femtocraft.api

trait DistributableResource {
  def max: Double

  def room: Double = max - amt

  def amt: Double

  def transferMax: Double

  def add(amt: Double): Double

  def remove(amt: Double): Double
}
