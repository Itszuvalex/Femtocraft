package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.core.FemtoMenu
import com.itszuvalex.itszulib.client.ScreenHelpers
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Button
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import java.util.Locale

/**
 * Plain functional screen: grey panel, slot outlines, title and inventory label, with helpers for power meters,
 * progress bars and text. v3's textured GUIs and widget toolkit are follow-up work (DECISIONS B2).
 */
abstract class FemtoScreen<M : AbstractContainerMenu>(menu: M, inventory: Inventory, title: Component, width: Int = 176, height: Int = 166) :
    AbstractContainerScreen<M>(menu, inventory, title, width, height) {
    /**
     * The 3D side configuration panel right of the screen, for machines with a sided configuration.
     */
    private var sidePanel: SideConfigPanel? = null

    override fun init() {
        super.init()
        val femtoMenu = menu as? FemtoMenu<*> ?: return
        val panel = sidePanel ?: FemtoMenu.configurationModes(femtoMenu.blockEntity).takeIf { it.isNotEmpty() }?.let { SideConfigPanel(femtoMenu, it) } ?: return
        sidePanel = panel
        panel.x = leftPos + imageWidth + 6
        panel.y = topPos
        addRenderableWidget(Button.builder(modeLabel(panel)) { button -> panel.cycleMode(); button.message = modeLabel(panel) }
            .bounds(panel.x, panel.y + SideConfigPanel.SIZE + 4, SideConfigPanel.SIZE, 14).build())
    }

    private fun modeLabel(panel: SideConfigPanel): Component =
        Component.translatable("tooltip.femtocraft.configurator.${panel.mode.name.lowercase()}")

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean =
        sidePanel?.mouseClicked(event.x(), event.y()) == true || super.mouseClicked(event, doubleClick)

    override fun mouseDragged(event: MouseButtonEvent, dx: Double, dy: Double): Boolean =
        sidePanel?.mouseDragged(event.x(), event.y(), dx, dy) == true || super.mouseDragged(event, dx, dy)

    override fun mouseReleased(event: MouseButtonEvent): Boolean =
        sidePanel?.mouseReleased(event.x(), event.y(), minecraft.hasShiftDown()) == true || super.mouseReleased(event)

    override fun extractBackground(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, a: Float) {
        super.extractBackground(graphics, mouseX, mouseY, a)
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, PANEL)
        for (slot in menu.slots) graphics.fill(leftPos + slot.x - 1, topPos + slot.y - 1, leftPos + slot.x + 17, topPos + slot.y + 17, SLOT)
        extractContents(graphics, mouseX, mouseY)
        sidePanel?.extract(graphics, mouseX, mouseY, font)
    }

    /**
     * Draws the screen's own widgets, in screen coordinates (add [leftPos]/[topPos]).
     */
    protected open fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {}

    /**
     * A vertical power meter (v3's `GuiPowerMeter`) with a tooltip.
     */
    protected fun powerMeter(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, x: Int, y: Int, battery: FemtoMenu.BatteryView, color: Int = POWER) {
        val left = leftPos + x
        val top = topPos + y
        graphics.fill(left - 1, top - 1, left + METER_W + 1, top + METER_H + 1, SLOT)
        val filled = (battery.fraction * METER_H).toInt()
        graphics.fill(left, top + METER_H - filled, left + METER_W, top + METER_H, color)
        ScreenHelpers.tooltipIfHovered(graphics, mouseX, mouseY, left, top, METER_W, METER_H, listOf(Component.translatable("tooltip.femtocraft.power", fmt(battery.storage), fmt(battery.max))))
    }

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
