package com.itszuvalex.femtocraft.api.power

import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.IBattery
import com.itszuvalex.itszulib.logistics.TileNetworkNode

/**
  * Created by Chris on 1/1/2017.
  */
trait IPowerNetworkNode extends TileNetworkNode[IPowerNetworkNode, PowerNetwork] {


  override def canConnect(loc: Loc4): Boolean = {
    if (!(getLoc.distSqr(loc) <= connectionRadius * connectionRadius))
      return false

    loc.getTileEntity(false) match {
      case None => false
      case Some(a: IPowerNetworkNode) =>
        connectType.canConnect(a.connectType)
    }
  }

  def storageType: PowerStorageNodeType

  def connectType: PowerConnectionNodeType

  def connectionRadius: Float

  def transferRate: Double

  def rendersConnections: Boolean

  def setRenderLocations(set: Set[Loc4]): Unit

  def storage: IBattery
}
