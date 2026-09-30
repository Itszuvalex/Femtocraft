package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.dev.DevContent
import com.itszuvalex.femtocraft.logistics.distributed.DistributedManager
import com.itszuvalex.femtocraft.nanite.NaniteManager
import com.itszuvalex.femtocraft.power.PowerManager
import com.mojang.logging.LogUtils
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.server.ServerStoppedEvent
import org.slf4j.Logger
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

/**
 * Femtocraft mod entry point. Loaded by Kotlin for Forge (`modLoader="kotlinforforge"`), which instantiates this object.
 */
@Mod(Femtocraft.ID)
object Femtocraft {
    const val ID = "femtocraft"
    const val NAME = "Femtocraft"

    @JvmField
    val LOGGER: Logger = LogUtils.getLogger()

    init {
        // Modules must exist before RegisterCapabilitiesEvent
        FemtoModules.init()
        FemtoComponents.register(MOD_BUS)
        FemtoBlocks.register(MOD_BUS)
        FemtoItems.register(MOD_BUS)
        FemtoBlockEntities.register(MOD_BUS)
        FemtoMenus.register(MOD_BUS)
        FemtoTabs.register(MOD_BUS)
        FemtoRecipes.register(MOD_BUS)
        com.itszuvalex.femtocraft.worldgen.FemtoWorldgen.register(MOD_BUS)

        if (!FMLEnvironment.isProduction()) {
            DevContent.register(MOD_BUS)
        }

        // Managers hold server-side locations; drop them with the server.
        NeoForge.EVENT_BUS.addListener { _: ServerStoppedEvent ->
            PowerManager.clear()
            DistributedManager.clear()
            NaniteManager.clear()
        }
    }
}
