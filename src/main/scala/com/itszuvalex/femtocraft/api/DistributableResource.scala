package com.itszuvalex.femtocraft.api

abstract class DistributableResource[T:Numeric] {
  val n: Numeric[T] = implicitly[Numeric[T]]

  def max: T

  def room: T ={
    n.minus(max, amt)
  }

  def amt: T

  def transferMax: T

  def add(amt: T): T

  def remove(amt: T): T
}
