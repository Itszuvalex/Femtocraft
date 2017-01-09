package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.power.{IPowerLeafNode, IPowerNetworkNode, PowerNetwork}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.logistics.LocationTracker
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
  * Created by Christopher Harris (Itszuvalex) on 8/3/15.
  */
object PowerManager {
  val nodeTracker = new LocationTracker
  val leafTracker = new LocationTracker

  def init(): Unit = {
    MinecraftForge.EVENT_BUS.register(this)
  }

  def clear() = {
    nodeTracker.clear()
    leafTracker.clear()
  }

  /**
    * Attempts to add node to the IPowerNetworkNode mapping.  If the node has no parent location, PowerManager will attempt to find a parent for it.  If one is not found,
    * it will add the node to its parentless list, and will then try to find a parent for it every time a new node is added.
    *
    * @param node Node to be added.
    */
  def addNode(node: IPowerNetworkNode): Unit = {
    val loc = node.getLoc

    val nodes = getIPowerNetworkNodesInRange(nodeTracker, node.getLoc, Capabilities.TILE_POWER_NODE, node.connectionRadius).filterNot(_.getLoc.compareTo(loc) == 0).toSet
    if (nodes.isEmpty) {
      val network = PowerNetwork.createFromTile(node)
      network.register()
    }
    else {
      nodes.withFilter(l => l.getLoc.distSqr(node.getLoc) <= (l.connectionRadius * l.connectionRadius)).withFilter(n => n.canConnect(loc) && node.canConnect(n.getLoc)).
        foreach { nloc =>
          Option(nloc.getNetwork).foreach(_.addNode(node))
        }
    }

    refreshLeafsOnMain(node)

    /* Actually track the node */
    nodeTracker.trackLocation(loc)
  }

  def addLeaf(node: IPowerLeafNode): Unit = {
    refreshLeaf(node)
    leafTracker.trackLocation(node.getStorageLoc)
  }

  def removeNode(node: IPowerNetworkNode): Unit = {
    nodeTracker.removeLocation(node.getLoc)
    if (node.getNetwork != null)
      node.getNetwork.removeNode(node)
    node.setNetwork(null)
  }

  def removeLeaf(node: IPowerLeafNode): Unit = {
    leafTracker.removeLocation(node.getStorageLoc)
  }

  def refreshLeafsOnMain(node: IPowerNetworkNode): Unit = {
    val leafs = getIPowerNetworkNodesInRange(leafTracker, node.getLoc, Capabilities.TILE_POWER_LEAF_NODE, node.connectionRadius).filterNot(_.getStorageLoc.compareTo(node.getLoc) == 0).toSet
    leafs.view.filter(_.getParent == null).
      filter(l => l.getStorageLoc.distSqr(node.getLoc) <= (l.connectionRadius * l.connectionRadius)) // Don't need to check own connection radius
      .filter(l => l.canSetParent(node) && node.canAddLeafNode(l)).
      toSeq.sortBy(_.getStorageLoc.distSqr(node.getLoc))
      .foreach { l =>
        node.addLeafNode(l)
        l.setParent(node)
      }
  }

  def refreshLeaf(node: IPowerLeafNode): Unit = {
    if (node.getParent != null) return

    val nodes = getIPowerNetworkNodesInRange(nodeTracker, node.getStorageLoc, Capabilities.TILE_POWER_NODE, node.connectionRadius).filterNot(_.getLoc.compareTo(node.getStorageLoc) == 0).toSet
    nodes.view.filter(l => l.getLoc.distSqr(node.getStorageLoc) <= l.connectionRadius * l.connectionRadius).filter(l => l.canAddLeafNode(node) && node.canSetParent(l)).
      toSeq.sortBy(_.getLoc.distSqr(node.getStorageLoc)).
      foreach { n =>
        n.addLeafNode(node)
        node.setParent(n)
        return // Only do this once.
      }
  }

  private def getIPowerNetworkNodesInRange[T](tracker: LocationTracker, loc: Loc4, capability: Capability[T], radius: Float): Iterable[T] = {
    tracker.getLocationsInRange(loc, radius).view
      .withFilter(_.compareTo(loc) != 0)
      .flatMap(_.getTileEntity(force = false))
      .withFilter(_.hasCapability(capability, null))
      .map(_.getCapability(capability, null))
  }


  @SubscribeEvent def onWorldUnload(worldEvent: WorldEvent.Unload): Unit = {
    if (!FMLCommonHandler.instance().getMinecraftServerInstance.isServerRunning) {
      clear()
    }
  }

}
