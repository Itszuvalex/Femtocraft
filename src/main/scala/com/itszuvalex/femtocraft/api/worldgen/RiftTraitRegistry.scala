package com.itszuvalex.femtocraft.api.worldgen

import scala.collection.mutable
import scala.util.Random

object RiftTraitRegistry {
  private val traits = mutable.HashMap[String, IRiftTrait]()

  def registerRiftTrait(riftTrait: IRiftTrait): Unit = traits(riftTrait.name) = riftTrait

  def getRiftTrait(name: String): Option[IRiftTrait] = traits.get(name)

  def generateTraits(rand: Random): Iterable[IRiftTrait] = {
    val ret = mutable.ArrayBuffer[IRiftTrait]()
    traits.values.foreach { t =>
      if (rand.nextFloat() < t.genChance)
        ret += t
                          }
    ret
  }
}
