package com.itszuvalex.femtocraft.api.computation

object IHeatStorage {
  val defaultTemperature: Double = 300d
}

trait IHeatStorage {
  /**
   *
   * @return
   */
  def material: IEnergyMaterial

  /**
   *
   * @return Returned in Kg
   */
  def mass: Double

  /**
   *
   * @return Amount of energy contained in this storage as calculated by the heatCapacity of the material, the mass of material, and the current temperature.
   */
  def energy: Double

  /**
   *
   * @param energyToAdd Amount of energy to add
   * @return Amount of energy remaining out of energyToAdd
   */
  def addEnergy(energyToAdd: Double): Double

  /**
   *
   * @param energyToRemove Amount of energy to remove
   * @return Amount of energy successfully removed.
   */
  def removeEnergy(energyToRemove: Double): Double

  /**
   *
   * @return The current temperature of the heat storage.
   */
  def temperature: Temperature
}
