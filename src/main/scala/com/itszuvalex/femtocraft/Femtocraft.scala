package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.core.NaniteRegistry
import com.itszuvalex.femtocraft.network.PacketHandler
import com.itszuvalex.femtocraft.proxy.ProxyCommon
import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.{Mod, SidedProxy}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.Items
import net.minecraft.item.Item
import org.apache.logging.log4j.LogManager

/**
 * Created by Christopher on 4/5/2015.
 */
@Mod(modid = Femtocraft.ID, name = Femtocraft.ID, version = Femtocraft.VERSION, modLanguage = "scala")
object Femtocraft {
  final val ID      = "Femtocraft"
  final val VERSION = Version.FULL_VERSION
  final val logger  = LogManager.getLogger(ID)
  final val blocks  = FemtoBlocks
  final val items   = FemtoItems

  @SidedProxy(clientSide = "com.itszuvalex.femtocraft.proxy.ProxyClient",
              serverSide = "com.itszuvalex.femtocraft.proxy.ProxyServer")
  var proxy: ProxyCommon = null

  val tab = new CreativeTabs(Femtocraft.ID) {
    override def getTabIconItem: Item = Items.nether_star
  }

  @EventHandler def preInit(event: FMLPreInitializationEvent): Unit = {
    PacketHandler.init()
    proxy.init()
    blocks.preInit()
    items.preInit()
    NaniteRegistry.init()
  }

  @EventHandler def init(event: FMLInitializationEvent): Unit = {
    blocks.init()
    items.init()
  }

  @EventHandler def postInit(event: FMLPostInitializationEvent): Unit = {
    blocks.postInit()
    items.postInit()
  }
}
