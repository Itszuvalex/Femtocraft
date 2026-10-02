package com.itszuvalex.femtocraft.dev

import net.minecraft.client.Minecraft
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.common.NeoForge

/**
 * Client half of [DevShowcase]: hides the HUD until the player has nanites (the last view, which shows the nanite
 * gauge). Only referenced on a client.
 */
object DevShowcaseClient {
    fun register() {
        if (!DevShowcase.enabled) return
        NeoForge.EVENT_BUS.addListener { _: ClientTickEvent.Post ->
            val mc = Minecraft.getInstance()
            val nanites = mc.player?.let { com.itszuvalex.femtocraft.nanite.PlayerNanites.tank(it).contents().isNotEmpty() } ?: false
            mc.options.hideGui = !nanites
        }
    }
}
