package com.itszuvalex.femtocraft.api.power

import java.util

import com.itszuvalex.femtocraft.api.{Capabilities, power}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.logistics.{ManagerNetwork, TileNetwork}
import com.itszuvalex.itszulib.util.Debug
import net.minecraftforge.common.capabilities.Capability
import org.apache.logging.log4j.Level

object PowerNetwork {
  val TICKS_TO_AVERAGE_POWER_OVER: Int = 20 * 10

  def createFromTile(tile: IPowerNetworkNode): PowerNetwork = {
    val network = new PowerNetwork
    network.addNode(tile)
    network
  }

  class Statistics {
    var lastTickProducerGen   = 0d
    var lastTickConsumerReq   = 0d
    var lastTickStored        = 0d
    var lastTickStorageMax    = 0d
    var lastTickStorageChange = 0d
    var lastTickTotalStored   = 0d
    var lastTickTotalStorage  = 0d
    var lastTickNetChange     = 0d
    val powerAverageCache     = new Array[Double](PowerNetwork.TICKS_TO_AVERAGE_POWER_OVER)
    var powerAverageCount     = 0
    var powerAverageInd       = 0
    var producerNodeCount     = 0
    var consumerNodeCount     = 0
    var storageNodeCount      = 0

    def startNewTick() = {
      lastTickProducerGen = 0d
      lastTickConsumerReq = 0d
      lastTickStorageChange = 0d
      lastTickStored = 0d
      lastTickStorageMax = 0d
      lastTickTotalStored = 0d
      lastTickTotalStorage = 0d
      consumerNodeCount = 0
      producerNodeCount = 0
      storageNodeCount = 0
    }

    def countProducers: Int = producerNodeCount

    def countConsumer: Int = consumerNodeCount

    def countStorage: Int = storageNodeCount

    def powerProducedLastTick: Double = lastTickProducerGen

    def powerConsumedLastTick: Double = lastTickConsumerReq

    def dedicatedPowerStored: Double = lastTickStored

    def dedicatedPowerStorage: Double = lastTickStorageMax

    def totalPowerStored: Double = lastTickTotalStored

    def totalPowerStorage: Double = lastTickTotalStorage

    def powerStorageDelta: Double = lastTickStorageChange

    def lastTickNetworkDelta: Double = lastTickNetChange

    def averagePowerTrend: Double = if (powerAverageCount == 0) 0d else powerAverageCache.map(_ / powerAverageCount).sum

    def addConsumer(node: IPowerStorageNode) = {
      lastTickConsumerReq += node.changeForLastTick
      consumerNodeCount += 1
      lastTickTotalStored += node.battery.storage
      lastTickTotalStorage += node.battery.maxStorage
    }

    def addStorage(node: IPowerStorageNode) = {
      lastTickStored += node.battery.storage
      lastTickStorageMax += node.battery.maxStorage
      lastTickStorageChange += node.changeForLastTick
      lastTickTotalStored += node.battery.storage
      lastTickTotalStorage += node.battery.maxStorage
      storageNodeCount += 1
    }

    def addProducer(node: IPowerStorageNode) = {
      lastTickProducerGen += node.changeForLastTick
      producerNodeCount += 1
      lastTickTotalStored += node.battery.storage
      lastTickTotalStorage += node.battery.maxStorage
    }

    def updatePowerTrend() = {
      lastTickNetChange = lastTickProducerGen + lastTickConsumerReq
      trackPowerTrend(lastTickNetChange)
    }

    private def trackPowerTrend(a: Double): Unit = {
      powerAverageCache(powerAverageInd) = a
      powerAverageCount = Math.min(PowerNetwork.TICKS_TO_AVERAGE_POWER_OVER, powerAverageCount + 1)
      powerAverageInd = (powerAverageInd + 1) % PowerNetwork.TICKS_TO_AVERAGE_POWER_OVER
    }
  }

}

class PowerNetwork() extends TileNetwork[IPowerNetworkNode, PowerNetwork](ManagerNetwork.getNextID) {
  val statistics = new power.PowerNetwork.Statistics

  def countProducers: Int = statistics.countProducers

  def countConsumer: Int = statistics.countConsumer

  def countStorage: Int = statistics.countStorage

  def powerProducedLastTick: Double = statistics.powerProducedLastTick

  def powerConsumedLastTick: Double = statistics.powerConsumedLastTick

  def dedicatedPowerStored: Double = statistics.dedicatedPowerStored

  def dedicatedPowerStorage: Double = statistics.dedicatedPowerStorage

  def totalPowerStored: Double = statistics.totalPowerStored

  def totalPowerStorage: Double = statistics.totalPowerStorage

  def powerStorageDelta: Double = statistics.powerStorageDelta

  def lastTickNetworkDelta: Double = statistics.lastTickNetworkDelta

  def averagePowerTrend: Double = statistics.averagePowerTrend

  override def networkCapability: Capability[IPowerNetworkNode] = Capabilities.TILE_POWER_NODE

  override def create(): PowerNetwork = new PowerNetwork

  override def onTickStart(): Unit = {}

  def producerNodes = nodeMap.values.flatMap(_.storageNodes(false)).withFilter(_.storageType == PowerStorageNodeType.PRODUCER)

  def storageNodes = nodeMap.values.flatMap(_.storageNodes(false)).withFilter(_.storageType == PowerStorageNodeType.STORAGE)

  def consumerNodes = nodeMap.values.flatMap(_.storageNodes(false)).withFilter(_.storageType == PowerStorageNodeType.CONSUMER)

  override def onTickEnd(): Unit = {
    try {
      var producedPower = 0d
      var storedPower = 0d
      var storageRoom = 0d
      var consumerRoom = 0d

      statistics.startNewTick()

      val cacheStorageNodes = storageNodes

      val producerPowerNodes = producerNodes.map { node =>
        val min = Math.min(node.battery.storage, node.transferRate)
        producedPower += min
        statistics.addProducer(node)
        (node, min)
      }.toSeq.
        // Order by nodes with least room.  This prioritizes preventing generators from filling up in power.
        sortWith((pairA, pairB) => (pairA._1.battery.maxStorage - pairA._1.battery.storage) < (pairB._1.battery.maxStorage - pairB._1.battery.storage))
      val storedPowerNodes = cacheStorageNodes.map { node =>
        val min = Math.min(node.battery.storage, node.transferRate)
        storedPower += min
        statistics.addStorage(node)
        (node, min)
      }.toSeq.
        // Order by nodes with least room.  This prioritizes preventing storage from filling up in power.
        sortWith((pairA, pairB) => (pairA._1.battery.maxStorage - pairA._1.battery.storage) < (pairB._1.battery.maxStorage - pairB._1.battery.storage))
      val storageRoomNodes = cacheStorageNodes.map { node =>
        val min = Math.min(node.battery.maxStorage - node.battery.storage, node.transferRate)
        storageRoom += min
        (node, min)
      }.toSeq.
        // Order by nodes with least power.  This prioritizes preventing storage from running out of power.
        sortWith((pairA, pairB) => pairA._1.battery.storage < pairB._1.battery.storage)
      // Doesn't matter since storage is assumed equal
      val consumerRoomNodes = consumerNodes.map { node =>
        val min = Math.min(node.battery.maxStorage - node.battery.storage, node.transferRate)
        consumerRoom += min
        statistics.addConsumer(node)
        (node, min)
      }.toSeq.
        // Order by nodes with least power.  This prioritizes preventing consumers from running out of power.
        sortWith((pairA, pairB) => pairA._1.battery.storage < pairB._1.battery.storage)

      // Return early to prevent unnecessary computation

      statistics.updatePowerTrend()

      // No power left to distribute
      if (producedPower <= 0 && storedPower <= 0) return

      // Nowhere to distribute power to
      if (consumerRoom <= 0 && storageRoom <= 0) return

      //Distribute
      val producerIt = producerPowerNodes.iterator
      val storageTakeIt = storedPowerNodes.iterator

      def nextPowerSource: (IPowerStorageNode, Double) = {
        if (producerIt.hasNext) {
          producerIt.next()
        }
        else if (storageTakeIt.hasNext) {
          storageTakeIt.next()
        }
        else null
      }

      val consumerIt = consumerRoomNodes.iterator
      val storageStoreIt = storageRoomNodes.iterator

      def nextPowerSink: (IPowerStorageNode, Double) = {
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
      var powerToDrain = if (powerSource != null) powerSource._2 else 0d
      var powerToFill = if (powerSink != null) powerSink._2 else 0d
      while ((powerDistributed < powerToDistribute) && powerSource != null && powerSink != null) {
        var powerShift = Math.min(powerToDrain, powerToFill)
        powerSource._1.battery.storage -= powerShift
        powerToDrain -= powerShift
        powerSink._1.battery.storage += powerShift
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

  override def onTakeover(iNetwork: PowerNetwork): Unit = {}

  override def onSplit(iNetwork: PowerNetwork): Unit = {}

  override def removeNodes(nodes: util.Collection[IPowerNetworkNode]): Unit = {
    super.removeNodes(nodes)

    val mst = MinimalSpanningTree.calculate(this)
    mst.foreach { case (a: Loc4, b: scala.collection.Set[Loc4]) =>
      nodeMap.get(a).filter(_.rendersConnections).foreach(_.setRenderLocations(b))
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
