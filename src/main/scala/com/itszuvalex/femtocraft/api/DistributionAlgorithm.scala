package com.itszuvalex.femtocraft.api

class DistributionAlgorithm(val producers: Seq[DistributableResource], val storage: Seq[DistributableResource], val consumer: Seq[DistributableResource]) {

  def distribute(): Unit = {
    var producedResource = 0d
    var storedResource   = 0d
    var storageRoom      = 0d
    var consumerRoom     = 0d

    val producerPowerNodes = producers.map { node =>
      val min = Math.min(node.amt, node.transferMax)
      producedResource += min
      (node, min)
    }.
                                        // Order by nodes with least room.  This prioritizes preventing generators from filling up in power.
                                      sortWith((pairA, pairB) => pairA._1.room < pairB._1.room)
    val storedPowerNodes   = storage.map { node =>
      val min = Math.min(node.amt, node.transferMax)
      storedResource += min
      (node, min)
    }.
                                      // Order by nodes with least room.  This prioritizes preventing storage from filling up in power.
                                    sortWith((pairA, pairB) => pairA._1.room < pairB._1.room)
    val storageRoomNodes   = storage.map { node =>
      val min = Math.min(node.room, node.transferMax)
      storageRoom += min
      (node, min)
    }.
                                      // Order by nodes with least amount.  This prioritizes preventing storage from running out of power.
                                    sortWith((pairA, pairB) => pairA._1.amt < pairB._1.amt)
    // Doesn't matter since storage is assumed equal
    val consumerRoomNodes  = consumer.map { node =>
      val min = Math.min(node.room, node.transferMax)
      consumerRoom += min
      (node, min)
    }. // Order by nodes with least power.  This prioritizes preventing consumers from running out of power.
                                     sortWith((pairA, pairB) => pairA._1.amt < pairB._1.amt)


    // No power left to distribute
    if (producedResource <= 0 && storedResource <= 0) return

    // Nowhere to distribute power to
    if (consumerRoom <= 0 && storageRoom <= 0) return


    //Distribute
    val producerIt    = producerPowerNodes.iterator
    val storageTakeIt = storedPowerNodes.iterator

    def nextResourceSource: (DistributableResource, Double) = {
      if (producerIt.hasNext) {
        producerIt.next()
      }
      else if (storageTakeIt.hasNext) {
        storageTakeIt.next()
      }
      else null
    }

    val consumerIt     = consumerRoomNodes.iterator
    val storageStoreIt = storageRoomNodes.iterator

    def nextResourceSink: (DistributableResource, Double) = {
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
    var powerDistributed  = 0d

    if (producedResource >= consumerRoom) {
      powerToDistribute = Math.min(producedResource, storageRoom + consumerRoom)
    }
    else {
      powerToDistribute = Math.min(consumerRoom, producedResource + storedResource)
    }

    var powerSource  = nextResourceSource
    var powerSink    = nextResourceSink
    var powerToDrain = if (powerSource != null) powerSource._2 else 0d
    var powerToFill  = if (powerSink != null) powerSink._2 else 0d
    while ((powerDistributed < powerToDistribute) && powerSource != null && powerSink != null) {
      var powerShift = Math.min(powerToDrain, powerToFill)
      powerSource._1.remove(powerShift)
      powerToDrain -= powerShift
      powerSink._1.add(powerShift)
      powerToFill -= powerShift
      powerDistributed += powerShift

      if (powerToDrain <= 0d) {
        powerSource = nextResourceSource
        if (powerSource != null)
          powerToDrain = powerSource._2
      }

      if (powerToFill <= 0d) {
        powerSink = nextResourceSink
        if (powerSink != null)
          powerToFill = powerSink._2
      }
    }
  }
}
