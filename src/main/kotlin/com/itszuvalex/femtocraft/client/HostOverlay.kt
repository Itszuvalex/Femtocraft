package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.archive.NaniteHost
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Player
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent
import net.neoforged.neoforge.client.gui.GuiLayer

/**
 * The nanite host's status, at the top left of the screen while the player is a host: their Archive nanites against
 * what their body keeps stocked ([NaniteHost.REGEN_CAP]), with a bar, and whether they are regrowing. Other systems
 * add lines with [addLine] (bonuses, strains, and so on as they come).
 */
object HostOverlay : GuiLayer {
    private const val X = 4
    private const val Y = 4
    private const val BAR_WIDTH = 60
    private const val LINE = 10

    private val lines = ArrayList<(Player) -> Component?>()

    /**
     * Adds a line under the nanite count; return null to show nothing this frame.
     */
    fun addLine(line: (Player) -> Component?) {
        lines += line
    }

    fun register(event: RegisterGuiLayersEvent) = event.registerAboveAll(Identifier.fromNamespaceAndPath(Femtocraft.ID, "host_overlay"), this)

    override fun render(graphics: GuiGraphicsExtractor, deltaTracker: DeltaTracker) {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        if (!NaniteHost.isHost(player) || mc.options.hideGui) return
        val font = mc.font
        val amount = NaniteHost.archiveNanites(player)
        val regrowing = amount < NaniteHost.REGEN_CAP && player.foodData.foodLevel >= NaniteHost.REGEN_MIN_FOOD
        val title = Component.translatable("hud.femtocraft.host.nanites", amount, NaniteHost.REGEN_CAP)
        graphics.text(font, title, X, Y, COLOR, true)
        val barY = Y + LINE
        graphics.fill(X, barY, X + BAR_WIDTH, barY + 3, 0xC0000000.toInt())
        graphics.fill(X, barY, X + BAR_WIDTH * amount.coerceAtMost(NaniteHost.REGEN_CAP) / NaniteHost.REGEN_CAP, barY + 3, COLOR)
        var y = barY + 6
        val status = when {
            amount >= NaniteHost.REGEN_CAP -> null
            regrowing -> Component.translatable("hud.femtocraft.host.regrowing")
            else -> Component.translatable("hud.femtocraft.host.hungry")
        }
        for (line in listOfNotNull(status) + lines.mapNotNull { it(player) }) {
            graphics.text(font, line, X, y, 0xFFC8C8C8.toInt(), true)
            y += LINE
        }
    }

    private const val COLOR = 0xFF000000.toInt() or NaniteHost.ARCHIVE_COLOR
}
