package com.itszuvalex.femtocraft.api.computation

import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

object HeatStorage {
  val MATERIAL_NBT = "mat"
  val MASS_NBT     = "mass"
  val ENERGY_NBT   = "en"
}

class HeatStorage(energyMaterial: IEnergyMaterial, val materialMass: Kg, materialTemperature: Temperature) extends IHeatStorage with INBTSerializable[NBTTagCompound] {

  def this(energyMaterial: IEnergyMaterial, materialMass: Kg, materialEnergy: Joules) = this(
    energyMaterial, materialMass,
    Temperature.kelvin((materialEnergy / materialMass) / energyMaterial.heatCapacity)
    )

  var mat : IEnergyMaterial = energyMaterial
  var temp: Temperature     = materialTemperature.toKelvin

  /**
   * HeatCapacity = J / Kg / 1^o^C
   *
   * J = HeatCapacity * 1^o^C * Kg
   */
  var energyInJoules: Joules = energyMaterial.heatCapacity * temp.kelvin * materialMass

  /**
   * HeatCapacity = J / Kg / 1^o^C
   *
   * ^o^C = J / Kg / HeatCapacity
   */
  private def calculateTemperature(): Unit = {
    temp = Temperature.kelvin((energy / mass) / material.heatCapacity)
  }

  /**
   *
   * @return Returned in Kg
   */
  override def mass: Kg = materialMass

  override def material: IEnergyMaterial = energyMaterial

  override def energy: Joules = energyInJoules

  override def temperature: Temperature = temp

  /**
   *
   * @param energyToAdd Amount of energy to add
   * @return Amount of energy remaining out of energyToAdd
   */
  override def addEnergy(energyToAdd: Double): Double = {
    energyInJoules += energyToAdd
    calculateTemperature()
    0d
  }

  /**
   *
   * @param energyToRemove Amount of energy to remove
   * @return Amount of energy successfully removed.
   */
  override def removeEnergy(energyToRemove: Double): Double = {
    val toRemove = math.min(energy, energyToRemove)
    energyInJoules -= toRemove
    calculateTemperature()
    toRemove
  }

  override def serializeNBT(): NBTTagCompound = {
    val nbt = new NBTTagCompound
    nbt.setDouble(HeatStorage.ENERGY_NBT, energy)
    nbt
  }

  override def deserializeNBT(nbt: NBTTagCompound): Unit = {
    energyInJoules = nbt.getDouble(HeatStorage.ENERGY_NBT)
    calculateTemperature()
  }
}
