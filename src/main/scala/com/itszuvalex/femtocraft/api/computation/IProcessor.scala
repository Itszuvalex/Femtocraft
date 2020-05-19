package com.itszuvalex.femtocraft.api.computation

import com.itszuvalex.femtocraft.api.power.DEPower

/**
 * Trait for processor [[com.itszuvalex.itszulib.api.core.Module Module]]/[[net.minecraftforge.common.capabilities.Capability Capability]]
 * This is intended for [[net.minecraft.item.ItemStack]] module return in [[net.minecraft.item.ItemStack#getCapability]].
 *
 * Processors should
 *
 *  - throttle-down at higher temperatures.  Consider implementing multiple thresholds / modes.
 *  - not allow run-away temperature gains.  Consider all *perTicks returning 0 if temperature is over a high threshold.
 *
 */
trait IProcessor {

  def maxPower(temperature: Temperature): DEPower

  /**
   *
   * @param temperature Current temperature of the processor
   * @return Number of FLOPs that would be generated per tick given temperature
   */
  def FLOPS(temperature: Temperature): FLOPS

  /**
   *
   * @param temperature Current temperature of the processor
   * @return Energy in J that would be generated per tick given temperature
   */
  def EnergyPerTick(temperature: Temperature): Joules

}
