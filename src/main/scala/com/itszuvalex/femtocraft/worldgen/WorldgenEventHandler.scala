package com.itszuvalex.femtocraft.worldgen

import net.minecraftforge.event.world.{ChunkDataEvent, ChunkEvent}
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class WorldgenEventHandler {
  @SubscribeEvent
  def onChunkDataLoad(chunkDataEvent: ChunkDataEvent.Load): Unit = {
    // Manually load nbt here
  }

  @SubscribeEvent
  def onChunkDataSave(chunkDataEvent: ChunkDataEvent.Load): Unit = {
    // Manually save nbt here
  }

  @SubscribeEvent
  def onChunkUnload(chunkEvent: ChunkEvent.Unload): Unit = {
    // Unload with no nbt
  }

  @SubscribeEvent
  def onChunkLoad(chunkEvent: ChunkEvent.Load): Unit = {
    // Load with no nbt
  }
}
