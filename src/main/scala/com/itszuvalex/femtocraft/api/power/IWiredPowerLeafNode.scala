package com.itszuvalex.femtocraft.api.power

import com.itszuvalex.itszulib.api.storage.IBattery

trait IWiredPowerLeafNode extends IWiredPowerConnectable {

  def battery: IBattery

  def powerType: PowerStorageNodeType

  def transferRate: Double

}
