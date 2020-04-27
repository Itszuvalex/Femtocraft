package com.itszuvalex.femtocraft.api.power

import net.minecraft.util.EnumFacing

trait IWiredPowerConnectable {

  def isConnectedWiredPower(facing: EnumFacing): Boolean

  def canConnectWiredPower(facing: EnumFacing): Boolean

  def connectWiredPower(facing: EnumFacing): Boolean

  def disconnectWiredPower(facing: EnumFacing): Boolean
}
