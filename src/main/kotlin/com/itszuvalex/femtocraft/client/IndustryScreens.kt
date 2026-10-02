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
import net.minecraft.client.gui.components.Button
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
            graphics.fill(leftPos + 151, topPos + 17, leftPos + 169, topPos + 71, SLOT)
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
        graphics.fill(leftPos + 151, topPos + 17, leftPos + 169, topPos + 71, SLOT)
        ScreenHelpers.fluidTank(graphics, leftPos + 152, topPos + 18, 16, 52, water, GerminationState.TANK_SIZE)
        ScreenHelpers.tooltipIfHovered(graphics, mouseX, mouseY, leftPos + 152, topPos + 18, 16, 52, ScreenHelpers.fluidTooltip(water, GerminationState.TANK_SIZE))
    }
}

class FocusingChamberScreen(menu: FocusingChamberMenu, inventory: Inventory, title: Component) : FemtoScreen<FocusingChamberMenu>(menu, inventory, title)

class FrameScreen(menu: FrameMenu, inventory: Inventory, title: Component) : FemtoScreen<FrameMenu>(menu, inventory, title) {
    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        if (menu.building) {
            text(graphics, Component.translatable("gui.femtocraft.constructing"), 8, 72 - 4)
            progress(graphics, 120, 70, 48, 4, menu.progress.toDouble() / FrameState.BUILD_TIME)
        }
    }
}

/**
 * One button per multiblock the frame can build (v3's `GuiMultiblockSelection`).
 */
class FrameSelectionScreen(menu: FrameSelectionMenu, inventory: Inventory, title: Component) : FemtoScreen<FrameSelectionMenu>(menu, inventory, title) {
    override fun init() {
        super.init()
        menu.options.forEachIndexed { i, multi ->
            addRenderableWidget(Button.builder(multi.displayName) { minecraft!!.gameMode!!.handleInventoryButtonClick(menu.containerId, i) }
                .bounds(leftPos + 8, topPos + 20 + i * 22, imageWidth - 16, 20).build())
        }
    }

    override fun extractLabels(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        graphics.text(font, title, titleLabelX, titleLabelY, TEXT, false)
    }
}
