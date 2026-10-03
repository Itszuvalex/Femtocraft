package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.computation.MainframeHeat
import com.itszuvalex.femtocraft.computation.MainframeMenu
import com.itszuvalex.itszulib.client.ScreenHelpers
import com.itszuvalex.itszulib.client.screen.ScreenStyle
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

/**
 * The mainframe: processor slots, power gauge, FLOPS computed last tick against what the processors manage now, and
 * a temperature gauge (red from where it starts to slow down) with the ambient temperature in its tooltip.
 */
class MainframeScreen(menu: MainframeMenu, inventory: Inventory, title: Component) : FemtoScreen<MainframeMenu>(menu, inventory, title) {
    override fun addComponents() {
        addPowerGauge(8, 18) { menu.battery }
    }

    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        text(graphics, Component.translatable("gui.femtocraft.mainframe.flops", fmt(menu.flops)), 86, 22)
        text(graphics, Component.translatable("gui.femtocraft.mainframe.capacity", fmt(menu.capacity)), 86, 34)
        text(graphics, Component.translatable("gui.femtocraft.mainframe.temperature", fmt1(menu.temperature)), 86, 46)
        val fraction = (menu.temperature / MainframeHeat.MAX).coerceIn(0.0, 1.0)
        val color = if (menu.temperature >= MainframeHeat.THROTTLE_START) HOT else COOL
        ScreenStyle.frame(graphics, leftPos + GAUGE_X, topPos + 18, METER_W, METER_H)
        ScreenHelpers.progressBar(graphics, leftPos + GAUGE_X, topPos + 18, METER_W, METER_H, fraction, color, ScreenStyle.DARK, vertical = true)
        ScreenHelpers.tooltipIfHovered(graphics, mouseX, mouseY, leftPos + GAUGE_X, topPos + 18, METER_W, METER_H, listOf(
            Component.translatable("gui.femtocraft.mainframe.temperature", fmt1(menu.temperature)),
            Component.translatable("gui.femtocraft.mainframe.ambient", fmt1(menu.ambient)),
            Component.translatable("gui.femtocraft.mainframe.throttle", fmt1(MainframeHeat.THROTTLE_START), fmt1(MainframeHeat.MAX)),
        ))
    }

    companion object {
        const val GAUGE_X = 160
        const val COOL = 0xFF4FA3E0.toInt()
        const val HOT = 0xFFE0573A.toInt()
    }
}
