package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.archive.NaniteHost
import com.itszuvalex.femtocraft.host.HostMenu
import com.itszuvalex.femtocraft.host.HostStats
import com.itszuvalex.femtocraft.host.HostStrain
import com.itszuvalex.femtocraft.host.HostStrains
import com.itszuvalex.femtocraft.host.NaniteArchetype
import com.itszuvalex.femtocraft.host.NaniteArchetypes
import com.itszuvalex.femtocraft.host.TalentState
import com.itszuvalex.femtocraft.host.TalentStat
import com.itszuvalex.femtocraft.host.TalentStats
import com.itszuvalex.femtocraft.host.Talents
import com.itszuvalex.femtocraft.nanite.NaniteStack
import com.itszuvalex.femtocraft.nanite.PlayerNanites
import com.itszuvalex.itszulib.client.screen.ButtonAccents
import com.itszuvalex.itszulib.client.screen.ButtonComponent
import com.itszuvalex.itszulib.client.screen.Column
import com.itszuvalex.itszulib.client.screen.ComponentHost
import com.itszuvalex.itszulib.client.screen.NodeLook
import com.itszuvalex.itszulib.client.screen.NodeState
import com.itszuvalex.itszulib.client.screen.NodeTreeModel
import com.itszuvalex.itszulib.client.screen.NodeTreeView
import com.itszuvalex.itszulib.client.screen.Row
import com.itszuvalex.itszulib.client.screen.ScreenComponent
import com.itszuvalex.itszulib.client.screen.ScreenStyle
import com.itszuvalex.itszulib.client.screen.SidePanel
import com.itszuvalex.itszulib.client.screen.StatRow
import com.itszuvalex.itszulib.client.screen.TitledPanel
import com.itszuvalex.itszulib.menu.MenuActionPayload
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.neoforged.neoforge.client.network.ClientPacketDistributor
import java.util.Optional

/**
 * The nanite host screen (the host key, [FemtoKeys.HOST]): the player's strains as talent trees, one per archetype
 * ([NaniteArchetypes]), with the talent points they have to spend; a talent's details and the buttons to integrate it
 * or reset the archetype's strain; and side panels with the nanites they carry (strain, version, talents, amount)
 * and their body as their talents make it ([HostStats]). Everything shown is synced to the player already (host,
 * tank and strain attachments, the talent registry); actions go through [HostMenu].
 */
class HostScreen(menu: HostMenu, inventory: Inventory, title: Component) : FemtoScreen<HostMenu>(menu, inventory, title, WIDTH, HEIGHT) {
    private var archetype: NaniteArchetype = NaniteArchetypes.ARCHIVE
    private var selected: Identifier? = null
    private var tree: NodeTreeView? = null

    private fun player(): Player? = minecraft.player

    private fun talents(): Talents = minecraft.level?.registryAccess()?.let(Talents::of) ?: Talents.EMPTY

    private fun strain(): HostStrain = player()?.let(HostStrains::of) ?: HostStrain.EMPTY

    override fun addComponents() {
        addComponent(ArchetypeTabs({ archetype }, { picked ->
            if (picked != archetype) {
                archetype = picked
                selected = null
                tree?.recentre()
            }
        }, { a -> strain().talents(a.id).size to talents().inArchetype(a.id).size }), 8, TABS_Y)
        tree = addComponent(NodeTreeView(WIDTH - 16, TREE_H, ::model, { selected }, { selected = it }), 8, TREE_Y)
        addComponent(Row(listOf(
            ButtonComponent(64, 14, { Component.translatable("gui.femtocraft.host.integrate") }, { send(HostMenu.ACTION_UNLOCK, HostMenu.networkId(minecraft.level!!.registryAccess(), selected)) },
                Component.translatable("gui.femtocraft.host.integrate.tip"), ButtonAccents.UPGRADE, ::canIntegrate),
            ButtonComponent(64, 14, { Component.translatable("gui.femtocraft.host.reset") }, { send(HostMenu.ACTION_RESET, NaniteArchetypes.ALL.indexOf(archetype)) },
                Component.translatable("gui.femtocraft.host.reset.tip"), ButtonAccents.DANGER, { strain().talents(archetype.id).isNotEmpty() }),
        ), gap = 4), WIDTH - 8 - 132, BUTTONS_Y)
        addPanel(SidePanel(Component.translatable("gui.femtocraft.host.nanites.tab"), Component.translatable("gui.femtocraft.host.nanites.title"),
            TitledPanel(Component.translatable("gui.femtocraft.host.nanites.title"), NanitesPanel(::player, ::talents)), icon = ItemStack(Items.GLOWSTONE_DUST), accent = ButtonAccents.INFO))
        addPanel(SidePanel(Component.translatable("gui.femtocraft.host.body.tab"), Component.translatable("gui.femtocraft.host.body.title"),
            TitledPanel(Component.translatable("gui.femtocraft.host.body.title"), bodyPanel()), icon = ItemStack(Items.GOLDEN_APPLE), accent = ButtonAccents.INFO))
    }

    private fun bodyPanel(): ScreenComponent {
        fun stats() = player()?.let(HostStats::of) ?: HostStats.BASE
        val rows = ArrayList<ScreenComponent>()
        rows += StatRow(Component.translatable("gui.femtocraft.host.body.archive"), {
            val p = player()
            if (p == null || !NaniteHost.isHost(p)) Component.translatable("gui.femtocraft.host.not_host.short")
            else Component.literal("${NaniteHost.archiveNanites(p)} / ${stats().regenCap}")
        }, PANEL_W)
        rows += StatRow(Component.translatable("gui.femtocraft.host.body.regen"), { Component.translatable("gui.femtocraft.host.body.seconds", "%.1f".format(java.util.Locale.ROOT, stats().regenInterval / 20.0)) }, PANEL_W)
        rows += StatRow(Component.translatable("gui.femtocraft.host.body.hunger"), { Component.literal("%.2f".format(java.util.Locale.ROOT, stats().regenExhaustion / 4.0)) }, PANEL_W)
        rows += StatRow(Component.translatable("gui.femtocraft.host.body.reach"), { Component.literal("+" + TalentStats.HOST_REACH.format(stats().reachBonus)) }, PANEL_W)
        rows += StatRow(Component.translatable("gui.femtocraft.host.body.tank"), { Component.literal(stats().tankCapacity.toString()) }, PANEL_W)
        rows += StatRow(Component.translatable("gui.femtocraft.host.body.research"), {
            val p = player() ?: return@StatRow Component.literal("-")
            val carried = HostStrains.of(p).talents(NaniteArchetypes.ARCHIVE.id)
            Component.literal(TalentStats.RESEARCH_PER_NANITE.format(talents().stat(TalentStats.RESEARCH_PER_NANITE, carried)))
        }, PANEL_W)
        return Column(rows, gap = 3)
    }

    private fun canIntegrate(): Boolean {
        val p = player() ?: return false
        val id = selected ?: return false
        return HostStrains.refusal(p, id) == null
    }

    private fun send(action: Int, data: Int) {
        if (data < 0) return
        ClientPacketDistributor.sendToServer(MenuActionPayload(menu.containerId, action, data))
    }

    /** The selected archetype's tree for this player. */
    private fun model(): NodeTreeModel {
        val talents = talents()
        val strain = strain()
        val player = player()
        val available = player?.let(HostStrains::available) ?: 0
        return object : NodeTreeModel {
            override val layout = talents.layout(archetype.id)

            override fun look(id: Identifier): NodeLook? {
                val talent = talents[id] ?: return null
                return when (talents.state(id, strain)) {
                    TalentState.UNLOCKED -> NodeLook(talent.iconStack(), NodeState.DONE)
                    TalentState.AVAILABLE -> NodeLook(talent.iconStack(), if (talent.cost <= available) NodeState.AVAILABLE else NodeState.LOCKED, badge = talent.cost.toString())
                    else -> NodeLook(talent.iconStack(), NodeState.LOCKED, badge = talent.cost.toString())
                }
            }

            override fun tooltip(id: Identifier): List<Component> = talentLines(talents, strain, id, available)
        }
    }

    /** A talent's name, description, effects, cost or state, and missing prerequisites. */
    private fun talentLines(talents: Talents, strain: HostStrain, id: Identifier, available: Int): List<Component> {
        val talent = talents[id] ?: return emptyList()
        val state = talents.state(id, strain)
        val lines = ArrayList<Component>()
        lines += talent.displayName(id).copy().withStyle(when (state) {
            TalentState.UNLOCKED -> ChatFormatting.GREEN
            TalentState.AVAILABLE -> ChatFormatting.YELLOW
            else -> ChatFormatting.GRAY
        })
        lines += talent.displayDescription(id).copy().withStyle(ChatFormatting.GRAY)
        for ((statId, amount) in talent.effects) {
            val stat = TalentStats[statId] ?: continue
            lines += Component.translatable("gui.femtocraft.host.effect", stat.displayName, stat.formatEffect(amount))
                .withStyle(if (stat.scope == TalentStat.Scope.NANITE) ChatFormatting.AQUA else ChatFormatting.BLUE)
        }
        when (state) {
            TalentState.UNLOCKED -> lines += Component.translatable("gui.femtocraft.host.unlocked").withStyle(ChatFormatting.GREEN)
            else -> lines += Component.translatable("gui.femtocraft.host.cost", talent.cost, available)
                .withStyle(if (talent.cost <= available) ChatFormatting.YELLOW else ChatFormatting.RED)
        }
        val missing = talent.prerequisites.filter { it !in strain.talents(talent.archetype) }
        if (state == TalentState.LOCKED && missing.isNotEmpty()) {
            lines += Component.translatable("gui.femtocraft.host.requires").withStyle(ChatFormatting.RED)
            for (p in missing) lines += Component.literal("  ").append(talents[p]?.displayName(p) ?: Component.literal(p.toString())).withStyle(ChatFormatting.RED)
        }
        return lines
    }

    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        val player = player() ?: return
        val points = Component.translatable("gui.femtocraft.host.points", HostStrains.available(player), HostStrains.earned(player))
        graphics.text(font, points, leftPos + WIDTH - 8 - font.width(points), topPos + 6, TEXT, false)
        val talents = talents()
        val id = selected
        val chosen = id?.let(talents::get)
        val detailW = WIDTH - 16
        when {
            !NaniteHost.isHost(player) -> lines(graphics, Component.translatable("gui.femtocraft.host.not_host"), 8, DETAILS_Y, detailW, 2)
            chosen == null -> lines(graphics, archetype.description, 8, DETAILS_Y, detailW, 2)
            else -> {
                lines(graphics, chosen.displayName(id), 8, DETAILS_Y, detailW, 1)
                val effects = chosen.effects.entries.mapNotNull { (s, a) -> TalentStats[s]?.let { "${it.displayName.string} ${it.formatEffect(a)}" } }.joinToString(", ")
                lines(graphics, Component.literal(effects).withStyle(ChatFormatting.AQUA), 8, DETAILS_Y + 10, detailW, 1)
                val cost = if (talents.state(id, strain()) == TalentState.UNLOCKED) Component.translatable("gui.femtocraft.host.unlocked").withStyle(ChatFormatting.GREEN)
                else Component.translatable("gui.femtocraft.host.cost.short", chosen.cost).withStyle(if (chosen.cost <= HostStrains.available(player)) ChatFormatting.YELLOW else ChatFormatting.RED)
                lines(graphics, cost, 8, BUTTONS_Y + 3, WIDTH - 16 - 136, 1)
            }
        }
    }

    /** [text] wrapped to [width], at most [max] lines. */
    private fun lines(graphics: GuiGraphicsExtractor, text: Component, x: Int, y: Int, width: Int, max: Int) {
        font.split(text, width).take(max).forEachIndexed { i, line -> graphics.text(font, line, leftPos + x, topPos + y + i * 10, TEXT, false) }
    }

    override fun extractLabels(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        graphics.text(font, title, titleLabelX, titleLabelY, TEXT, false)
    }

    companion object {
        const val WIDTH = 256
        const val TABS_Y = 18
        const val TREE_Y = TABS_Y + ArchetypeTabs.H + 4
        const val TREE_H = 112
        const val DETAILS_Y = TREE_Y + TREE_H + 5
        const val BUTTONS_Y = DETAILS_Y + 22
        const val HEIGHT = BUTTONS_Y + 14 + 6
        const val PANEL_W = 150
    }
}

/**
 * One tab per archetype ([NaniteArchetypes.ALL]): a swatch of its colour and its short name, the selected one raised;
 * a tooltip names it, describes it and counts its talents unlocked ([counts]: unlocked to total).
 */
class ArchetypeTabs(
    private val selected: () -> NaniteArchetype,
    private val onSelect: (NaniteArchetype) -> Unit,
    private val counts: (NaniteArchetype) -> Pair<Int, Int>,
) : ScreenComponent(NaniteArchetypes.ALL.size * (W + GAP) - GAP, H) {
    private fun at(mx: Double, my: Double): NaniteArchetype? {
        if (my < y || my >= y + H) return null
        val i = ((mx - x) / (W + GAP)).toInt()
        if (mx < x || i !in NaniteArchetypes.ALL.indices || mx - x - i * (W + GAP) >= W) return null
        return NaniteArchetypes.ALL[i]
    }

    override fun extract(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, host: ComponentHost) {
        val font = host.hostFont
        val hovered = at(mouseX.toDouble(), mouseY.toDouble())
        NaniteArchetypes.ALL.forEachIndexed { i, a ->
            val tx = x + i * (W + GAP)
            val state = when {
                a == selected() -> ScreenStyle.ButtonState.SELECTED
                a == hovered -> ScreenStyle.ButtonState.HOVERED
                else -> ScreenStyle.ButtonState.IDLE
            }
            ScreenStyle.button(graphics, tx, y, W, H, state, null)
            graphics.fill(tx + 3, y + 4, tx + 8, y + 9, a.color or (0xFF shl 24))
            val label = Component.translatable("nanite.femtocraft.archetype.${a.id.lowercase()}.short")
            graphics.text(font, label, tx + 10, y + 3, ScreenStyle.TEXT, false)
        }
        if (hovered != null) {
            val (have, total) = counts(hovered)
            graphics.setTooltipForNextFrame(font, listOf(
                hovered.displayName.copy().withColor(hovered.color),
                hovered.description.copy().withStyle(ChatFormatting.GRAY),
                Component.translatable("gui.femtocraft.host.tab.count", have, total).withStyle(ChatFormatting.GRAY),
            ), Optional.empty(), mouseX, mouseY)
        }
    }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val a = at(event.x(), event.y()) ?: return false
        Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1f))
        onSelect(a)
        return true
    }

    companion object {
        const val W = 32
        const val H = 13
        const val GAP = 2
    }
}

/**
 * The nanites a host carries: their tank's fill, then one line per kind of nanite (archetype colour, strain and
 * version, how many talents they carry, amount), with the talents listed on hover.
 */
class NanitesPanel(private val player: () -> Player?, private val talents: () -> Talents) : ScreenComponent(HostScreen.PANEL_W, 10 + ROWS * LINE) {
    override fun extract(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, host: ComponentHost) {
        val font = host.hostFont
        val p = player() ?: return
        val contents = PlayerNanites.tank(p).contents()
        val capacity = HostStats.of(p).tankCapacity
        graphics.text(font, Component.translatable("gui.femtocraft.host.nanites.tank", contents.sumOf { it.amount }, capacity), x, y, ScreenStyle.TEXT, false)
        if (contents.isEmpty()) {
            graphics.text(font, Component.translatable("gui.femtocraft.host.nanites.none"), x, y + 12, ScreenStyle.TEXT_MUTED, false)
            return
        }
        contents.take(ROWS).forEachIndexed { i, stack ->
            val ly = y + 12 + i * LINE
            val color = NaniteArchetypes.byId(stack.archetype)?.color ?: 0x808080
            graphics.fill(x, ly + 1, x + 6, ly + 7, color or (0xFF shl 24))
            val name = "${stack.strain} v${stack.version.major}.${stack.version.minor}"
            graphics.text(font, name, x + 9, ly, ScreenStyle.TEXT, false)
            val amount = stack.amount.toString()
            graphics.text(font, amount, x + width - font.width(amount), ly, ScreenStyle.TEXT, false)
            if (mouseX >= x && mouseX < x + width && mouseY >= ly && mouseY < ly + LINE) graphics.setTooltipForNextFrame(font, tooltip(stack), Optional.empty(), mouseX, mouseY)
        }
        if (contents.size > ROWS) graphics.text(font, Component.translatable("gui.femtocraft.host.nanites.more", contents.size - ROWS), x, y + 12 + ROWS * LINE, ScreenStyle.TEXT_MUTED, false)
    }

    private fun tooltip(stack: NaniteStack): List<Component> {
        val talents = talents()
        val lines = ArrayList<Component>()
        lines += Component.literal("${stack.amount} × ").append(NaniteArchetypes.byId(stack.archetype)?.displayName ?: Component.literal(stack.archetype))
        lines += Component.translatable("gui.femtocraft.host.nanites.strain", stack.strain, "${stack.version.major}.${stack.version.minor}").withStyle(ChatFormatting.GRAY)
        if (stack.talents.isEmpty()) lines += Component.translatable("gui.femtocraft.host.nanites.no_talents").withStyle(ChatFormatting.DARK_GRAY)
        for (t in stack.talents) lines += Component.literal("  ").append(talents[t]?.displayName(t) ?: Component.literal(t.toString())).withStyle(ChatFormatting.AQUA)
        return lines
    }

    companion object {
        const val ROWS = 8
        const val LINE = 10
    }
}
