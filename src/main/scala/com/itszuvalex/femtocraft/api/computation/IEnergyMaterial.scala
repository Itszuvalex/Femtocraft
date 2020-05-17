package com.itszuvalex.femtocraft.api.computation


object IEnergyMaterial {
  val INVALID: IEnergyMaterial = new IEnergyMaterial {
    override def name: String = "Invalid"

    /**
     *
     * @return The heat capacity of the current material in J/kG per ^o^C/^o^K
     */
    override def heatCapacity: Double = .0001d
  }
}

trait IEnergyMaterial {

  def name: String

  /**
   *
   * @return The heat capacity of the current material in J/kG per ^o^C/^o^K
   */
  def heatCapacity: Double
}
