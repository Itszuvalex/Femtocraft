package com.itszuvalex.femtocraft.api.computation

import net.minecraft.util.EnumFacing

trait IWiredComputationConnectable {

  def isConnectedWiredComputation(facing: EnumFacing): Boolean

  def canConnectWiredComputation(facing: EnumFacing): Boolean

  def connectWiredComputation(facing: EnumFacing): Boolean

  def disconnectWiredComputation(facing: EnumFacing): Boolean
}
