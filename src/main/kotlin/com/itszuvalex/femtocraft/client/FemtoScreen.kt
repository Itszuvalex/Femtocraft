package com.itszuvalex.femtocraft.client

import com.itszuvalex.itszulib.client.screen.ComponentScreen
import com.itszuvalex.itszulib.client.screen.EnergyGauge
import com.itszuvalex.itszulib.menu.EnergyView
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import com.itszuvalex.itszulib.client.screen.ScreenStyle
import com.itszuvalex.femtocraft.Femtocraft
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import java.util.Locale

/**
 * Functional screen on ItszuLib's [ComponentScreen] (DECISIONS B2): the theme's panel and slots (Femtocraft's theme,
 * `femtocraft:femtocraft`, by default), title and inventory label, the side configuration panel behind the "IO" tab
 * when the machine has a sided configuration, and helpers for power gauges, progress bars and text. v3's textured GUIs
 * and widget toolkit are follow-up work.
 */
abstract class FemtoScreen<M : AbstractContainerMenu>(menu: M, inventory: Inventory, title: Component, width: Int = 176, height: Int = 166) :
    ComponentScreen<M>(menu, inventory, title, width, height) {

    override fun defaultTheme(): Identifier = THEME

    override fun extractPanel(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        super.extractPanel(graphics, mouseX, mouseY)
        extractContents(graphics, mouseX, mouseY)
    }

    /**
     * Draws the screen's own widgets, in screen coordinates (add [leftPos]/[topPos]).
     */
    protected open fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {}

    /**
     * Adds a vertical power gauge (v3's `GuiPowerMeter`) at ([x], [y]) in the image, over a synced battery.
     */
    protected fun addPowerGauge(x: Int, y: Int, view: () -> EnergyView) = addComponent(powerGauge(view), x, y)

    /** A power gauge for a layout ([com.itszuvalex.itszulib.client.screen.Row] and the like). */
    protected fun powerGauge(view: () -> EnergyView) = EnergyGauge(view, Component.translatable("gui.femtocraft.power_unit"), POWER, METER_W, METER_H)

    /**
     * A horizontal progress bar filled to [fraction].
     */
    protected fun progress(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int, height: Int, fraction: Double, color: Int = PROGRESS) {
        ScreenStyle.frame(graphics, leftPos + x, topPos + y, width, height)
        graphics.fill(leftPos + x, topPos + y, leftPos + x + width, topPos + y + height, ScreenStyle.DARK)
        graphics.fill(leftPos + x, topPos + y, leftPos + x + (fraction.coerceIn(0.0, 1.0) * width).toInt(), topPos + y + height, color)
    }

    /**
     * An inset area in the image (a tank's well), like a slot's.
     */
    protected fun inset(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int, height: Int) =
        ScreenStyle.inset(graphics, leftPos + x, topPos + y, width, height)

    protected fun text(graphics: GuiGraphicsExtractor, text: Component, x: Int, y: Int) =
        graphics.text(font, text, leftPos + x, topPos + y, TEXT, false)

    companion object {
        /** Femtocraft's screen theme (`assets/femtocraft/itszulib/themes/femtocraft.json`, on ItszuLib's dark one). */
        @JvmField
        val THEME: Identifier = Identifier.fromNamespaceAndPath(Femtocraft.ID, "femtocraft")

        /** The current theme's text colour. */
        val TEXT: Int get() = ScreenStyle.TEXT
        val PROGRESS: Int get() = ScreenStyle.PROGRESS
        const val POWER = 0xFF33CCFF.toInt()
        const val METER_W = 8
        const val METER_H = 52

        fun fmt(value: Double): String = "%,.0f".format(Locale.ROOT, value)

        fun fmt1(value: Double): String = "%,.1f".format(Locale.ROOT, value)
    }
}
