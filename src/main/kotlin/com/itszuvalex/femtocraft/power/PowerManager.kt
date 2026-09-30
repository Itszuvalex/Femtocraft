package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.FemtoModules
import com.itszuvalex.itszulib.api.utility.LocationTracker

/**
 * Server-side index of loaded power nodes, used to find parents and children by radius. Port of the 1.7.10
 * `PowerManager`. Cleared when the server stops.
 */
object PowerManager {
    val nodeTracker = LocationTracker()
    val parentlessTracker = LocationTracker()

    fun clear() {
        nodeTracker.clear()
        parentlessTracker.clear()
    }

    /**
     * Tracks [node], finds it a parent if it has none, and adopts parentless nodes in range.
     */
    fun addNode(node: IPowerNode) {
        if (node.getParentLoc() == null) findParent(node)

        getNodesInRange(parentlessTracker, node, node.childrenConnectionRadius())
            .filter { (cnode, _) -> cnode.canSetParent(node) && node.canAddChild(cnode) }
            .forEach { (cnode, _) ->
                if (cnode.setParent(node) && node.addChild(cnode)) parentlessTracker.removeLocation(cnode.getNodeLoc())
            }

        nodeTracker.trackLocation(node.getNodeLoc())
        refreshParentlessStatus(node)
    }

    fun refreshParentlessStatus(node: IPowerNode) {
        if (node.getParentLoc() == null) {
            parentlessTracker.trackLocation(node.getNodeLoc())
            findParent(node)
        } else {
            parentlessTracker.removeLocation(node.getNodeLoc())
        }
    }

    private fun findParent(node: IPowerNode): Boolean =
        getNodesInRange(nodeTracker, node, node.parentConnectionRadius())
            .filter { (pnode, _) -> pnode.canAddChild(node) && node.canSetParent(pnode) }
            .sortedBy { it.second }
            .any { (pnode, _) -> pnode.addChild(node) && node.setParent(pnode) }

    /**
     * Nodes tracked by [tracker] within [radius] of [node] that are also within their own parent radius and [node]'s
     * children radius, with their squared distance.
     */
    private fun getNodesInRange(tracker: LocationTracker, node: IPowerNode, radius: Float): List<Pair<IPowerNode, Double>> {
        val loc = node.getNodeLoc()
        val childRadius = node.childrenConnectionRadius()
        return tracker.getLocationsInRange(loc, radius)
            .filter { it != loc }
            .mapNotNull { it.getIBlockEntity(false)?.getModule(FemtoModules.POWER_NODE, null) }
            .map { it to it.getNodeLoc().distSqr(loc) }
            .filter { (cnode, dist) ->
                val pr = cnode.parentConnectionRadius()
                dist <= pr * pr && dist <= childRadius * childRadius
            }
            .toList()
    }

    fun removeNode(node: IPowerNode) {
        nodeTracker.removeLocation(node.getNodeLoc())
        parentlessTracker.removeLocation(node.getNodeLoc())
    }
}
