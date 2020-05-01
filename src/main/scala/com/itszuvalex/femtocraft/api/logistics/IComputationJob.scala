package com.itszuvalex.femtocraft.api.logistics

trait IComputationJob {

  def FLOPs: Double

  def FLOPsRequired: Double

  def FLOPSContributablePerTick: Double

  /**
   *
   * @param flops Amount of FLOPs to contribute.
   * @return Amount of flops remaining out of flops.
   */
  def contributeFlops(flops: Double): Double

}
