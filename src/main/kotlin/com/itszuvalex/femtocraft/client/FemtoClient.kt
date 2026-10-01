package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.power.PowerContent
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent

/**
 * Client-only registrations. Only referenced when running on a client.
 */
object FemtoClient {
    fun register(modBus: IEventBus) {
        modBus.addListener(::registerScreens)
    }

    private fun registerScreens(event: RegisterMenuScreensEvent) {
        event.register(PowerContent.CRYSTAL_MOUNT_MENU.get(), ::CrystalMountScreen)
        event.register(PowerContent.CRYSTAL_MACHINE_MENU.get(), ::CrystalMachineScreen)
    }
}
