package com.itszuvalex.femtocraft.api.logistics

/**
  * Created by Chris on 2/19/2017.
  */
trait IConnection[T] {
  def resource: IResource

  def channel: String

  /**
    * Active is whether or not this channel is actively performing computation.
    * I.E.  An input channel would be performing computation if it had resources in its buffer.
    * Items: an ItemStack
    * Fluids: Some amount of ml.  (If it is a constant transfer, though I recommend time-dispatch buffering)
    * Nanites: Similar to Fluids
    *
    * @return
    */
  def active: Boolean

  def flopsRemaining: Double

  def flopsMaximum: Double

  /**
    *
    * @param flops Amount of flops that can be contributed.
    *
    * @return Amount of FLOPs from FLOPs that are unused.
    */
  def contributeFlops(flops: Double): Double

  def buffer: T

  def setBuffer(a: T): Unit

}
