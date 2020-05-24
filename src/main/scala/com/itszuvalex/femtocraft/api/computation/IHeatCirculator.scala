package com.itszuvalex.femtocraft.api.computation

/**
 * Adds the ability to circulate
 */
trait IHeatCirculator extends IHeatNetworkLeafNode {
  def energyTransfer(): Double
}
