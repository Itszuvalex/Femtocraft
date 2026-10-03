package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.client.FemtoClient
import com.itszuvalex.femtocraft.cyber.CyberContent
import com.itszuvalex.femtocraft.dev.DevContent
import com.itszuvalex.femtocraft.industry.IndustryContent
import com.itszuvalex.femtocraft.logistics.LogisticsContent
import com.itszuvalex.femtocraft.logistics.ProviderManager
import com.itszuvalex.femtocraft.logistics.distributed.DistributedManager
import com.itszuvalex.femtocraft.nanite.NaniteContent
import com.itszuvalex.femtocraft.power.PowerContent
import com.itszuvalex.femtocraft.power.WirelessPowerManager
import com.itszuvalex.femtocraft.worldgen.WorldgenContent
import com.mojang.logging.LogUtils
import net.neoforged.api.distmarker.Dist
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
        // Content objects register into FemtoRegistries (and register their modules) when initialized.
        FemtoSounds.init()
        com.itszuvalex.femtocraft.core.FemtoParticles.init()
        PowerContent.init()
        IndustryContent.init(MOD_BUS)
        // Multiblock shapes must be registered before any world loads.
        com.itszuvalex.femtocraft.industry.FrameMultiblocks.init()
        com.itszuvalex.femtocraft.archive.ArchiveContent.init()
        NaniteContent.init()
        LogisticsContent.init()
        com.itszuvalex.femtocraft.computation.ComputationContent.init()
        CyberContent.init()
        WorldgenContent.init()
        FemtoRegistries.register(MOD_BUS)

        NeoForge.EVENT_BUS.addListener { _: ServerStoppedEvent ->
            WirelessPowerManager.clear()
            DistributedManager.clear()
            ProviderManager.clear()
        }

        if (FMLEnvironment.getDist() == Dist.CLIENT) FemtoClient.register(MOD_BUS)
        if (!FMLEnvironment.isProduction()) DevContent.register(MOD_BUS)
    }
}
