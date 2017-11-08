package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.api.ManagerCapabilities
import com.itszuvalex.femtocraft.api.logistics.LogisticsResourceRegistry
import com.itszuvalex.femtocraft.api.nanite.NaniteRegistry
import com.itszuvalex.femtocraft.cyber.CybermaterialRegistry
import com.itszuvalex.femtocraft.industry.FrameMultiblockRegistry
import com.itszuvalex.femtocraft.network.FemtoPacketHandler
import com.itszuvalex.femtocraft.player.PlayerNaniteCapabilities
import com.itszuvalex.femtocraft.power.PowerManager
import com.itszuvalex.femtocraft.proxy.{ProxyCommon, ProxyGuiCommon}
import com.itszuvalex.femtocraft.worldgen.FemtocraftOreGenerator
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.{Item, ItemStack}
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.fml.common.{Mod, SidedProxy}
import org.apache.logging.log4j.LogManager

/**
  * Created by Christopher on 4/5/2015.
  */
@Mod(modid = Femtocraft.ID, name = "Femtocraft", version = Femtocraft.VERSION, modLanguage = "scala", dependencies = "required-after:itszulib")
object Femtocraft {
  final val ID      = "femtocraft"
  final val VERSION = Version.FULL_VERSION
  final val logger  = LogManager.getLogger(ID)
  final val blocks  = FemtoBlocks
  final val items   = FemtoItems
  final val fluids  = FemtoFluids
  val tab                      = new CreativeTabs(Femtocraft.ID) {
    override def getTabIconItem: ItemStack = new ItemStack(Item.getItemFromBlock(FemtoBlocks.blockNaniteHiveSmall))
  }
  @SidedProxy(clientSide = "com.itszuvalex.femtocraft.proxy.ProxyClient",
    serverSide = "com.itszuvalex.femtocraft.proxy.ProxyServer")
  var proxy   : ProxyCommon    = _
  @SidedProxy(clientSide = "com.itszuvalex.femtocraft.proxy.ProxyGuiClient",
    serverSide = "com.itszuvalex.femtocraft.proxy.ProxyGuiCommon")
  var guiProxy: ProxyGuiCommon = _

  @EventHandler def preInit(event: FMLPreInitializationEvent): Unit = {
    MinecraftForge.EVENT_BUS.register(FemtoSounds)
    MinecraftForge.EVENT_BUS.register(FemtoBlocks)
    MinecraftForge.EVENT_BUS.register(FemtoItems)

    FemtoFluids.preInit()
    FemtoRecipes.preInit()
    NaniteRegistry.preInit()

    FemtoPacketHandler.preInit()
    GameRegistry.registerWorldGenerator(new FemtocraftOreGenerator, FemtocraftOreGenerator.GENERATION_WEIGHT)
    NetworkRegistry.INSTANCE.registerGuiHandler(this, guiProxy)
    PlayerNaniteCapabilities.register()
    ManagerCapabilities.register()
    proxy.preInit()
  }

  @EventHandler def init(event: FMLInitializationEvent): Unit = {
    FemtoBlocks.init()
    FemtoItems.init()
    FemtoFluids.init()
    FemtoRecipes.init()
    FrameMultiblockRegistry.init()
    PowerManager.init()
    LogisticsResourceRegistry.init()
    proxy.init()
  }

  @EventHandler def postInit(event: FMLPostInitializationEvent): Unit = {
    FemtoBlocks.postInit()
    FemtoItems.postInit()
    FemtoFluids.postInit()
    FemtoRecipes.postInit()
    CybermaterialRegistry.postInit()
    proxy.postInit()
  }
}
