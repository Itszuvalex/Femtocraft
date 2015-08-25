package com.itszuvalex.femtocraft.nanite

import com.itszuvalex.itszulib.logistics.LocationTracker

/**
 * Created by Christopher Harris (Itszuvalex) on 8/24/15.
 */
object NaniteManager {
  private val hiveTracker           = new LocationTracker
  private val parentlessNodeTracker = new LocationTracker

  def addHive(hive: INaniteHive) = {
    hiveTracker.trackLocation(hive.getHiveLoc)
  }

  def removeHive(hive: INaniteHive) = {
    hiveTracker.removeLocation(hive.getHiveLoc)
  }

  def addNode(node: INaniteNode) = {
    /* If added node doesn't have a stored parent */
    if (node.getHiveLoc == null) {
      findParent(node)
    }
    refreshParentlessStatus(node)
  }

  def removeNode(node: INaniteNode) = {
    parentlessNodeTracker.removeLocation(node.getNodeLoc)
  }

  def refreshParentlessStatus(node: INaniteNode) =
    if (node.getHiveLoc == null)
      parentlessNodeTracker.trackLocation(node.getNodeLoc)
    else
      parentlessNodeTracker.removeLocation(node.getNodeLoc)


  private def findParent(node: INaniteNode) = {
    val loc = node.getNodeLoc
    parentlessNodeTracker.getLocationsInRange(loc, node.hiveConnectionRadius).view
    .filterNot(_ == node.getNodeLoc)
    .flatMap(_.getTileEntity(force = false))
    .collect { case hive: INaniteHive => hive }
    .map(hive => (hive, hive.getHiveLoc.distSqr(loc)))
    .filter(pair => (pair._2 <= (pair._1.connectionRadius * pair._1.connectionRadius)) &&
                    (pair._2 <= (node.hiveConnectionRadius * node.hiveConnectionRadius)))
    .filter { case (hive, _) => hive.canAddNode(node) && node.canSetHive(hive) }
    .toList.sortWith(_._2 < _._2)
    .exists(pnode => pnode._1.addNode(node) && node.setHive(pnode._1))
  }

  def clear() = {
    hiveTracker.clear()
    parentlessNodeTracker.clear()
  }
}
