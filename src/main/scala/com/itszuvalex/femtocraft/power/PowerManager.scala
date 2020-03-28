package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.api.power.{IPowerLeafNode, IPowerNetworkNode, PowerNetwork}
import com.itszuvalex.itszulib.api.core.{IModule, Loc4}
import com.itszuvalex.itszulib.logistics.LocationTracker
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
  * Created by Christopher Harris (Itszuvalex) on 8/3/15.
  */
object PowerManager {
  def instance: PowerManager = Femtocraft.proxy.powerManager
}

class PowerManager {
  val nodeTracker = new LocationTracker
  val leafTracker = new LocationTracker

  def init(): Unit = {
    MinecraftForge.EVENT_BUS.register(this)
  }

  /**
    * Attempts to add node to the IPowerNetworkNode mapping.  If the node has no parent location, PowerManager will attempt to find a parent for it.  If one is not found,
    * it will add the node to its parentless list, and will then try to find a parent for it every time a new node is added.
    *
    * @param node Node to be added.
    */
  def addNode(node: IPowerNetworkNode): Unit = {
    val loc = node.getLoc

    val nodes = getIPowerNetworkNodesInRange(nodeTracker, node.getLoc, ManagerModules.TILE_POWER_NODE, node.connectionRadius).filterNot(_.getLoc.compareTo(loc) == 0).toSet
    if (nodes.isEmpty) {
      val network = PowerNetwork.createFromNode(node)
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

  def refreshLeafsOnMain(node: IPowerNetworkNode): Unit = {
    val leafs = getIPowerNetworkNodesInRange(leafTracker, node.getLoc, ManagerModules.TILE_POWER_LEAF_NODE, node.connectionRadius).filterNot(_.getStorageLoc.compareTo(node.getLoc) == 0).toSet
    leafs.view.filter(_.getParent == null).
         filter(l => l.getStorageLoc.distSqr(node.getLoc) <= (l.connectionRadius * l.connectionRadius)) // Don't need to check own connection radius
         .filter(l => l.canSetParent(node) && node.canAddLeafNode(l)).
         toSeq.sortBy(_.getStorageLoc.distSqr(node.getLoc))
         .foreach { l =>
           node.addLeafNode(l)
           l.setParent(node)
         }
  }

  def addLeaf(node: IPowerLeafNode): Unit = {
    refreshLeaf(node)
    leafTracker.trackLocation(node.getStorageLoc)
  }

  def refreshLeaf(node: IPowerLeafNode): Unit = {
    if (node.getParent != null) return

    val nodes = getIPowerNetworkNodesInRange(nodeTracker, node.getStorageLoc, ManagerModules.TILE_POWER_NODE, node.connectionRadius).filterNot(_.getLoc.compareTo(node.getStorageLoc) == 0).toSet
    nodes.view.filter(l => l.getLoc.distSqr(node.getStorageLoc) <= l.connectionRadius * l.connectionRadius).filter(l => l.canAddLeafNode(node) && node.canSetParent(l)).
         toSeq.sortBy(_.getLoc.distSqr(node.getStorageLoc)).
         foreach { n =>
           n.addLeafNode(node)
           node.setParent(n)
           return // Only do this once.
         }
  }

  private def getIPowerNetworkNodesInRange[T](tracker: LocationTracker, loc: Loc4, module: IModule[T], radius: Float): Iterable[T] = {
    tracker.getLocationsInRange(loc, radius).view
           .withFilter(_.compareTo(loc) != 0)
           .flatMap(_.getITileEntity(force = false))
           .withFilter(_.hasModule(module, null))
           .map(_.getModule(module, null))
  }

  def removeNode(node: IPowerNetworkNode): Unit = {
    nodeTracker.removeLocation(node.getLoc)
    if (node.getNetwork != null)
      node.getNetwork.removeNode(node)
    node.setNetwork(null)
  }

  def onNodeBroken(node: IPowerNetworkNode): Unit = {
    node.leafNodes(true).foreach(_.onParentBroken(node))
  }

  def removeLeaf(node: IPowerLeafNode): Unit = {
    leafTracker.removeLocation(node.getStorageLoc)
  }

  def onLeafBroken(node: IPowerLeafNode): Unit = {
    Option(node.getParent).flatMap(_.getITileEntity(true)).withFilter(_.hasModule(ManagerModules.TILE_POWER_NODE, null)).map(_.getModule(ManagerModules.TILE_POWER_NODE, null)).foreach(_.removeLeafNode(node))
  }

  @SubscribeEvent def onWorldUnload(worldEvent: WorldEvent.Unload): Unit = {
    val server = FMLCommonHandler.instance().getMinecraftServerInstance
    if (server == null || !server.isServerRunning) {
      clear()
    }
  }

  def clear() = {
    nodeTracker.clear()
    leafTracker.clear()
  }

}
