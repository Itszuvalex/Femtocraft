package com.itszuvalex.femtocraft.logistics.tile

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.logistics.IConduit
import com.itszuvalex.itszulib.core.TileEntityBase
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

/**
  * Created by Chris on 2/16/2017.
  */
class TileConduit extends TileEntityBase {
  override def hasDescription: Boolean = true

  override def getMod: AnyRef = Femtocraft

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
    if (capability == Capabilities.TILE_CONDUIT)
      new IConduit {
        override def isConnected(facing: EnumFacing): Boolean = facing.getIndex == Math.abs(getLoc.x % 6)
      }.asInstanceOf[T]
    else super.getCapability(capability, facing)
  }

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = {
    if (capability == Capabilities.TILE_CONDUIT) true
    else super.hasCapability(capability, facing)
  }
}
