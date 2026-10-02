package com.itszuvalex.femtocraft.dev

import net.minecraft.client.Minecraft
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.common.NeoForge

/**
 * Client half of [DevShowcase]: hides the HUD. Only referenced on a client.
 */
object DevShowcaseClient {
    fun register() {
        if (!DevShowcase.enabled) return
        NeoForge.EVENT_BUS.addListener { _: ClientTickEvent.Post -> Minecraft.getInstance().options.hideGui = true }
    }
}
