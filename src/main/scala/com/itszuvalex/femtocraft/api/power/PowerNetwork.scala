package com.itszuvalex.femtocraft.api.power

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.logistics.{ManagerNetwork, TileNetwork}
import com.itszuvalex.itszulib.util.Debug
import net.minecraftforge.common.capabilities.Capability
import org.apache.logging.log4j.Level

import scala.collection.mutable

object PowerNetwork {
  def createFromTile(tile: IPowerNetworkNode): PowerNetwork = {
    val network = new PowerNetwork
    network.addNode(tile)
    network
  }
}

class PowerNetwork() extends TileNetwork[IPowerNetworkNode, PowerNetwork](ManagerNetwork.getNextID) {
  val producerSet: mutable.HashSet[IPowerNetworkNode] = new mutable.HashSet[IPowerNetworkNode]()
  val storageSet : mutable.HashSet[IPowerNetworkNode] = new mutable.HashSet[IPowerNetworkNode]()
  val consumerSet: mutable.HashSet[IPowerNetworkNode] = new mutable.HashSet[IPowerNetworkNode]()

  override def networkCapability: Capability[IPowerNetworkNode] = Capabilities.POWER_NODE

  override def create(): PowerNetwork = new PowerNetwork

  override def onTickStart(): Unit = {}

  override def onTickEnd(): Unit = {
    try {
      var producedPower = 0d
      var storedPower = 0d
      var storageRoom = 0d
      var consumerRoom = 0d

      val producerPowerNodes = producerSet.map { node =>
        val min = Math.min(node.storage.storage, node.transferRate)
        producedPower += min
        (node, min)
      }.toSeq.sortWith(_._2 > _._2)
      val storedPowerNodes = storageSet.map { node =>
        val min = Math.min(node.storage.storage, node.transferRate)
        storedPower += min
        (node, min)
      }.toSeq.sortWith(_._2 > _._2)
      val storageRoomNodes = storageSet.map { node =>
        val min = Math.min(node.storage.maxStorage - node.storage.storage, node.transferRate)
        storageRoom += min
        (node, min)
      }.toSeq.sortWith(_._2 > _._2)
      val consumerRoomNodes = consumerSet.map { node =>
        val min = Math.min(node.storage.maxStorage - node.storage.storage, node.transferRate)
        consumerRoom += min
        (node, min)
      }.toSeq.sortWith(_._2 > _._2)

      // Return early to prevent unnecessary computation

      // No power left to distribute
      if (producedPower <= 0 && storedPower <= 0) return

      // Nowhere to distribute power to
      if (consumerRoom <= 0 && storageRoom <= 0) return

      //Distribute
      val producerIt = producerPowerNodes.iterator
      val storageTakeIt = storageRoomNodes.iterator

      def nextPowerSource: (IPowerNetworkNode, Double) = {
        if (producerIt.hasNext) {
          producerIt.next()
        }
        else if (storageTakeIt.hasNext) {
          storageTakeIt.next()
        }
        else null
      }

      val consumerIt = consumerRoomNodes.iterator
      val storageStoreIt = storedPowerNodes.iterator

      def nextPowerSink: (IPowerNetworkNode, Double) = {
        if (consumerIt.hasNext) {
          consumerIt.next()
        }
        else if (storageStoreIt.hasNext) {
          storageStoreIt.next()
        }
        else null
      }

      //Freely distribute, since all requests should be fulfilled
      var powerToDistribute = 0d
      var powerDistributed = 0d

      if (producedPower >= consumerRoom) {
        powerToDistribute = Math.min(producedPower, storageRoom + consumerRoom)
      }
      else {
        powerToDistribute = Math.min(consumerRoom, producedPower + storedPower)
      }

      var powerSource = nextPowerSource
      var powerSink = nextPowerSink
      var powerToDrain = 0d
      var powerToFill = 0d
      while ((powerDistributed < powerToDistribute) && powerSource != null && powerSink != null) {
        var powerShift = Math.min(powerToDrain, powerToFill)
        powerSource._1.storage.storage -= powerShift
        powerToDrain -= powerShift
        powerSink._1.storage.storage += powerShift
        powerToFill -= powerShift
        powerDistributed += powerShift

        if (powerToDrain <= 0d) {
          powerSource = nextPowerSource
          if (powerSource != null)
            powerToDrain = powerSource._2
        }

        if (powerToFill <= 0d) {
          powerSink = nextPowerSink
          if (powerSink != null)
            powerToFill = powerSink._2
        }
      }
    }
    catch {
      case e: Throwable => Debug.log(Level.ERROR, e.toString)
    }
  }

  override def onTakeover(iNetwork: PowerNetwork): Unit = {
    producerSet ++= iNetwork.producerSet
    storageSet ++= iNetwork.storageSet
    consumerSet ++= iNetwork.consumerSet
  }

  override def onSplit(iNetwork: PowerNetwork): Unit = {

  }

  override def addNodeSilently(node: IPowerNetworkNode): Unit = {
    super.addNodeSilently(node)
    node.storageType match {
      case PowerStorageNodeType.PRODUCER => producerSet += node
      case PowerStorageNodeType.STORAGE => storageSet += node
      case PowerStorageNodeType.CONSUMER => consumerSet += node
      case _ =>
    }
  }

  override def refresh(): Unit = {
    super.refresh()
    val mst = MinimalSpanningTree.calculate(this)
    mst.foreach { case (a: Loc4, b: scala.collection.Set[Loc4]) =>
      nodeMap(a).setRenderLocations(b)
    }
  }

  override def addNode(node: IPowerNetworkNode): Unit = {
    super.addNode(node)

    val mst = MinimalSpanningTree.calculate(this)
    mst.foreach { case (a: Loc4, b: scala.collection.Set[Loc4]) =>
      nodeMap.get(a).filter(_.rendersConnections).foreach(_.setRenderLocations(b))
    }
  }
}
