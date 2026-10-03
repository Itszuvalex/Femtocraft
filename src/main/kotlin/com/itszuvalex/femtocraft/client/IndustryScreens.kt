package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.industry.CrystalLiquifierBlockEntity
import com.itszuvalex.femtocraft.industry.FocusingChamberMenu
import com.itszuvalex.femtocraft.industry.FrameMenu
import com.itszuvalex.femtocraft.industry.FrameSelectionMenu
import com.itszuvalex.femtocraft.industry.FrameState
import com.itszuvalex.femtocraft.industry.GerminationChamberMenu
import com.itszuvalex.femtocraft.industry.GerminationState
import com.itszuvalex.femtocraft.industry.MachineMenu
import com.itszuvalex.itszulib.client.ScreenHelpers
import net.minecraft.client.gui.GuiGraphicsExtractor
import com.itszuvalex.itszulib.client.screen.ThemedButton
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.level.material.Fluids
import net.neoforged.neoforge.fluids.FluidStack

class MachineScreen(menu: MachineMenu, inventory: Inventory, title: Component) : FemtoScreen<MachineMenu>(menu, inventory, title) {
    override fun addComponents() {
        addPowerGauge(8, 18) { menu.battery }
    }

    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        progress(graphics, 79, 40, 24, 6, menu.progress)
        if (menu.blockEntity is CrystalLiquifierBlockEntity) {
            inset(graphics, 151, 17, 18, 54)
            ScreenHelpers.fluidTank(graphics, leftPos + 152, topPos + 18, 16, 52, menu.tank.toMinecraft(), CrystalLiquifierBlockEntity.TANK_SIZE)
            ScreenHelpers.tooltipIfHovered(graphics, mouseX, mouseY, leftPos + 152, topPos + 18, 16, 52, ScreenHelpers.fluidTooltip(menu.tank.toMinecraft(), CrystalLiquifierBlockEntity.TANK_SIZE))
        }
    }
}

class GerminationChamberScreen(menu: GerminationChamberMenu, inventory: Inventory, title: Component) : FemtoScreen<GerminationChamberMenu>(menu, inventory, title) {
    override fun addComponents() {
        addPowerGauge(8, 18) { menu.battery }
    }

    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        progress(graphics, 66, 40, 24, 6, menu.progress)
        val water = FluidStack(Fluids.WATER, menu.water)
        inset(graphics, 151, 17, 18, 54)
        ScreenHelpers.fluidTank(graphics, leftPos + 152, topPos + 18, 16, 52, water, GerminationState.TANK_SIZE)
        ScreenHelpers.tooltipIfHovered(graphics, mouseX, mouseY, leftPos + 152, topPos + 18, 16, 52, ScreenHelpers.fluidTooltip(water, GerminationState.TANK_SIZE))
    }
}

class FocusingChamberScreen(menu: FocusingChamberMenu, inventory: Inventory, title: Component) : FemtoScreen<FocusingChamberMenu>(menu, inventory, title)

/**
 * A frame structure: its resource slots, and on the left what the multiblock it builds needs, each as the item with
 * `held/needed` (green once the frame holds enough). The frame starts building once everything is in.
 */
class FrameScreen(menu: FrameMenu, inventory: Inventory, title: Component) : FemtoScreen<FrameMenu>(menu, inventory, title) {
    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        if (menu.building) {
            text(graphics, Component.translatable("gui.femtocraft.constructing"), 8, 72 - 4)
            progress(graphics, 120, 70, 48, 4, menu.progress.toDouble() / FrameState.BUILD_TIME)
            return
        }
        val multi = menu.multiblock ?: return
        text(graphics, multi.displayName, 8, 72 - 4)
        val needs = menu.requirements()
        if (needs.isEmpty()) {
            text(graphics, Component.translatable("gui.femtocraft.frame.needs_nothing"), 8, 20)
            return
        }
        needs.forEachIndexed { i, (need, have) ->
            val x = leftPos + 8 + (i / ROWS) * COLUMN
            val y = topPos + 17 + (i % ROWS) * 18
            graphics.fakeItem(need, x, y)
            val done = have >= need.count
            graphics.text(font, "$have/${need.count}", x + 18, y + 4, if (done) DONE else MISSING, false)
            ScreenHelpers.tooltipIfHovered(graphics, mouseX, mouseY, x, y, 16, 16, listOf(need.hoverName, Component.translatable("gui.femtocraft.frame.held", have, need.count)))
        }
    }

    companion object {
        private const val ROWS = 3
        private const val COLUMN = 26
        private const val DONE = 0xFF2E8B2E.toInt()
        private const val MISSING = 0xFFB02020.toInt()
    }
}

/**
 * One button per multiblock the frame can build (v3's `GuiMultiblockSelection`).
 */
class FrameSelectionScreen(menu: FrameSelectionMenu, inventory: Inventory, title: Component) : FemtoScreen<FrameSelectionMenu>(menu, inventory, title) {
    override fun init() {
        super.init()
        menu.options.forEachIndexed { i, multi ->
            val needs = listOf(Component.translatable("tooltip.femtocraft.frame.needs"), Component.translatable("tooltip.femtocraft.frame.needs.frames", multi.numFrames)) +
                multi.required().map { Component.translatable("tooltip.femtocraft.frame.needs.item", it.count, it.hoverName) }
            addRenderableWidget(ThemedButton(
                leftPos + 8, topPos + 20 + i * 22, imageWidth - 16, 20, multi.displayName,
                { minecraft!!.gameMode!!.handleInventoryButtonClick(menu.containerId, i) },
                needs.reduce { a, b -> a.copy().append("\n").append(b) },
            ))
        }
    }

    override fun extractLabels(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        graphics.text(font, title, titleLabelX, titleLabelY, TEXT, false)
    }
}
