package com.itszuvalex.femtocraft.worldgen

import com.itszuvalex.femtocraft.api.worldgen.IRift
import com.itszuvalex.itszulib.logistics.LocationTracker
import com.itszuvalex.itszulib.util.Debug
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.FMLCommonHandler
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.logging.log4j.Level

object FemtocraftRiftTracker {
  val riftLocs = new LocationTracker

  def init(): Unit = {
    MinecraftForge.EVENT_BUS.register(this)
  }

  def registerRift(rift: IRift): Unit = {
    val loc = rift.location
    riftLocs.trackLocation(loc)
    loc.getWorld.foreach{world =>
      world.playSound(loc.x + .5, loc.y + .5, loc.z + .5, )
    }
    Debug.log(Level.INFO, s"Adding Rift at Loc: ${rift.location}")
  }

  def deregisterRift(rift: IRift): Unit = {
    riftLocs.removeLocation(rift.location)
    Debug.log(Level.INFO, s"Removing Rift at Loc: ${rift.location}")
  }

  @SubscribeEvent def onWorldUnload(worldEvent: WorldEvent.Unload): Unit = {
    val server = FMLCommonHandler.instance().getMinecraftServerInstance
    if (server == null || !server.isServerRunning) {
      Debug.log(Level.INFO, s"Removing Rifts due to server stopping.")
      riftLocs.clear()
    }
  }
}
