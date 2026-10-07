package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.nanite.PlayerNanites
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.resources.Identifier
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent
import net.neoforged.neoforge.client.gui.GuiLayer

/**
 * The player's nanite gauge at the right edge of the screen, halfway down. Port of v3's
 * `PlayerNaniteCapabilitiesOverlay`: it slides in when the amount of nanites changes, stays for two seconds after the
 * last change, then slides out.
 */
object NaniteOverlay : GuiLayer {
    private val BASE = Identifier.fromNamespaceAndPath(Femtocraft.ID, "textures/gui/naniteoverlay_base.png")
    private val FILL = Identifier.fromNamespaceAndPath(Femtocraft.ID, "textures/gui/naniteoverlay_fill.png")
    private const val WIDTH = 16
    private const val HEIGHT = 32

    /**
     * Pixels of the fill texture's frame at its top and bottom.
     */
    private const val FILL_INSET = 2
    private const val REVEAL_MS = 500L
    private const val SHOW_MS = 2000L
    private const val HIDE_MS = 750L

    /**
     * A change within this long of the previous one continues the same reveal.
     */
    private const val CHAIN_MS = 1000L

    private var lastAmount = -1
    private var chainStart = 0L
    private var lastChange = Long.MIN_VALUE / 2
    private var offset = WIDTH

    fun register(event: RegisterGuiLayersEvent) = event.registerAboveAll(Identifier.fromNamespaceAndPath(Femtocraft.ID, "nanite_overlay"), this)

    override fun render(graphics: GuiGraphicsExtractor, deltaTracker: DeltaTracker) {
        val player = Minecraft.getInstance().player ?: return
        val amount = PlayerNanites.tank(player).contents().sumOf { it.amount }
        val now = System.currentTimeMillis()
        if (amount != lastAmount) {
            // The first reading (joining a world) is not a change.
            if (lastAmount >= 0) interact(now)
            lastAmount = amount
        }
        updateOffset(now)
        if (offset >= WIDTH) return
        val x = graphics.guiWidth() - WIDTH + offset
        val y = (graphics.guiHeight() - HEIGHT) / 2
        graphics.blit(BASE, x, y, x + WIDTH, y + HEIGHT, 0f, 1f, 0f, 1f)
        val fill = ((HEIGHT - 2 * FILL_INSET) * amount.toFloat() / com.itszuvalex.femtocraft.host.HostStats.of(player).tankCapacity).toInt() + FILL_INSET
        val v = 1f - fill.toFloat() / HEIGHT
        graphics.blit(FILL, x, y + HEIGHT - fill, x + WIDTH, y + HEIGHT, 0f, 1f, v, 1f)
    }

    private fun interact(now: Long) {
        if (now - lastChange >= CHAIN_MS) chainStart = now
        lastChange = now
    }

    private fun updateOffset(now: Long) {
        val sinceStart = now - chainStart
        if (sinceStart < REVEAL_MS) {
            offset = minOf(offset, WIDTH - (WIDTH * sinceStart / REVEAL_MS).toInt())
            return
        }
        val sinceChange = now - lastChange
        offset = when {
            sinceChange <= REVEAL_MS + SHOW_MS -> 0
            sinceChange <= REVEAL_MS + SHOW_MS + HIDE_MS -> Math.ceil(WIDTH * (sinceChange - REVEAL_MS - SHOW_MS).toDouble() / HIDE_MS).toInt()
            else -> WIDTH
        }
    }
}
