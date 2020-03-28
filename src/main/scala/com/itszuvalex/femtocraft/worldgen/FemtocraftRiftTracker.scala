package com.itszuvalex.femtocraft.worldgen

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.api.worldgen.IRift
import com.itszuvalex.femtocraft.network.FemtoPacketHandler
import com.itszuvalex.femtocraft.network.messages.MessageRiftSync
import com.itszuvalex.itszulib.api.core.{ChunkCoord, Loc4}
import com.itszuvalex.itszulib.logistics.LocationTracker
import com.itszuvalex.itszulib.util.Debug
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.{ChunkWatchEvent, WorldEvent}
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.logging.log4j.Level

import scala.collection.mutable

object FemtocraftRiftTracker {
  def instance: FemtocraftRiftTracker = Femtocraft.proxy.riftTracker
}

class FemtocraftRiftTracker {
  val riftLocs = new LocationTracker
  val rifts    = new mutable.HashMap[Loc4, IRift]()

  def init(): Unit = {
    MinecraftForge.EVENT_BUS.register(this)
  }

  def registerRift(rift: IRift): Unit = {
    val loc = rift.location
    riftLocs.trackLocation(loc)
    rifts(loc) = rift
    Debug.log(Level.INFO, s"Adding Rift at Loc: ${rift.location}")
  }

  def deregisterRift(rift: IRift): Unit = {
    val loc = rift.location
    riftLocs.removeLocation(loc)
    rifts.remove(loc)
    Debug.log(Level.INFO, s"Removing Rift at Loc: ${rift.location}")
  }

  @SubscribeEvent def onWorldUnload(worldEvent: WorldEvent.Unload): Unit = {
    val server = FMLCommonHandler.instance().getMinecraftServerInstance
    if (server == null || !server.isServerRunning) {
      Debug.log(Level.INFO, s"Removing Rifts due to server stopping.")
      riftLocs.clear()
      rifts.clear()
    }
  }

  def syncRift(rift: IRift, player: EntityPlayerMP): Unit = {
    FemtoPacketHandler.INSTANCE.sendTo(new MessageRiftSync(rift), player)
  }

  @SubscribeEvent def onChunkWatch(watchEvent: ChunkWatchEvent.Watch): Unit = {
    val chunk = watchEvent.getChunk
    riftLocs.getLocationsInChunk(watchEvent.getPlayer.dimension, ChunkCoord(chunk.x, chunk.z)).map(rifts).foreach(syncRift(_, watchEvent.getPlayer))
  }

  @SubscribeEvent def onChunkUnwatch(watchEvent: ChunkWatchEvent.UnWatch): Unit = {
    val chunk = watchEvent.getChunk

  }
}
