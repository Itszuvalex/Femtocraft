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
    private var shotFor = -1
    private var asked = false
    private var scrolled = false
    private var cancelled = false

    /**
     * From view 22, a screenshot (`run/screenshots/showcase_view_<n>.png`) a few seconds into each view, once the menu is
     * open and the chunks have loaded, for looking at the new rendering; the delete confirmation (view 28) is asked for
     * first.
     */
    private fun shoot(mc: Minecraft) {
        val view = DevShowcase.currentView
        if (view < 22) return
        if (view == 28 && DevShowcase.viewTicks >= 60 && !asked) {
            asked = true
            val screen = mc.screen as? com.itszuvalex.itszulib.client.screen.ComponentScreen<*>
            (screen?.openPanel?.component as? com.itszuvalex.itszulib.client.screen.ChannelPanel)?.askDeleteFirst()
        }
        if (view == 29 && DevShowcase.viewTicks >= 5 && !cancelled) {
            cancelled = true
            val screen = mc.screen as? com.itszuvalex.itszulib.client.screen.ComponentScreen<*>
            (screen?.openPanel?.component as? com.itszuvalex.itszulib.client.screen.ChannelPanel)?.cancelAsk()
        }
        if (view == 30 && DevShowcase.viewTicks >= 60 && !scrolled) {
            scrolled = true
            val screen = mc.screen as? com.itszuvalex.itszulib.client.screen.ComponentScreen<*>
            (screen?.openPanel?.component as? com.itszuvalex.itszulib.client.screen.ChannelPanel)?.scrollTo(7)
        }
        if (DevShowcase.viewTicks >= 200 && shotFor != view) {
            shotFor = view
            net.minecraft.client.Screenshot.grab(mc.gameDirectory, "showcase_view_$view.png", mc.mainRenderTarget, 1) { }
        }
    }

    fun register() {
        if (!DevShowcase.enabled) return
        NeoForge.EVENT_BUS.addListener { _: ClientTickEvent.Post ->
            val mc = Minecraft.getInstance()
            val nanites = mc.player?.let { com.itszuvalex.femtocraft.nanite.PlayerNanites.tank(it).contents().isNotEmpty() } ?: false
            mc.options.hideGui = !nanites && DevShowcase.currentView != 23
            shoot(mc)
            val screen = mc.screen as? com.itszuvalex.itszulib.client.screen.ComponentScreen<*>
            if (screen != null && screen !== panelOpenedFor && screen.openPanel == null) {
                panelOpenedFor = screen
                screen.panels().firstOrNull()?.let(screen::togglePanel)
            }
        }
    }
}
