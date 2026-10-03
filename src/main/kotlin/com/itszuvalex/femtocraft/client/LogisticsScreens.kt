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
 * The fluid reservoir: its tanks (one gauge per run of linked cells, as wide as its cells), a link button between each
 * pair of neighbouring cells ("+" links them, "-" splits them again; unavailable while their tanks hold different
 * fluids or locks) and a lock button under each tank. A locked tank shows a padlock, and while empty a faint fill of
 * the fluid it waits for. Holding a bucket of a fluid while locking an empty tank locks it to that fluid.
 */
class FluidReservoirScreen(menu: FluidReservoirMenu, inventory: Inventory, title: Component) :
    FemtoScreen<FluidReservoirMenu>(menu, inventory, title, 176, FluidReservoirMenu.HEIGHT) {
    init {
        inventoryLabelY = FluidReservoirMenu.INVENTORY_Y - 11
    }

    override fun addComponents() {
        addComponent(ReservoirTanksView(menu), Anchor.TOP, 0, 17)
    }
}

/** The reservoir screen's tanks, link and lock buttons ([FluidReservoirScreen]). */
class ReservoirTanksView(private val menu: FluidReservoirMenu) : com.itszuvalex.itszulib.client.screen.ScreenComponent(
    FluidReservoirState.TANKS * COL - GAP, GAUGE_H + 2 * ROW_GAP + LINK_H + LOCK_H,
) {
    private val tanks get() = menu.view

    private fun gaugeX(group: IntRange) = x + group.first * COL
    private fun gaugeW(group: IntRange) = group.count() * COL - GAP
    private fun linkRect(boundary: Int) = intArrayOf(x + (boundary + 1) * COL - GAP / 2 - LINK_W / 2, y + GAUGE_H + ROW_GAP, LINK_W, LINK_H)
    private fun lockRect(group: IntRange) = intArrayOf(gaugeX(group), y + GAUGE_H + 2 * ROW_GAP + LINK_H, gaugeW(group), LOCK_H)

    private fun over(r: IntArray, mx: Number, my: Number) = mx.toDouble() >= r[0] && mx.toDouble() < r[0] + r[2] && my.toDouble() >= r[1] && my.toDouble() < r[1] + r[3]

    private fun fluidName(id: net.minecraft.resources.Identifier): Component =
        net.minecraft.core.registries.BuiltInRegistries.FLUID.getValue(id).fluidType.description

    override fun extract(graphics: net.minecraft.client.gui.GuiGraphicsExtractor, mouseX: Int, mouseY: Int, host: com.itszuvalex.itszulib.client.screen.ComponentHost) {
        val font = host.hostFont
        val style = com.itszuvalex.itszulib.client.screen.ScreenStyle
        tanks.groups.forEachIndexed { t, g ->
            val gx = gaugeX(g)
            val gw = gaugeW(g)
            val stack = tanks.get(t).toMinecraft()
            val lock = tanks.lockOf(t)
            style.frame(graphics, gx, y, gw, GAUGE_H)
            graphics.fill(gx, y, gx + gw, y + GAUGE_H, style.DARK)
            if (stack.isEmpty && lock != null) {
                // The fluid it waits for, faint.
                ScreenHelpers.fluidTank(graphics, gx, y, gw, GAUGE_H, net.neoforged.neoforge.fluids.FluidStack(net.minecraft.core.registries.BuiltInRegistries.FLUID.getValue(lock), 1), 1)
                graphics.fill(gx, y, gx + gw, y + GAUGE_H, (style.DARK and 0xFFFFFF) or (0xB0 shl 24))
            } else {
                ScreenHelpers.fluidTank(graphics, gx, y, gw, GAUGE_H, stack, tanks.capacity(t))
            }
            if (lock != null) padlock(graphics, gx + gw - 8, y + 2)
            val lines = ArrayList(ScreenHelpers.fluidTooltip(stack, tanks.capacity(t)))
            lines += if (lock != null) Component.translatable("gui.femtocraft.reservoir.locked", fluidName(lock)) else Component.translatable("gui.femtocraft.reservoir.unlocked")
            if (g.count() > 1) lines += Component.translatable("gui.femtocraft.reservoir.linked", g.count())
            ScreenHelpers.tooltipIfHovered(graphics, mouseX, mouseY, gx, y, gw, GAUGE_H, lines)

            val r = lockRect(g)
            val hovered = over(r, mouseX, mouseY)
            style.button(graphics, r[0], r[1], r[2], r[3], if (lock != null) com.itszuvalex.itszulib.client.screen.ScreenStyle.ButtonState.SELECTED else if (hovered) com.itszuvalex.itszulib.client.screen.ScreenStyle.ButtonState.HOVERED else com.itszuvalex.itszulib.client.screen.ScreenStyle.ButtonState.IDLE,
                style.theme.accent(com.itszuvalex.itszulib.client.screen.ButtonAccents.INFO))
            val label = Component.translatable(if (lock != null) "gui.femtocraft.reservoir.unlock" else "gui.femtocraft.reservoir.lock")
            graphics.text(font, label, r[0] + (r[2] - font.width(label)) / 2, r[1] + 2, style.TEXT, false)
            if (hovered) graphics.setTooltipForNextFrame(font, Component.translatable(if (lock != null) "gui.femtocraft.reservoir.unlock.tip" else "gui.femtocraft.reservoir.lock.tip"), mouseX, mouseY)
        }
        for (b in 0 until FluidReservoirState.TANKS - 1) {
            val r = linkRect(b)
            val linked = tanks.isLinked(b)
            val usable = linked || tanks.canLink(b)
            val hovered = over(r, mouseX, mouseY)
            val state = when {
                !usable -> com.itszuvalex.itszulib.client.screen.ScreenStyle.ButtonState.INACTIVE
                hovered -> com.itszuvalex.itszulib.client.screen.ScreenStyle.ButtonState.HOVERED
                else -> com.itszuvalex.itszulib.client.screen.ScreenStyle.ButtonState.IDLE
            }
            style.button(graphics, r[0], r[1], r[2], r[3], state, style.theme.accent(com.itszuvalex.itszulib.client.screen.ButtonAccents.IO))
            val label = if (linked) "-" else "+"
            graphics.text(font, label, r[0] + (r[2] - font.width(label)) / 2 + 1, r[1] + 1, if (usable) style.TEXT else style.TEXT_MUTED, false)
            if (hovered) {
                val tip = when {
                    linked -> "gui.femtocraft.reservoir.unlink"
                    usable -> "gui.femtocraft.reservoir.link"
                    else -> "gui.femtocraft.reservoir.link.blocked"
                }
                graphics.setTooltipForNextFrame(font, Component.translatable(tip, b + 1, b + 2), mouseX, mouseY)
            }
        }
    }

    /** A small padlock: a shackle over a body. */
    private fun padlock(graphics: net.minecraft.client.gui.GuiGraphicsExtractor, px: Int, py: Int) {
        val c = com.itszuvalex.itszulib.client.screen.ScreenStyle.TEXT
        val shadow = 0xC0000000.toInt()
        graphics.fill(px - 1, py - 1, px + 7, py + 9, shadow)
        graphics.fill(px + 1, py, px + 5, py + 1, c)
        graphics.fill(px + 1, py, px + 2, py + 4, c)
        graphics.fill(px + 4, py, px + 5, py + 4, c)
        graphics.fill(px, py + 4, px + 6, py + 8, c)
    }

    override fun mouseClicked(event: net.minecraft.client.input.MouseButtonEvent, doubleClick: Boolean): Boolean {
        for (g in tanks.groups) if (over(lockRect(g), event.x(), event.y())) return send(FluidReservoirMenu.ACTION_LOCK, g.first)
        for (b in 0 until FluidReservoirState.TANKS - 1) {
            if (!over(linkRect(b), event.x(), event.y())) continue
            if (!tanks.isLinked(b) && !tanks.canLink(b)) return true
            return send(FluidReservoirMenu.ACTION_LINK, b)
        }
        return false
    }

    private fun send(action: Int, data: Int): Boolean {
        net.minecraft.client.Minecraft.getInstance().soundManager.play(
            net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1f))
        ClientPacketDistributor.sendToServer(MenuActionPayload(menu.containerId, action, data))
        return true
    }

    companion object {
        const val COL = 40
        const val GAP = 6
        const val GAUGE_H = 52
        const val ROW_GAP = 3
        const val LINK_W = 12
        const val LINK_H = 11
        const val LOCK_H = 12
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
