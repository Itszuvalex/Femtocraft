package com.itszuvalex.femtocraft.nanite

import com.itszuvalex.femtocraft.FemtoModules
import com.itszuvalex.itszulib.api.utility.Loc4
import com.itszuvalex.itszulib.api.utility.LocationTracker

/**
 * A nanite hive: nanite nodes within [connectionRadius] attach to it. Port of 1.7.10 `INaniteHive`.
 */
interface INaniteHive {
    fun getType(): String
    fun getHiveLoc(): Loc4
    fun getNodeLocs(): Set<Loc4>
    fun getNodes(): List<INaniteNode>
    fun addNode(node: INaniteNode): Boolean
    fun canAddNode(node: INaniteNode): Boolean
    fun removeNode(node: INaniteNode)
    fun connectionRadius(): Float
}

/**
 * Something served by a nanite hive (e.g. the item repository). Port of 1.7.10 `INaniteNode`.
 */
interface INaniteNode {
    fun getNodeLoc(): Loc4
    fun getHiveLoc(): Loc4?
    fun getHive(): INaniteHive?
    fun setHive(hive: INaniteHive?): Boolean
    fun canSetHive(hive: INaniteHive): Boolean
    fun hiveConnectionRadius(): Float
}

/**
 * Server-side index of hives and hive-less nodes. Port of 1.7.10 `NaniteManager`. Cleared when the server stops.
 */
object NaniteManager {
    private val hiveTracker = LocationTracker()
    private val parentlessNodeTracker = LocationTracker()

    fun addHive(hive: INaniteHive) {
        hiveTracker.trackLocation(hive.getHiveLoc())
        findNodes(hive)
    }

    /**
     * Attaches hive-less nodes in range of both the hive's and the node's radius.
     */
    fun findNodes(hive: INaniteHive) {
        val loc = hive.getHiveLoc()
        parentlessNodeTracker.getLocationsInRange(loc, hive.connectionRadius())
            .filter { it != loc }
            .mapNotNull { it.getIBlockEntity(false)?.getModule(FemtoModules.NANITE_NODE, null) }
            .filter { it.getHiveLoc() == null }
            .filter { it.getNodeLoc().distSqr(loc) < it.hiveConnectionRadius() * it.hiveConnectionRadius() }
            .toList()
            .forEach { node ->
                if (node.canSetHive(hive) && hive.addNode(node) && node.setHive(hive)) refreshParentlessStatus(node)
            }
    }

    fun removeHive(hive: INaniteHive) = hiveTracker.removeLocation(hive.getHiveLoc())

    fun addNode(node: INaniteNode) {
        if (node.getHiveLoc() == null) findParent(node)
        refreshParentlessStatus(node)
    }

    fun refreshParentlessStatus(node: INaniteNode) {
        if (node.getHiveLoc() == null) parentlessNodeTracker.trackLocation(node.getNodeLoc())
        else parentlessNodeTracker.removeLocation(node.getNodeLoc())
    }

    private fun findParent(node: INaniteNode): Boolean {
        val loc = node.getNodeLoc()
        return hiveTracker.getLocationsInRange(loc, node.hiveConnectionRadius())
            .filter { it != loc }
            .mapNotNull { it.getIBlockEntity(false)?.getModule(FemtoModules.NANITE_HIVE, null) }
            .map { it to it.getHiveLoc().distSqr(loc) }
            .filter { (hive, dist) -> dist <= hive.connectionRadius() * hive.connectionRadius() }
            .filter { (hive, _) -> hive.canAddNode(node) && node.canSetHive(hive) }
            .sortedBy { it.second }
            .any { (hive, _) -> hive.addNode(node) && node.setHive(hive) }
    }

    fun removeNode(node: INaniteNode) = parentlessNodeTracker.removeLocation(node.getNodeLoc())

    fun clear() {
        hiveTracker.clear()
        parentlessNodeTracker.clear()
    }
}
