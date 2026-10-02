package com.itszuvalex.femtocraft.dev

import net.neoforged.bus.api.IEventBus

/**
 * Development-only content and game tests. Registered only when `!FMLEnvironment.isProduction()`.
 */
object DevContent {
    fun register(modBus: IEventBus) {
        DevGameTests.register(modBus)
        DevShowcase.register()
        if (net.neoforged.fml.loading.FMLEnvironment.getDist() == net.neoforged.api.distmarker.Dist.CLIENT) DevShowcaseClient.register()
    }
}
