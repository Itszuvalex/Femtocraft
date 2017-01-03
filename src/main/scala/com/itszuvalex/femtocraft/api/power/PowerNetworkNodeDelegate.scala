package com.itszuvalex.femtocraft.api.power

import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.IBattery
import com.itszuvalex.itszulib.core.TileEntityBase

/**
  * Created by Chris on 1/1/2017.
  */
class PowerNetworkNodeDelegate(tileEntity: TileEntityBase,
  storageNodeType: => PowerStorageNodeType,
  connectNodeType: => PowerConnectionNodeType,
  radius: => Float,
  transfer: => Double,
  renders: => Boolean,
  power: => IBattery
) extends IPowerNetworkNode {
  var renderLocs: scala.collection.Set[Loc4] = Set()

  override def storageType: PowerStorageNodeType = storageNodeType

  override def connectType: PowerConnectionNodeType = connectNodeType

  override def connectionRadius: Float = radius

  override def transferRate: Double = transfer

  override def rendersConnections: Boolean = renders

  override def setRenderLocations(set: scala.collection.Set[Loc4]): Unit = {
    renderLocs = set
    tileEntity.setUpdate()
  }

  override def renderLocations: scala.collection.Set[Loc4] = renderLocs

  override def storage: IBattery = power

  override def getLoc: Loc4 = tileEntity.getLoc
}
