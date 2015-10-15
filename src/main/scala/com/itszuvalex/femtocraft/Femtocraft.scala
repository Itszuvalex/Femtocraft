package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.core.Cyber.{CyberMachineRegistry, CybermaterialRegistry}
import com.itszuvalex.femtocraft.core.Industry.FrameMultiblockRegistry
import com.itszuvalex.femtocraft.network.FemtoPacketHandler
import com.itszuvalex.femtocraft.proxy.{ProxyCommon, ProxyGuiCommon}
import com.itszuvalex.femtocraft.worldgen.FemtocraftOreGenerator
import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.common.{Mod, SidedProxy}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.Items
import net.minecraft.item.Item
import org.apache.logging.log4j.LogManager

/**
 * Created by Christopher on 4/5/2015.
 */
@Mod(modid = Femtocraft.ID, name = Femtocraft.ID, version = Femtocraft.VERSION, modLanguage = "scala", dependencies = "required-after:ItszuLib")
object Femtocraft {
  final val ID      = "Femtocraft"
  final val VERSION = Version.FULL_VERSION
  final val logger  = LogManager.getLogger(ID)
  final val blocks  = FemtoBlocks
  final val items   = FemtoItems
  final val fluids  = FemtoFluids

  @SidedProxy(clientSide = "com.itszuvalex.femtocraft.proxy.ProxyClient",
              serverSide = "com.itszuvalex.femtocraft.proxy.ProxyServer")
  var proxy: ProxyCommon = null

  @SidedProxy(clientSide = "com.itszuvalex.femtocraft.proxy.ProxyGuiClient",
              serverSide = "com.itszuvalex.femtocraft.proxy.ProxyGuiCommon")
  var guiProxy: ProxyGuiCommon = null

  val tab = new CreativeTabs(Femtocraft.ID) {
    override def getTabIconItem: Item = Items.nether_star
  }

  @EventHandler def preInit(event: FMLPreInitializationEvent): Unit = {
    FemtoBlocks.preInit()
    FemtoItems.preInit()
    FemtoFluids.preInit()

    FemtoPacketHandler.preInit()

    GameRegistry.registerWorldGenerator(new FemtocraftOreGenerator, FemtocraftOreGenerator.GENERATION_WEIGHT)
    NetworkRegistry.INSTANCE.registerGuiHandler(this, guiProxy)
  }

  @EventHandler def init(event: FMLInitializationEvent): Unit = {
    FemtoBlocks.init()
    FemtoItems.init()
    FemtoFluids.init()
    FrameMultiblockRegistry.init()
    CyberMachineRegistry.init()
  }

  @EventHandler def postInit(event: FMLPostInitializationEvent): Unit = {
    FemtoBlocks.postInit()
    FemtoItems.postInit()
    FemtoFluids.postInit()
    CybermaterialRegistry.postInit()
    proxy.postInit()
  }
}
