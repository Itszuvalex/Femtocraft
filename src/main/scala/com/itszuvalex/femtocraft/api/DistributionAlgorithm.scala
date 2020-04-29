package com.itszuvalex.femtocraft.api


class DistributionAlgorithm[T: Numeric](val producers: Seq[DistributableResource[T]], val storage: Seq[DistributableResource[T]], val consumer: Seq[DistributableResource[T]]) {
  val numeric: Numeric[T] = implicitly[Numeric[T]]

  import scala.math.Numeric.Implicits._
  import scala.math.Ordering.Implicits._

  def distribute(): Unit = {
    var producedResource: T = numeric.zero
    var storedResource  : T = numeric.zero
    var storageRoom     : T = numeric.zero
    var consumerRoom    : T = numeric.zero

    val producerPowerNodes = producers.map { node =>
      val min = numeric.min(node.amt, node.transferMax)
      producedResource += min
      (node, min)
    }.
                                        // Order by nodes with least room.  This prioritizes preventing generators from filling up in power.
                                      sortWith((pairA, pairB) => pairA._1.room < pairB._1.room)
    val storedPowerNodes   = storage.map { node =>
      val min = numeric.min(node.amt, node.transferMax)
      storedResource += min
      (node, min)
    }.
                                      // Order by nodes with least room.  This prioritizes preventing storage from filling up in power.
                                    sortWith((pairA, pairB) => pairA._1.room < pairB._1.room)
    val storageRoomNodes   = storage.map { node =>
      val min = numeric.min(node.room, node.transferMax)
      storageRoom += min
      (node, min)
    }.
                                      // Order by nodes with least amount.  This prioritizes preventing storage from running out of power.
                                    sortWith((pairA, pairB) => pairA._1.amt < pairB._1.amt)
    // Doesn't matter since storage is assumed equal
    val consumerRoomNodes  = consumer.map { node =>
      val min = numeric.min(node.room, node.transferMax)
      consumerRoom += min
      (node, min)
    }. // Order by nodes with least power.  This prioritizes preventing consumers from running out of power.
                                     sortWith((pairA, pairB) => pairA._1.amt < pairB._1.amt)


    // No power left to distribute
    if (producedResource <= numeric.zero && storedResource <= numeric.zero) return

    // Nowhere to distribute power to
    if (consumerRoom <= numeric.zero && storageRoom <= numeric.zero) return


    //Distribute
    val producerIt    = producerPowerNodes.iterator
    val storageTakeIt = storedPowerNodes.iterator

    def nextResourceSource: (DistributableResource[T], T) = {
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

    def nextResourceSink: (DistributableResource[T], T) = {
      if (consumerIt.hasNext) {
        consumerIt.next()
      }
      else if (storageStoreIt.hasNext) {
        storageStoreIt.next()
      }
      else null
    }

    //Freely distribute, since all requests should be fulfilled
    var powerToDistribute = numeric.zero
    var powerDistributed  = numeric.zero

    if (producedResource >= consumerRoom) {
      powerToDistribute = numeric.min(producedResource, storageRoom + consumerRoom)
    }
    else {
      powerToDistribute = numeric.min(consumerRoom, producedResource + storedResource)
    }

    var powerSource  = nextResourceSource
    var powerSink    = nextResourceSink
    var powerToDrain = if (powerSource != null) powerSource._2 else numeric.zero
    var powerToFill  = if (powerSink != null) powerSink._2 else numeric.zero
    while ((powerDistributed < powerToDistribute) && powerSource != null && powerSink != null) {
      var powerShift = numeric.min(powerToDrain, powerToFill)
      powerSource._1.remove(powerShift)
      powerToDrain -= powerShift
      powerSink._1.add(powerShift)
      powerToFill -= powerShift
      powerDistributed += powerShift

      if (powerToDrain <= numeric.zero) {
        powerSource = nextResourceSource
        if (powerSource != null)
          powerToDrain = powerSource._2
      }

      if (powerToFill <= numeric.zero) {
        powerSink = nextResourceSink
        if (powerSink != null)
          powerToFill = powerSink._2
      }
    }
  }
}
