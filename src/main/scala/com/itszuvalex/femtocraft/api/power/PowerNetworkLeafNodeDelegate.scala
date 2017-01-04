package com.itszuvalex.femtocraft.api.power

import com.itszuvalex.femtocraft.power.PowerManager
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.IBattery
import com.itszuvalex.itszulib.core.TileEntityBase

/**
  * Created by Chris on 1/1/2017.
  */
class PowerNetworkLeafNodeDelegate(tileEntity: TileEntityBase,
  storageNodeType: => PowerStorageNodeType,
  connectNodeType: => PowerConnectionNodeType,
  radius: => Float,
  transfer: => Double,
  renders: => Boolean,
  power: => IBattery
) extends PowerNetworkNodeDelegate(tileEntity, storageNodeType, connectNodeType, radius, transfer, renders, power) {
  var parent: Option[Loc4] = None

  override def canConnect(loc: Loc4): Boolean = {
    if (!super.canConnect(loc)) return false

    if (parent.isEmpty) return true

    getLoc.distSqr(loc) < getLoc.distSqr(parent.get)
  }

  override def connect(node: Loc4): Unit = {
    super.connect(node)

    if (parent.isDefined && node.compareTo(parent.get) != 0) {
      parent = Some(node)
    }
    else
      parent = Some(node)
  }

  override def disconnect(node: Loc4): Unit = {
    super.disconnect(node)
    if (parent.isDefined && node.compareTo(parent.get) == 0) {
      parent = None
      network.removeNode(this)
      PowerManager.addNode(this)
    }
    else
      parent = None
  }
}
