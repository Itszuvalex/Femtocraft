package com.itszuvalex.femtocraft.api.computation

import com.itszuvalex.femtocraft.api.IConduitTier
import com.itszuvalex.itszulib.logistics.IPersistedConnectableNetworkNode

trait IWiredComputationNode extends IPersistedConnectableNetworkNode[IWiredComputationNode, WiredComputationNetwork] with IWiredComputationConnectable {

  def tier: IConduitTier

}
