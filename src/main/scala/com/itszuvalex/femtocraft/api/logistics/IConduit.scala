package com.itszuvalex.femtocraft.api.logistics

import net.minecraft.util.EnumFacing

/**
  * Created by Chris on 2/16/2017.
  */
trait IConduit {

  def isConnected(facing: EnumFacing): Boolean

  def canAddConnection(facing: EnumFacing): Boolean

  def addConnection(facing: EnumFacing): Unit

  def removeConnection(facing: EnumFacing): Unit

}
