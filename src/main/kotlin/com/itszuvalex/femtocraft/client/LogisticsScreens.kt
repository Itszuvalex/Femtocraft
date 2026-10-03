package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.logistics.ConduitMenu
import com.itszuvalex.femtocraft.logistics.FluidReservoirMenu
import com.itszuvalex.femtocraft.logistics.FluidReservoirState
import com.itszuvalex.femtocraft.logistics.ItemVaultMenu
import com.itszuvalex.femtocraft.logistics.NaniteVaultMenu
import com.itszuvalex.femtocraft.logistics.NaniteVaultState
import com.itszuvalex.itszulib.client.screen.Anchor
import com.itszuvalex.itszulib.client.screen.FluidGauge
import com.itszuvalex.itszulib.client.screen.Row
import com.itszuvalex.itszulib.client.screen.StorageTerminalView
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
import com.itszuvalex.itszulib.client.screen.ButtonAccents
import com.itszuvalex.itszulib.client.screen.ThemedButton
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import com.itszuvalex.femtocraft.logistics.Chips
import net.minecraft.world.entity.player.Inventory
import net.neoforged.neoforge.client.network.ClientPacketDistributor

class ItemRepositoryScreen(menu: ItemRepositoryMenu, inventory: Inventory, title: Component) : FemtoScreen<ItemRepositoryMenu>(menu, inventory, title, 176, 222)

class FluidRepositoryScreen(menu: FluidRepositoryMenu, inventory: Inventory, title: Component) : FemtoScreen<FluidRepositoryMenu>(menu, inventory, title) {
    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        inset(graphics, 79, 17, 18, 54)
        ScreenHelpers.fluidTank(graphics, leftPos + 80, topPos + 18, 16, 52, menu.tank.toMinecraft(), FluidRepositoryBlockEntity.TANK_SIZE)
        ScreenHelpers.tooltipIfHovered(graphics, mouseX, mouseY, leftPos + 80, topPos + 18, 16, 52, ScreenHelpers.fluidTooltip(menu.tank.toMinecraft(), FluidRepositoryBlockEntity.TANK_SIZE))
    }
}

class NaniteRepositoryScreen(menu: NaniteRepositoryMenu, inventory: Inventory, title: Component) : FemtoScreen<NaniteRepositoryMenu>(menu, inventory, title) {
    override fun init() {
        super.init()
        addRenderableWidget(ThemedButton(leftPos + 128, topPos + 54, 40, 14, Component.translatable("gui.femtocraft.nanite.fill"), { send(NaniteMachineMenu.ACTION_FILL) }, accent = ButtonAccents.IO))
        addRenderableWidget(ThemedButton(leftPos + 128, topPos + 6, 40, 14, Component.translatable("gui.femtocraft.nanite.drain"), { send(NaniteMachineMenu.ACTION_DRAIN) }, accent = ButtonAccents.IO))
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
        addRenderableWidget(ThemedButton(leftPos + 8, topPos + 80, 40, 14, Component.translatable("gui.femtocraft.conduit.mode"), { send(ConduitMenu.ACTION_MODE) }, accent = ButtonAccents.IO))
        addRenderableWidget(ThemedButton(leftPos + 50, topPos + 80, 40, 14, Component.translatable("gui.femtocraft.conduit.interface"), { send(ConduitMenu.ACTION_INTERFACE) }, accent = ButtonAccents.IO))
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
        val data = Chips.settingsOf(slot.item) ?: return
        text(graphics, Component.literal("${data.direction.name.lowercase()} / ${data.interfaceDirection.serializedName}"), 94, 83)
    }

    companion object {
        private const val SELECTED = 0xFFFFFF55.toInt()
    }
}

class NanoPackScreen(menu: NanoPackMenu, inventory: Inventory, title: Component) : FemtoScreen<NanoPackMenu>(menu, inventory, title)

/**
 * The item vault: a storage terminal (search box with search modes, sort, paged grid with counts) over the vault, and
 * the player's inventory below.
 */
class ItemVaultScreen(menu: ItemVaultMenu, inventory: Inventory, title: Component) :
    FemtoScreen<ItemVaultMenu>(menu, inventory, title, 176, ItemVaultMenu.HEIGHT) {
    init {
        inventoryLabelY = ItemVaultMenu.INVENTORY_Y - 11
    }

    override fun addComponents() {
        addComponent(StorageTerminalView(menu.vault, 9, ItemVaultMenu.ROWS), 8, 17)
    }
}

/**
 * The fluid reservoir: its four tanks.
 */
class FluidReservoirScreen(menu: FluidReservoirMenu, inventory: Inventory, title: Component) : FemtoScreen<FluidReservoirMenu>(menu, inventory, title) {
    override fun addComponents() {
        val gauges = (0 until FluidReservoirState.TANKS).map { i ->
            FluidGauge({ menu.tanks.get(i).toMinecraft() }, { FluidReservoirState.CAPACITY }, 24, 56)
        }
        addComponent(Row(gauges, gap = 14), Anchor.TOP, 0, 18)
    }
}

/**
 * The nanite vault: its strains (the largest first), the player's nanites, and fill and drain buttons.
 */
class NaniteVaultScreen(menu: NaniteVaultMenu, inventory: Inventory, title: Component) : FemtoScreen<NaniteVaultMenu>(menu, inventory, title) {
    override fun init() {
        super.init()
        addRenderableWidget(ThemedButton(leftPos + 128, topPos + 54, 40, 14, Component.translatable("gui.femtocraft.nanite.fill"), { send(NaniteMachineMenu.ACTION_FILL) }, accent = ButtonAccents.IO))
        addRenderableWidget(ThemedButton(leftPos + 128, topPos + 6, 40, 14, Component.translatable("gui.femtocraft.nanite.drain"), { send(NaniteMachineMenu.ACTION_DRAIN) }, accent = ButtonAccents.IO))
    }

    private fun send(action: Int) = ClientPacketDistributor.sendToServer(MenuActionPayload(menu.containerId, action, 0))

    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        text(graphics, Component.translatable("gui.femtocraft.nanite.tank", menu.tank.sumOf { it.amount }, NaniteVaultState.VOLUME), 8, 20)
        val strains = menu.tank.sortedByDescending { it.amount }
        strains.take(STRAIN_LINES).forEachIndexed { i, s ->
            text(graphics, Component.translatable("gui.femtocraft.nanite_vault.strain", s.strain, "${s.version.major}.${s.version.minor}", s.amount), 12, 32 + i * 10)
        }
        if (strains.size > STRAIN_LINES) text(graphics, Component.translatable("gui.femtocraft.nanite_vault.more", strains.size - STRAIN_LINES), 12, 32 + STRAIN_LINES * 10)
        text(graphics, Component.translatable("gui.femtocraft.nanite.player", menu.playerTank.sumOf { it.amount }), 8, 72)
    }

    companion object {
        const val STRAIN_LINES = 3
    }
}
