package com.itszuvalex.femtocraft.api

abstract class DistributableResource[T: Numeric] {

  import scala.math.Numeric.Implicits._

  def max: T

  def room: T = max - amt

  def amt: T

  def transferMax: T

  def add(amt: T): T

  def remove(amt: T): T
}
