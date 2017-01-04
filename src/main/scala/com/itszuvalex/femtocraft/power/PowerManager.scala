package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.power.{IPowerNetworkNode, PowerNetwork}
import com.itszuvalex.itszulib.logistics.LocationTracker
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
  * Created by Christopher Harris (Itszuvalex) on 8/3/15.
  */
object PowerManager {
  val nodeTracker = new LocationTracker

  def init(): Unit = {
    MinecraftForge.EVENT_BUS.register(this)
  }

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

    val nodes = getIPowerNetworkNodesInRange(nodeTracker, node, node.connectionRadius).filterNot(_._1.getLoc.compareTo(loc) == 0).toSet
    if (nodes.isEmpty) {
      val network = PowerNetwork.createFromTile(node)
      network.register()
    }
    else {
      nodes.map(_._1).withFilter(_.canConnect(loc)).foreach { nloc =>
        Option(nloc.getNetwork).foreach(_.addNode(node))
      }
    }

    /* Actually track the node */
    nodeTracker.trackLocation(loc)
  }

  def removeNode(node: IPowerNetworkNode): Unit = {
    nodeTracker.removeLocation(node.getLoc)
    if (node.getNetwork != null)
      node.getNetwork.removeNode(node)
    node.setNetwork(null)
  }


  private def getIPowerNetworkNodesInRange(tracker: LocationTracker, node: IPowerNetworkNode, radius: Float): Iterable[(IPowerNetworkNode, Double)] = {
    val loc = node.getLoc
    tracker.getLocationsInRange(loc, radius).view
      .filterNot(_.compareTo(node.getLoc) == 0)
      .flatMap(_.getTileEntity(force = false))
      .filter(_.hasCapability(Capabilities.POWER_NODE, EnumFacing.UP))
      .map {_.getCapability(Capabilities.POWER_NODE, EnumFacing.UP)}
      .map(cnode => (cnode, cnode.getLoc.distSqr(loc)))
      .filter(pair => (pair._2 <= (pair._1.connectionRadius * pair._1.connectionRadius)) &&
        (pair._2 <= (node.connectionRadius * node.connectionRadius)))
  }


  @SubscribeEvent def onWorldUnload(worldEvent: WorldEvent.Unload): Unit = {
    if (!FMLCommonHandler.instance().getMinecraftServerInstance.isServerRunning) {
      clear()
    }
  }

}
