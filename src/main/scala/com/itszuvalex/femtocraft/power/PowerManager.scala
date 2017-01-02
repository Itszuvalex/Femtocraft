package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.api.power.{IPowerNetworkNode, PowerNetwork}
import com.itszuvalex.itszulib.logistics.LocationTracker
import net.minecraft.tileentity.TileEntity

/**
  * Created by Christopher Harris (Itszuvalex) on 8/3/15.
  */
object PowerManager {
  val nodeTracker = new LocationTracker

  def clear() = {
    nodeTracker.clear()
  }

  /**
    * Attempts to add node to the IPowerNetworkNode mapping.  If the node has no parent location, PowerManager will attempt to find a parent for it.  If one is not found,
    * it will add the node to its parentless list, and will then try to find a parent for it every time a new node is added.
    *
    * @param node Node to be added.
    */
  def addNode(node: IPowerNetworkNode): Unit = {
    val loc = node.getLoc

    /* Actually track the node */
    nodeTracker.trackLocation(loc)

    val network = PowerNetwork.createFromTile(node)
    val nodes = getIPowerNetworkNodesInRange(nodeTracker, node, node.connectionRadius).filterNot(_._1 == loc)
      .map(_._1).foreach { nloc =>
      if (node.getNetwork.canConnect(loc, nloc.getLoc) && node.getNetwork != nloc.getNetwork)
        node.getNetwork.addConnection(loc, nloc.getLoc)
    }
  }

  def removeNode(node: IPowerNetworkNode): Unit = {
    nodeTracker.removeLocation(node.getLoc)
    node.getNetwork.removeNode(node)
  }


  private def getIPowerNetworkNodesInRange(tracker: LocationTracker, node: IPowerNetworkNode, radius: Float): Iterable[(TileEntity with IPowerNetworkNode, Double)] = {
    val loc = node.getLoc
    tracker.getLocationsInRange(loc, radius).view
      .filterNot(_ == node.getLoc)
      .flatMap(_.getTileEntity(force = false))
      .collect { case cnode: IPowerNetworkNode => cnode }
      .map(cnode => (cnode, cnode.getLoc.distSqr(loc)))
      .filter(pair => (pair._2 <= (pair._1.connectionRadius * pair._1.connectionRadius)) &&
        (pair._2 <= (node.connectionRadius * node.connectionRadius)))
  }


}
