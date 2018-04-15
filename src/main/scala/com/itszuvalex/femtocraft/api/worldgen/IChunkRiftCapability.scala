package com.itszuvalex.femtocraft.api.worldgen

import com.itszuvalex.itszulib.api.core.Loc4

trait IChunkRiftCapability {
  def rifts: Iterable[IRift]

  def addRift(rift: IRift): Unit

  def removeRift(rift: IRift): Unit

  def getRiftAtLocation(loc: Loc4): Option[IRift] = {
    rifts.find(_.location == loc)
  }

}
