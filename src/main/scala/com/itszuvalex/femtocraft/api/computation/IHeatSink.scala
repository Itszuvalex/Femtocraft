package com.itszuvalex.femtocraft.api.computation

trait IHeatSink extends IHeatNetworkLeafNode {
  def heatStorage: IHeatStorage

  /**
   * To prevent HeatSinks from gaining infinite energy, this exists.
   * This will return false if the Heat Sink cannot accept more heat.  Thematically : Metal melting, electronics frying, plastic fans failing, etc
   *
   * @return False when this heat sink cannot accept more heat.
   */
  def canAcceptHeat: Boolean
}
