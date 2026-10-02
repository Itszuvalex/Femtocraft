package com.itszuvalex.femtocraft.dev

import net.minecraft.client.Minecraft
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.common.NeoForge

/**
 * Client half of [DevShowcase]: hides the HUD until the player has nanites (the last view, which shows the nanite
 * gauge), and opens the first side panel (the side configuration panel) of a screen the showcase opens. Only
 * referenced on a client.
 */
object DevShowcaseClient {
    private var panelOpenedFor: Any? = null

    fun register() {
        if (!DevShowcase.enabled) return
        NeoForge.EVENT_BUS.addListener { _: ClientTickEvent.Post ->
            val mc = Minecraft.getInstance()
            val nanites = mc.player?.let { com.itszuvalex.femtocraft.nanite.PlayerNanites.tank(it).contents().isNotEmpty() } ?: false
            mc.options.hideGui = !nanites
            val screen = mc.screen as? com.itszuvalex.itszulib.client.screen.ComponentScreen<*>
            if (screen != null && screen !== panelOpenedFor && screen.openPanel == null) {
                panelOpenedFor = screen
                screen.panels().firstOrNull()?.let(screen::togglePanel)
            }
        }
    }
}
