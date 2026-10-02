package com.itszuvalex.femtocraft.client

import com.itszuvalex.itszulib.client.screen.ComponentScreen
import com.itszuvalex.itszulib.client.screen.EnergyGauge
import com.itszuvalex.itszulib.menu.EnergyView
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import java.util.Locale

/**
 * Plain functional screen on ItszuLib's [ComponentScreen] (DECISIONS B2): grey panel, slot outlines, title and inventory
 * label, the side configuration panel behind the "IO" tab when the machine has a sided configuration, and helpers for
 * power gauges, progress bars and text. v3's textured GUIs and widget toolkit are follow-up work.
 */
abstract class FemtoScreen<M : AbstractContainerMenu>(menu: M, inventory: Inventory, title: Component, width: Int = 176, height: Int = 166) :
    ComponentScreen<M>(menu, inventory, title, width, height) {

    override fun extractPanel(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, PANEL)
        for (slot in menu.slots) graphics.fill(leftPos + slot.x - 1, topPos + slot.y - 1, leftPos + slot.x + 17, topPos + slot.y + 17, SLOT)
        extractContents(graphics, mouseX, mouseY)
    }

    /**
     * Draws the screen's own widgets, in screen coordinates (add [leftPos]/[topPos]).
     */
    protected open fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {}

    /**
     * Adds a vertical power gauge (v3's `GuiPowerMeter`) at ([x], [y]) in the image, over a synced battery.
     */
    protected fun addPowerGauge(x: Int, y: Int, view: () -> EnergyView) =
        addComponent(EnergyGauge(view, Component.translatable("gui.femtocraft.power_unit"), POWER, METER_W, METER_H), x, y)

    /**
     * A horizontal progress bar filled to [fraction].
     */
    protected fun progress(graphics: GuiGraphicsExtractor, x: Int, y: Int, width: Int, height: Int, fraction: Double, color: Int = PROGRESS) {
        graphics.fill(leftPos + x - 1, topPos + y - 1, leftPos + x + width + 1, topPos + y + height + 1, SLOT)
        graphics.fill(leftPos + x, topPos + y, leftPos + x + (fraction.coerceIn(0.0, 1.0) * width).toInt(), topPos + y + height, color)
    }

    protected fun text(graphics: GuiGraphicsExtractor, text: Component, x: Int, y: Int) =
        graphics.text(font, text, leftPos + x, topPos + y, TEXT, false)

    companion object {
        const val PANEL = 0xFFC6C6C6.toInt()
        const val SLOT = 0xFF8B8B8B.toInt()
        const val TEXT = 0xFF404040.toInt()
        const val POWER = 0xFF33CCFF.toInt()
        const val PROGRESS = 0xFF55DD55.toInt()
        const val METER_W = 8
        const val METER_H = 52

        fun fmt(value: Double): String = "%,.0f".format(Locale.ROOT, value)

        fun fmt1(value: Double): String = "%,.1f".format(Locale.ROOT, value)
    }
}
