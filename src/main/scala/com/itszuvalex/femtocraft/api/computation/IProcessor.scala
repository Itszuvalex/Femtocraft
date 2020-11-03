package com.itszuvalex.femtocraft.api.computation

import com.itszuvalex.femtocraft.api.power.DEPower

case class ProcessorTick(flops: FLOPS, power: DEPower)

/**
 * Trait for processor [[com.itszuvalex.itszulib.api.core.Module Module]]/[[net.minecraftforge.common.capabilities.Capability Capability]]
 * This is intended for [[net.minecraft.item.ItemStack ItemStack]] module return in [[net.minecraft.item.ItemStack#getCapability ItemStack#GetCapability]].
 *
 * Processors should
 *
 *  - throttle-down at higher temperatures.  Consider implementing multiple thresholds / modes.
 *  - not allow run-away temperature gains.  Consider all *perTicks returning 0 if temperature is over a high threshold.
 *
 */
trait IProcessor {

  /**
   *
   * @param power       Power available for ticking
   * @return The results of ticking this processor at this temperature.
   */
  def tick(power: DEPower, simulate: Boolean): ProcessorTick

  /**
   * Utility function so that you don't have to try and parse archaic [[com.itszuvalex.femtocraft.api.computation.ProcessorTick ProcessorTick]] results of a throttled processor.
   *
   * @param power       Power available for ticking
   * @return True if this processor can be ticked.
   */
  def canTick(power: DEPower): Boolean

  /**
   *
   * @return Efficiency rating of this processor at this temperature.
   */
  def efficiency(): Double
}
