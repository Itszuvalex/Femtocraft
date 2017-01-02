package com.itszuvalex.femtocraft.api.power

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.IBattery
import com.itszuvalex.itszulib.logistics.TileNetworkNode
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing

/**
  * Created by Chris on 1/1/2017.
  */
trait IPowerNetworkNode extends TileNetworkNode[IPowerNetworkNode, PowerNetwork] {


  override def canConnect(loc: Loc4): Boolean = {
    if (!(getLoc.distSqr(loc) <= connectionRadius * connectionRadius))
      return false

    if (getLoc.compareTo(loc) == 0)
      return false

    loc.getTileEntity(false) match {
      case None => false
      case Some(a: TileEntity) if a.hasCapability(Capabilities.POWER_NODE, EnumFacing.UP) =>
        connectType.canConnect(a.getCapability(Capabilities.POWER_NODE, EnumFacing.UP).connectType)
    }
  }

  def storageType: PowerStorageNodeType

  def connectType: PowerConnectionNodeType

  def connectionRadius: Float

  def transferRate: Double

  def rendersConnections: Boolean

  def setRenderLocations(set: scala.collection.Set[Loc4]): Unit

  def renderLocations: scala.collection.Set[Loc4]

  def storage: IBattery
}
