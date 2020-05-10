package com.itszuvalex.femtocraft.api.power

import com.itszuvalex.femtocraft.api.IConduitTier
import com.itszuvalex.itszulib.logistics.IPersistedConnectableNetworkNode

trait IWiredPowerNode extends IPersistedConnectableNetworkNode[IWiredPowerNode, WiredPowerNetwork] with IWiredPowerConnectable {

  def tier: IConduitTier

}
