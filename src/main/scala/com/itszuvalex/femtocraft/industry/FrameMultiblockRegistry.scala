package com.itszuvalex.femtocraft.industry

import scala.collection._

/**
  * Created by Christopher on 8/26/2015.
  */
object FrameMultiblockRegistry {
  private val frameMap = mutable.HashMap[String, IFrameMultiblock]()

  def getMultiblock(name: String) = frameMap.get(name)

  def getMultiblocksForFrameType(ftype: String) = frameMap.values.filter(_.getAllowedFrameTypes.contains(ftype))

  def init(): Unit = {
    //registerMultiblock(new MultiblockGerminationChamber)
  }

  def registerMultiblock(multi: IFrameMultiblock) = frameMap.put(multi.getName, multi)
}
