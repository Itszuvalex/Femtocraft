package com.itszuvalex.femtocraft.core

import com.itszuvalex.femtocraft.industry.multiblock.{MultiblockArcFurnace, MultiblockCentrifuge, MultiblockCrystallizationChamber}

import scala.collection._

/**
 * Created by Christopher on 8/26/2015.
 */
object FrameMultiblockRegistry {
  private val frameMap = mutable.HashMap[String, IFrameMultiblock]()

  def registerMultiblock(multi: IFrameMultiblock) = frameMap.put(multi.getName, multi)

  def getMultiblock(name: String) = frameMap.get(name)

  def getMultiblocksForFrameType(ftype: String) = frameMap.values.filter(_.getAllowedFrameTypes.contains(ftype))

  def init(): Unit = {
    registerMultiblock(new MultiblockArcFurnace)
    registerMultiblock(new MultiblockCentrifuge)
    registerMultiblock(new MultiblockCrystallizationChamber)
  }
}
