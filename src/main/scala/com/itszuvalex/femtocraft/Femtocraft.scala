package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.network.PacketHandler
import com.itszuvalex.femtocraft.proxy.ProxyCommon
import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.{Mod, SidedProxy}
import org.apache.logging.log4j.LogManager

/**
 * Created by Christopher on 4/5/2015.
 */
@Mod(modid = Femtocraft.ID, name = Femtocraft.ID, version = Femtocraft.VERSION, modLanguage = "scala")
object Femtocraft {
  final val ID      = "Femtocraft"
  final val VERSION = Version.FULL_VERSION
  final val logger  = LogManager.getLogger(ID)

  @SidedProxy(clientSide = "com.itszuvalex.femtocraft.proxy.ProxyClient",
              serverSide = "com.itszuvalex.femtocraft.proxy.ProxyServer")
  var proxy: ProxyCommon = null

  @EventHandler def preInit(event: FMLPreInitializationEvent): Unit = {
    PacketHandler.init()
    proxy.init()
  }

  @EventHandler def load(event: FMLInitializationEvent): Unit = {
  }

  @EventHandler def postInit(event: FMLPostInitializationEvent): Unit = {

  }
}
