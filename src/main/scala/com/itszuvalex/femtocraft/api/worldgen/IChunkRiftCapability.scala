package com.itszuvalex.femtocraft.api.worldgen


trait IChunkRiftCapability {
  def rifts: Iterable[IRift]

  def addRift(rift: IRift): Unit

  def removeRift(rift: IRift): Unit

}
