package com.itszuvalex.femtocraft.api.logistics

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.itszulib.logistics.{ManagerNetwork, TileNetwork}
import net.minecraftforge.common.capabilities.Capability

/**
  * Created by Chris on 2/19/2017.
  */
class LogisticsNetwork extends TileNetwork[ILogisticsNetworkNode, LogisticsNetwork](ManagerNetwork.getNextID) {
  override def networkCapability: Capability[ILogisticsNetworkNode] = Capabilities.TILE_LOGISTICS_NODE

  override def create(): LogisticsNetwork = new LogisticsNetwork

  override def onTickStart(): Unit = {}

  override def onTickEnd(): Unit = {}

  override def onTakeover(iNetwork: LogisticsNetwork): Unit = {}

  override def onSplit(iNetwork: LogisticsNetwork): Unit = {}

}
