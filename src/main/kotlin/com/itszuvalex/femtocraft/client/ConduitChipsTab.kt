package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.logistics.Chips
import com.itszuvalex.femtocraft.logistics.ConduitMenu
import com.itszuvalex.femtocraft.logistics.LogisticsConduit
import com.itszuvalex.femtocraft.logistics.LogisticsContent
import com.itszuvalex.itszulib.api.filter.ResourceFilter
import com.itszuvalex.itszulib.client.screen.ButtonAccents
import com.itszuvalex.itszulib.client.screen.ButtonComponent
import com.itszuvalex.itszulib.client.screen.Column
import com.itszuvalex.itszulib.client.screen.ComponentHost
import com.itszuvalex.itszulib.client.screen.FilterRow
import com.itszuvalex.itszulib.client.screen.Label
import com.itszuvalex.itszulib.client.screen.Row
import com.itszuvalex.itszulib.client.screen.ScreenComponent
import com.itszuvalex.itszulib.client.screen.ScreenStyle
import com.itszuvalex.itszulib.client.screen.SidePanel
import com.itszuvalex.itszulib.client.screen.TitledPanel
import com.itszuvalex.itszulib.menu.MenuActionPayload
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.client.network.ClientPacketDistributor

/**
 * The conduit's chips tab: every chip in the conduit, by face, where clicking a chip only selects it (taking chips in
 * and out is the main page's job); then the selected chip's direction and interface buttons (shift cycles backwards),
 * its current settings and its filter ([FilterRow], as in AE2: allow or deny, exact or any data, and nine cells set by
 * clicking with the item (item chips) or a filled container (fluid chips) held).
 */
object ConduitChipsTab {
    fun panel(menu: ConduitMenu): SidePanel {
        val selection = Selection(menu)
        val title = Component.translatable("gui.femtocraft.conduit.chips.title")
        val content = Column(listOf(
            ChipPicker(selection),
            Label({
                val chip = selection.chip()
                val data = Chips.settingsOf(chip)
                if (chip.isEmpty || data == null) Component.translatable("gui.femtocraft.conduit.chips.none")
                else Component.translatable("gui.femtocraft.conduit.chips.settings", Direction.from3DDataValue(selection.index / LogisticsConduit.CHIPS_PER_FACE).serializedName, data.direction.name.lowercase(), data.interfaceDirection.serializedName)
            }, fixedWidth = ChipPicker.WIDTH),
            Row(listOf(
                ButtonComponent(60, 14, { Component.translatable("gui.femtocraft.conduit.mode") }, { selection.send(ConduitMenu.ACTION_MODE) },
                    Component.translatable("gui.femtocraft.conduit.mode.tip"), ButtonAccents.IO, selection::hasChip),
                ButtonComponent(60, 14, { Component.translatable("gui.femtocraft.conduit.interface") }, { selection.send(ConduitMenu.ACTION_INTERFACE) },
                    Component.translatable("gui.femtocraft.conduit.interface.tip"), ButtonAccents.IO, selection::hasChip),
            )),
            FilterRow({ selection.filter() }, { action ->
                if (selection.hasChip()) ClientPacketDistributor.sendToServer(MenuActionPayload(menu.containerId, ConduitMenu.ACTION_FILTER,
                    ConduitMenu.filterData(selection.index / LogisticsConduit.CHIPS_PER_FACE, selection.index % LogisticsConduit.CHIPS_PER_FACE, action)))
            }, setHint = Component.translatable("gui.femtocraft.conduit.filter.set")),
        ), gap = 5)
        return SidePanel(Component.translatable("gui.femtocraft.conduit.chips.tab"), title, TitledPanel(title, content),
            icon = ItemStack(LogisticsContent.ITEM_CHIP.get()), accent = ButtonAccents.IO)
    }

    /** The selected chip's slot (face * [LogisticsConduit.CHIPS_PER_FACE] + index), or -1. */
    class Selection(private val menu: ConduitMenu) {
        var index = -1

        fun chipAt(i: Int): ItemStack = menu.slots.getOrNull(i)?.item?.takeIf(Chips::isChip) ?: ItemStack.EMPTY

        fun chip(): ItemStack = chipAt(index)

        fun hasChip(): Boolean = !chip().isEmpty

        /** The selected chip's filter, or null if no chip that takes one is selected. */
        fun filter(): ResourceFilter<*>? {
            val chip = chip()
            val kind = Chips.kindOf(chip)?.takeIf { it.filterable } ?: return null
            return kind.data(chip, null).filter
        }

        fun send(action: Int) {
            if (!hasChip()) return
            val backward = Minecraft.getInstance().hasShiftDown()
            ClientPacketDistributor.sendToServer(MenuActionPayload(menu.containerId, action,
                ConduitMenu.data(index / LogisticsConduit.CHIPS_PER_FACE, index % LogisticsConduit.CHIPS_PER_FACE, backward)))
        }
    }

    /**
     * The conduit's chips laid out as on the main page (two faces a row, four chips a face); clicking a chip selects
     * it, nothing more.
     */
    class ChipPicker(private val selection: Selection) : ScreenComponent(WIDTH, 3 * ROW) {
        private fun cellX(slot: Int): Int {
            val face = slot / LogisticsConduit.CHIPS_PER_FACE
            return x + (face % 2) * (FACE_W + GAP) + LETTER_W + (slot % LogisticsConduit.CHIPS_PER_FACE) * CELL
        }

        private fun cellY(slot: Int): Int = y + (slot / LogisticsConduit.CHIPS_PER_FACE / 2) * ROW

        private fun slotAt(mx: Double, my: Double): Int = (0 until SLOTS).firstOrNull {
            mx >= cellX(it) && mx < cellX(it) + CELL && my >= cellY(it) && my < cellY(it) + CELL
        } ?: -1

        override fun extract(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, host: ComponentHost) {
            val font = host.hostFont
            for (face in Direction.entries) {
                val i = face.get3DDataValue()
                val first = i * LogisticsConduit.CHIPS_PER_FACE
                graphics.text(font, Component.literal(face.serializedName.substring(0, 1).uppercase()), cellX(first) - LETTER_W + 1, cellY(first) + 5, ScreenStyle.TEXT, false)
            }
            val hovered = slotAt(mouseX.toDouble(), mouseY.toDouble())
            for (slot in 0 until SLOTS) {
                val cx = cellX(slot)
                val cy = cellY(slot)
                ScreenStyle.slot(graphics, cx + 1, cy + 1)
                val chip = selection.chipAt(slot)
                if (!chip.isEmpty) graphics.item(chip, cx + 1, cy + 1)
                if (slot == selection.index && !chip.isEmpty) outline(graphics, cx, cy)
                if (slot == hovered && !chip.isEmpty) {
                    graphics.fill(cx + 1, cy + 1, cx + 17, cy + 17, HOVER)
                    graphics.setTooltipForNextFrame(font, Screen.getTooltipFromItem(Minecraft.getInstance(), chip), chip.tooltipImage, chip, mouseX, mouseY)
                }
            }
        }

        private fun outline(graphics: GuiGraphicsExtractor, cx: Int, cy: Int) {
            graphics.fill(cx, cy, cx + CELL, cy + 1, SELECTED)
            graphics.fill(cx, cy + CELL - 1, cx + CELL, cy + CELL, SELECTED)
            graphics.fill(cx, cy, cx + 1, cy + CELL, SELECTED)
            graphics.fill(cx + CELL - 1, cy, cx + CELL, cy + CELL, SELECTED)
        }

        override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
            val slot = slotAt(event.x(), event.y())
            if (slot < 0) return false
            if (selection.chipAt(slot).isEmpty) return true
            selection.index = slot
            Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f))
            return true
        }

        companion object {
            const val CELL = 18
            const val LETTER_W = 10
            const val GAP = 6
            const val ROW = 20
            const val FACE_W = LETTER_W + LogisticsConduit.CHIPS_PER_FACE * CELL
            const val WIDTH = 2 * FACE_W + GAP
            const val SLOTS = 6 * LogisticsConduit.CHIPS_PER_FACE
            private const val SELECTED = 0xFFFFFF55.toInt()
            private const val HOVER = 0x60FFFFFF
        }
    }
}
