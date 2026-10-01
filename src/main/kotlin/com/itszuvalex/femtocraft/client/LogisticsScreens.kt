package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.logistics.ConduitMenu
import com.itszuvalex.femtocraft.logistics.FluidRepositoryBlockEntity
import com.itszuvalex.femtocraft.logistics.FluidRepositoryMenu
import com.itszuvalex.femtocraft.logistics.ItemRepositoryMenu
import com.itszuvalex.femtocraft.logistics.NanoPackMenu
import com.itszuvalex.femtocraft.logistics.NaniteRepositoryBlockEntity
import com.itszuvalex.femtocraft.logistics.NaniteRepositoryMenu
import com.itszuvalex.femtocraft.nanite.NaniteMachineMenu
import com.itszuvalex.itszulib.client.ScreenHelpers
import com.itszuvalex.itszulib.menu.MenuActionPayload
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.Button
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import com.itszuvalex.femtocraft.logistics.ItemChips
import net.minecraft.world.entity.player.Inventory
import net.neoforged.neoforge.client.network.ClientPacketDistributor

class ItemRepositoryScreen(menu: ItemRepositoryMenu, inventory: Inventory, title: Component) : FemtoScreen<ItemRepositoryMenu>(menu, inventory, title, 176, 222)

class FluidRepositoryScreen(menu: FluidRepositoryMenu, inventory: Inventory, title: Component) : FemtoScreen<FluidRepositoryMenu>(menu, inventory, title) {
    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        graphics.fill(leftPos + 79, topPos + 17, leftPos + 97, topPos + 71, SLOT)
        ScreenHelpers.fluidTank(graphics, leftPos + 80, topPos + 18, 16, 52, menu.tank.toMinecraft(), FluidRepositoryBlockEntity.TANK_SIZE)
        ScreenHelpers.tooltipIfHovered(graphics, mouseX, mouseY, leftPos + 80, topPos + 18, 16, 52, ScreenHelpers.fluidTooltip(menu.tank.toMinecraft(), FluidRepositoryBlockEntity.TANK_SIZE))
    }
}

class NaniteRepositoryScreen(menu: NaniteRepositoryMenu, inventory: Inventory, title: Component) : FemtoScreen<NaniteRepositoryMenu>(menu, inventory, title) {
    override fun init() {
        super.init()
        addRenderableWidget(Button.builder(Component.translatable("gui.femtocraft.nanite.fill")) { send(NaniteMachineMenu.ACTION_FILL) }.bounds(leftPos + 128, topPos + 54, 40, 14).build())
        addRenderableWidget(Button.builder(Component.translatable("gui.femtocraft.nanite.drain")) { send(NaniteMachineMenu.ACTION_DRAIN) }.bounds(leftPos + 128, topPos + 6, 40, 14).build())
    }

    private fun send(action: Int) = ClientPacketDistributor.sendToServer(MenuActionPayload(menu.containerId, action, 0))

    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        text(graphics, Component.translatable("gui.femtocraft.nanite.tank", menu.tank.sumOf { it.amount }, NaniteRepositoryBlockEntity.VOLUME), 8, 24)
        text(graphics, Component.translatable("gui.femtocraft.nanite.player", menu.playerTank.sumOf { it.amount }), 8, 40)
    }
}

/**
 * Click a chip to select it; the buttons cycle the selected chip's direction and interface face (shift goes
 * backwards).
 */
class ConduitScreen(menu: ConduitMenu, inventory: Inventory, title: Component) : FemtoScreen<ConduitMenu>(menu, inventory, title, 176, 186) {
    private var selected = -1

    override fun init() {
        super.init()
        addRenderableWidget(Button.builder(Component.translatable("gui.femtocraft.conduit.mode")) { send(ConduitMenu.ACTION_MODE) }.bounds(leftPos + 8, topPos + 80, 40, 14).build())
        addRenderableWidget(Button.builder(Component.translatable("gui.femtocraft.conduit.interface")) { send(ConduitMenu.ACTION_INTERFACE) }.bounds(leftPos + 50, topPos + 80, 40, 14).build())
    }

    private fun send(action: Int) {
        if (selected < 0) return
        val backward = minecraft.hasShiftDown()
        ClientPacketDistributor.sendToServer(MenuActionPayload(menu.containerId, action, ConduitMenu.data(selected / 4, selected % 4, backward)))
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        hoveredSlot?.let { if (it.container !is Inventory && it.index < 24) selected = it.index }
        return super.mouseClicked(event, doubleClick)
    }

    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        for (face in Direction.entries) {
            val i = face.get3DDataValue()
            text(graphics, Component.literal(face.serializedName.substring(0, 1).uppercase()), 16 + (i % 2) * 76, 22 + (i / 2) * 20)
        }
        val slot = menu.slots.getOrNull(selected) ?: return
        graphics.fill(leftPos + slot.x - 1, topPos + slot.y + 16, leftPos + slot.x + 17, topPos + slot.y + 17, SELECTED)
        val data = slot.item.get(ItemChips.CONNECTION.get()) ?: return
        text(graphics, Component.literal("${data.direction.name.lowercase()} / ${data.interfaceDirection.serializedName}"), 94, 83)
    }

    companion object {
        private const val SELECTED = 0xFFFFFF55.toInt()
    }
}

class NanoPackScreen(menu: NanoPackMenu, inventory: Inventory, title: Component) : FemtoScreen<NanoPackMenu>(menu, inventory, title)
