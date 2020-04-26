package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.api.ManagerCapabilities
import com.itszuvalex.femtocraft.api.logistics.LogisticsResourceRegistry
import com.itszuvalex.femtocraft.api.nanite.NaniteRegistry
import com.itszuvalex.femtocraft.cyber.CybermaterialRegistry
import com.itszuvalex.femtocraft.industry.FrameMultiblockRegistry
import com.itszuvalex.femtocraft.network.FemtoPacketHandler
import com.itszuvalex.femtocraft.player.PlayerNaniteCapability
import com.itszuvalex.femtocraft.power.WirelessPowerManager
import com.itszuvalex.femtocraft.proxy.{ProxyCommon, ProxyGuiCommon}
import com.itszuvalex.femtocraft.worldgen.FemtocraftOreGenerator
import com.itszuvalex.itszulib.initialization.{InitializationStage, ModInit}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.{Item, ItemStack}
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.network.NetworkRegistry
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.fml.common.{Mod, SidedProxy}
import org.apache.logging.log4j.LogManager

/**
 * Created by Christopher on 4/5/2015.
 */
@Mod(modid = Femtocraft.ID, name = Femtocraft.NAME, version = Femtocraft.VERSION, modLanguage = Femtocraft.MOD_LANGUAGE, dependencies = Femtocraft.DEPENDENCIES)
object Femtocraft extends ModInit {
  final val ID           = "femtocraft"
  final val NAME         = "Femtocraft"
  final val VERSION      = Version.FULL_VERSION
  final val MOD_LANGUAGE = "scala"
  final val DEPENDENCIES = "required-after:itszulib"
  final val logger       = LogManager.getLogger(ID)
  final val blocks       = FemtoBlocks
  final val items        = FemtoItems
  final val fluids       = FemtoFluids


  val tab                      = new CreativeTabs(Femtocraft.ID) {
    override def getTabIconItem: ItemStack = new ItemStack(Item.getItemFromBlock(FemtoBlocks.blockItemRepository))
  }
  @SidedProxy(clientSide = "com.itszuvalex.femtocraft.proxy.ProxyClient",
              serverSide = "com.itszuvalex.femtocraft.proxy.ProxyServer")
  var proxy   : ProxyCommon    = _
  @SidedProxy(clientSide = "com.itszuvalex.femtocraft.proxy.ProxyGuiClient",
              serverSide = "com.itszuvalex.femtocraft.proxy.ProxyGuiCommon")
  var guiProxy: ProxyGuiCommon = _

  initializationManager.addInitStage(InitializationStage.Pre, () => {
    MinecraftForge.EVENT_BUS.register(FemtoSounds)
    MinecraftForge.EVENT_BUS.register(FemtoBlocks)
    MinecraftForge.EVENT_BUS.register(FemtoItems)
    MinecraftForge.EVENT_BUS.register(proxy)

    FemtoFluids.preInit()
    FemtoRecipes.preInit()
    NaniteRegistry.preInit()

    FemtoPacketHandler.preInit()
    GameRegistry.registerWorldGenerator(new FemtocraftOreGenerator, FemtocraftOreGenerator.GENERATION_WEIGHT)
    NetworkRegistry.INSTANCE.registerGuiHandler(this, guiProxy)
    PlayerNaniteCapability.register()
    ManagerCapabilities.register()
    proxy.preInit()
  })

  initializationManager.addInitStage(InitializationStage.Main, () => {
    FemtoBlocks.init()
    FemtoItems.init()
    FemtoFluids.init()
    FemtoRecipes.init()
    FrameMultiblockRegistry.init()
    WirelessPowerManager.instance.init()
    LogisticsResourceRegistry.init()
    proxy.init()
  })

  initializationManager.addInitStage(InitializationStage.Post, () => {
    FemtoBlocks.postInit()
    FemtoItems.postInit()
    FemtoFluids.postInit()
    FemtoRecipes.postInit()
    CybermaterialRegistry.postInit()
    proxy.postInit()
  })
}
