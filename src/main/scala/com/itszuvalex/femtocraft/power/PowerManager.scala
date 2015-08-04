package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.power.node.IPowerNode
import com.itszuvalex.itszulib.logistics.LocationTracker
import net.minecraft.tileentity.TileEntity

/**
 * Created by Christopher Harris (Itszuvalex) on 8/3/15.
 */
object PowerManager {
  val nodeTracker       = new LocationTracker
  val parentlessTracker = new LocationTracker

  def clear() = {
    nodeTracker.clear()
    parentlessTracker.clear()
  }

  /**
   * Attempts to add node to the IPowerNode mapping.  If the node has no parent location, PowerManager will attempt to find a parent for it.  If one is not found,
   * it will add the node to its parentless list, and will then try to find a parent for it every time a new node is added.
   * @param node Node to be added.
   */
  def addNode(node: IPowerNode): Unit = {
    val loc = node.getNodeLoc
    System.out.println("Tracking location: " + loc)
    /* If added node doesn't have a stored parent */
    if (node.getParentLoc == null) {
      System.out.println("Finding parent")
      if (getIPowerNodesInRange(nodeTracker, node, node.parentConnectionRadius).filter { case (cnode, _) => cnode.canAddChild(node) && node.canAddParent(cnode) }.toList.sortWith(_._2 < _._2).exists(pnode => pnode._1.addChild(node) && node.setParent(pnode._1)))
        System.out.println("Found parent")
    }
    /* Try and add new node as parent to as many parentless nodes as possible. */
    getIPowerNodesInRange(parentlessTracker, node, node.childrenConnectionRadius).filter { case (cnode, _) => cnode.canAddParent(node) && node.canAddChild(cnode) }.foreach { case (cnode, _) => if (cnode.setParent(node) && node.addChild(cnode)) parentlessTracker.removeLocation(cnode.getNodeLoc) }

    /* Actually track the node */
    nodeTracker.trackLocation(loc)
    if (node.getParentLoc == null)
      parentlessTracker.trackLocation(loc)
  }


  private def getIPowerNodesInRange(tracker: LocationTracker, node: IPowerNode, radius: Float): Iterable[(TileEntity with IPowerNode, Double)] = {
    val loc = node.getNodeLoc
    tracker.getLocationsInRange(loc, radius).flatMap(_.getTileEntity(force = false)).collect { case cnode: IPowerNode if cnode != node => cnode }.map(cnode => (cnode, cnode.getNodeLoc.distSqr(loc))).filter(pair =>
                                                                                                                                                                                                                (pair._2 <= (pair._1.parentConnectionRadius * pair._1.parentConnectionRadius)) && (pair._2 <= (node.childrenConnectionRadius * node.childrenConnectionRadius)))
  }

  def removeNode(node: IPowerNode): Unit = {
    nodeTracker.removeLocation(node.getNodeLoc)
    parentlessTracker.removeLocation(node.getNodeLoc)
  }


}
