package com.itszuvalex.femtocraft.api.power

import java.util

import com.itszuvalex.femtocraft.api
import com.itszuvalex.femtocraft.api.{DistributableBattery, DistributionAlgorithm, ManagerModules, power}
import com.itszuvalex.itszulib.api.core.{IModule, Loc4}
import com.itszuvalex.itszulib.logistics.{ManagerNetwork, TileNetwork}
import com.itszuvalex.itszulib.util.Debug
import org.apache.logging.log4j.Level

object PowerNetwork {
  val TICKS_TO_AVERAGE_POWER_OVER: Int = 20 * 10

  def createFromNode(tile: IPowerNetworkNode): PowerNetwork = {
    val network = new PowerNetwork
    network.addNode(tile)
    network
  }

  class Statistics {
    val powerAverageCache     = new Array[Double](PowerNetwork.TICKS_TO_AVERAGE_POWER_OVER)
    var lastTickProducerGen   = 0d
    var lastTickConsumerReq   = 0d
    var lastTickStored        = 0d
    var lastTickStorageMax    = 0d
    var lastTickStorageChange = 0d
    var lastTickTotalStored   = 0d
    var lastTickTotalStorage  = 0d
    var lastTickNetChange     = 0d
    var powerAverageCount     = 0
    var powerAverageInd       = 0
    var producerNodeCount     = 0
    var consumerNodeCount     = 0
    var storageNodeCount      = 0

    def startNewTick(): Unit = {
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

    def addConsumer(node: IPowerStorageNode): Unit = {
      lastTickConsumerReq += node.changeForLastTick
      consumerNodeCount += 1
      lastTickTotalStored += node.battery.storage
      lastTickTotalStorage += node.battery.maxStorage
    }

    def addStorage(node: IPowerStorageNode): Unit = {
      lastTickStored += node.battery.storage
      lastTickStorageMax += node.battery.maxStorage
      lastTickStorageChange += node.changeForLastTick
      lastTickTotalStored += node.battery.storage
      lastTickTotalStorage += node.battery.maxStorage
      storageNodeCount += 1
    }

    def addProducer(node: IPowerStorageNode): Unit = {
      lastTickProducerGen += node.changeForLastTick
      producerNodeCount += 1
      lastTickTotalStored += node.battery.storage
      lastTickTotalStorage += node.battery.maxStorage
    }

    def updatePowerTrend(): Unit = {
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

class PowerNetwork() extends TileNetwork[IPowerNetworkNode, PowerNetwork](ManagerNetwork.instance.getNextID) {
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

  override def networkModule: IModule[IPowerNetworkNode] = ManagerModules.TILE_POWER_NODE

  override def create(): PowerNetwork = new PowerNetwork

  override def onTickStart(): Unit = {}

  override def onTickEnd(): Unit = {
    try {
      statistics.startNewTick()

      val cacheProducerNodes = producerNodes
      val cacheStorageNodes = storageNodes
      val cacheConsumerNodes = consumerNodes

      cacheProducerNodes.foreach(statistics.addProducer)
      cacheStorageNodes.foreach(statistics.addStorage)
      cacheConsumerNodes.foreach(statistics.addConsumer)

      statistics.updatePowerTrend()

      new DistributionAlgorithm(cacheProducerNodes.map(n => DistributableBattery(n.battery, n.transferRate _)).toSeq,
                                cacheStorageNodes.map(n => api.DistributableBattery(n.battery, n.transferRate _)).toSeq,
                                cacheConsumerNodes.map(n => api.DistributableBattery(n.battery, n.transferRate _)).toSeq)
        .distribute()
    }
    catch {
      case e: Throwable => Debug.log(Level.ERROR, e.toString)
    }
  }

  def producerNodes = nodeMap.values.flatMap(_.storageNodes(false)).withFilter(_.storageType == PowerStorageNodeType.PRODUCER)

  def storageNodes = nodeMap.values.flatMap(_.storageNodes(false)).withFilter(_.storageType == PowerStorageNodeType.STORAGE)

  def consumerNodes = nodeMap.values.flatMap(_.storageNodes(false)).withFilter(_.storageType == PowerStorageNodeType.CONSUMER)

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
