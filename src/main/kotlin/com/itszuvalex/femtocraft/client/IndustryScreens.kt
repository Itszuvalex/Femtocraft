package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.industry.Autocrafter
import com.itszuvalex.femtocraft.industry.AutocrafterMenu
import com.itszuvalex.femtocraft.industry.CrystalLiquifierBlockEntity
import com.itszuvalex.itszulib.client.screen.ButtonAccents
import com.itszuvalex.itszulib.menu.MenuActionPayload
import net.neoforged.neoforge.client.network.ClientPacketDistributor
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
 * A frame structure, titled with the multiblock it builds: what that needs, one slot each with the item's name beside
 * it (ItszuLib's requirement slots: the wanted item faded with how many are still needed in red, then the full amount
 * in green; nothing comes back out), or its build progress.
 */
class FrameScreen(menu: FrameMenu, inventory: Inventory, title: Component) : FemtoScreen<FrameMenu>(menu, inventory, title) {
    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        if (menu.building) {
            text(graphics, Component.translatable("gui.femtocraft.constructing"), 8, 22)
            progress(graphics, 8, 34, 160, 4, menu.progress.toDouble() / FrameState.BUILD_TIME)
            return
        }
        if (menu.needs.isEmpty()) {
            text(graphics, Component.translatable("gui.femtocraft.frame.needs_nothing"), 8, 22)
            return
        }
        val nameWidth = FrameMenu.COLUMN_WIDTH - 22
        menu.needs.forEachIndexed { i, need ->
            val slot = menu.slots.getOrNull(i) ?: return@forEachIndexed
            val name = need.hoverName.string
            val shown = if (font.width(name) <= nameWidth) name else font.plainSubstrByWidth(name, nameWidth - font.width("...")) + "..."
            graphics.text(font, shown, leftPos + slot.x + 20, topPos + slot.y + 4, TEXT, false)
            if (shown != name) ScreenHelpers.tooltipIfHovered(graphics, mouseX, mouseY, leftPos + slot.x + 20, topPos + slot.y, nameWidth, 16, listOf(need.hoverName))
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

/**
 * The autocrafter (DECISIONS D31): the grid (what each slot wants shown faded), the result, FLOPS towards the next
 * craft, the template slot, the output, and the Set recipe and Clear recipe buttons.
 */
class AutocrafterScreen(menu: AutocrafterMenu, inventory: Inventory, title: Component) :
    FemtoScreen<AutocrafterMenu>(menu, inventory, title, height = AutocrafterMenu.HEIGHT) {
    override fun init() {
        super.init()
        addRenderableWidget(ThemedButton(
            leftPos + AutocrafterMenu.GRID_X, topPos + 74, 54, 14, Component.translatable("gui.femtocraft.autocrafter.set"),
            { send(AutocrafterMenu.ACTION_SET) }, Component.translatable("gui.femtocraft.autocrafter.set.tooltip"), accent = ButtonAccents.IO,
        ))
        addRenderableWidget(ThemedButton(
            leftPos + AutocrafterMenu.OUTPUT_X, topPos + 74, 54, 14, Component.translatable("gui.femtocraft.autocrafter.clear"),
            { send(AutocrafterMenu.ACTION_CLEAR) }, Component.translatable("gui.femtocraft.autocrafter.clear.tooltip"), accent = ButtonAccents.DANGER,
        ))
    }

    private fun send(action: Int) = ClientPacketDistributor.sendToServer(MenuActionPayload(menu.containerId, action, 0))

    override fun addComponents() {
        addPowerGauge(8, 18) { menu.battery }
    }

    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        val x = AutocrafterMenu.RESULT_X
        val y = AutocrafterMenu.RESULT_Y
        inset(graphics, x - 1, y - 1, 18, 18)
        if (!menu.result.isEmpty) {
            graphics.item(menu.result, leftPos + x, topPos + y)
            if (menu.result.count > 1) {
                val count = menu.result.count.toString()
                graphics.text(font, count, leftPos + x + 17 - font.width(count), topPos + y + 9, 0xFFFFFFFF.toInt(), true)
            }
            if (mouseX in leftPos + x until leftPos + x + 16 && mouseY in topPos + y until topPos + y + 16) {
                val lines = net.minecraft.client.gui.screens.Screen.getTooltipFromItem(net.minecraft.client.Minecraft.getInstance(), menu.result)
                graphics.setTooltipForNextFrame(font, lines, menu.result.tooltipImage, menu.result, mouseX, mouseY)
            }
        }
        progress(graphics, x - 3, y + 21, 22, 4, menu.flops / Autocrafter.FLOPS_PER_CRAFT)
        ScreenHelpers.tooltipIfHovered(graphics, mouseX, mouseY, leftPos + x - 3, topPos + y + 21, 22, 4, listOf(
            Component.translatable("gui.femtocraft.autocrafter.flops", fmt(menu.flops), fmt(Autocrafter.FLOPS_PER_CRAFT)),
        ))
        if (menu.editing()) text(graphics, Component.translatable("gui.femtocraft.autocrafter.editing"), AutocrafterMenu.GRID_X, 92)
    }
}
