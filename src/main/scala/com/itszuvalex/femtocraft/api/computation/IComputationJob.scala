package com.itszuvalex.femtocraft.api.computation

trait IComputationJob {

  def FLOPs: FLOPS

  def FLOPSRequired: FLOPS

  def FLOPSRemaining: FLOPS = FLOPSRequired - FLOPs

  def FLOPSContributablePerTick: FLOPS

  /**
   *
   * @param flops Amount of FLOPs to contribute.
   * @return Amount of flops remaining out of flops.
   */
  def contributeFLOPS(flops: FLOPS): FLOPS

}
