package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.dev.DevContent
import com.mojang.logging.LogUtils
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.FMLEnvironment
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
        if (!FMLEnvironment.isProduction()) {
            DevContent.register(MOD_BUS)
        }
    }
}
