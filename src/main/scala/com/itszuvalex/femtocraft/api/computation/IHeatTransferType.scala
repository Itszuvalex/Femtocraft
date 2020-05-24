package com.itszuvalex.femtocraft.api.computation

object IHeatTransferType {
  val Air: IHeatTransferType = new IHeatTransferType {
    override def canConnectTo(heatTransferType: IHeatTransferType): Boolean = heatTransferType == IHeatTransferType.Air
  }
}

trait IHeatTransferType {
  def canConnectTo(heatTransferType: IHeatTransferType): Boolean
}
