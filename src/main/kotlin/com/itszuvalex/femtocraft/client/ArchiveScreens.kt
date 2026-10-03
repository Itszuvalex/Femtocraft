package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.archive.ArchiveContent
import com.itszuvalex.femtocraft.archive.ArchiveMenu
import com.itszuvalex.femtocraft.archive.ArchiveRecord
import com.itszuvalex.femtocraft.archive.ArchiveResearch
import com.itszuvalex.femtocraft.archive.CodexMenu
import com.itszuvalex.femtocraft.archive.NaniteHost
import com.itszuvalex.itszulib.client.screen.ButtonAccents
import com.itszuvalex.itszulib.client.screen.TechTreeView
import com.itszuvalex.itszulib.client.screen.ThemedButton
import com.itszuvalex.itszulib.menu.MenuActionPayload
import com.itszuvalex.itszulib.research.TechTree
import com.itszuvalex.itszulib.research.Technologies
import com.itszuvalex.itszulib.team.Research
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import net.neoforged.neoforge.client.network.ClientPacketDistributor

/**
 * A screen with Femtocraft's tech tree that edits the team's research queue (click a technology to queue it after its
 * missing prerequisites, right-click to take it off) and shows the team's focus and queue below the tree.
 */
abstract class ResearchScreen<M : AbstractContainerMenu>(menu: M, inventory: Inventory, title: Component, height: Int) :
    FemtoScreen<M>(menu, inventory, title, WIDTH, height) {
    override fun addComponents() {
        addComponent(TechTreeView(WIDTH - 16, TREE_HEIGHT, ArchiveContent.TREE, selected = ::focus, onSelect = { send(ArchiveResearch.ACTION_QUEUE, it) }, onAlternate = { send(ArchiveResearch.ACTION_UNQUEUE, it) }), 8, 18)
    }

    private var offer: ThemedButton? = null

    override fun init() {
        super.init()
        offer = addRenderableWidget(ThemedButton(
            leftPos + WIDTH - 8 - OFFER_W, topPos + 4, OFFER_W, 12, Component.translatable("gui.femtocraft.research.offer"),
            { ClientPacketDistributor.sendToServer(MenuActionPayload(menu.containerId, ArchiveResearch.ACTION_DELIVER, 0)) },
            Component.translatable("gui.femtocraft.research.offer.tooltip"), accent = ButtonAccents.UPGRADE,
        ))
    }

    /** The offer button is live while the focus still needs items. */
    override fun containerTick() {
        super.containerTick()
        offer?.active = focus()?.let { f -> technologies().remaining(f, research())?.items?.any { it.second > 0 } } == true
    }

    private fun send(action: Int, technology: Identifier) {
        val access = minecraft.level?.registryAccess() ?: return
        val id = ArchiveResearch.networkId(access, technology)
        if (id >= 0) ClientPacketDistributor.sendToServer(MenuActionPayload(menu.containerId, action, id))
    }

    protected fun technologies(): Technologies = minecraft.level?.registryAccess()?.let(TechTree::of) ?: Technologies.EMPTY

    protected fun research(): Research = minecraft.player?.let(TechTree::research) ?: Research.EMPTY

    protected fun focus(): Identifier? = technologies().focus(ArchiveContent.TREE, research())

    protected fun name(id: Identifier): Component = technologies()[id]?.displayName(id) ?: Component.literal(id.toString())

    /** [line] cut to the screen's inner width. */
    protected fun line(graphics: GuiGraphicsExtractor, line: Component, x: Int, y: Int) {
        val cut = font.split(line, WIDTH - 8 - x).firstOrNull() ?: return
        graphics.text(font, cut, leftPos + x, topPos + y, TEXT, false)
    }

    /**
     * The focus with its progress, then what is queued after it. @return The y below.
     */
    protected fun focusAndQueue(graphics: GuiGraphicsExtractor, y: Int): Int {
        val focus = focus()
        val research = research()
        if (focus == null) {
            line(graphics, Component.translatable(if (research.queue.isEmpty()) "gui.femtocraft.research.nothing_queued" else "gui.femtocraft.research.blocked"), 8, y)
        } else {
            line(graphics, Component.translatable("gui.femtocraft.research.focus", name(focus)), 8, y)
            val cost = technologies()[focus]?.cost ?: 0L
            if (cost > 0) progress(graphics, 8, y + 12, WIDTH - 16, 3, research.progressOf(focus).toDouble() / cost)
        }
        val next = research.queue.filter { it != focus && technologies()[it]?.tree == ArchiveContent.TREE }
        if (next.isNotEmpty()) {
            val names = Component.empty()
            next.forEachIndexed { i, id -> if (i > 0) names.append(", "); names.append(name(id)) }
            line(graphics, Component.translatable("gui.femtocraft.research.next", names), 8, y + 18)
        }
        return y + 30
    }

    /**
     * Up to [rows] of the team's Archives, each with where it is and what it is doing. @return The y below.
     */
    protected fun archives(graphics: GuiGraphicsExtractor, archives: List<ArchiveRecord>, y: Int, rows: Int): Int {
        line(graphics, Component.translatable("gui.femtocraft.research.archives", archives.size), 8, y)
        var at = y + 12
        for (record in archives.take(rows)) {
            val pos = record.pos.pos()
            val where = "${record.pos.dimension().identifier().path} ${pos.x}, ${pos.y}, ${pos.z}"
            line(graphics, Component.literal("  $where: ").append(Component.translatable("gui.femtocraft.archive.status.${record.status.name.lowercase()}")), 8, at)
            at += 11
        }
        if (archives.size > rows) {
            line(graphics, Component.translatable("gui.femtocraft.research.more", archives.size - rows), 8, at)
            at += 11
        }
        return at
    }

    override fun extractLabels(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        graphics.text(font, title, titleLabelX, titleLabelY, TEXT, false)
    }

    companion object {
        const val OFFER_W = 64
        const val WIDTH = 256
        const val TREE_HEIGHT = 136
        const val BELOW_TREE = 18 + TREE_HEIGHT + 6
    }
}

/**
 * The Archive: the tech tree and the team's queue, what this Archive is doing, the player's own Archive nanites and the
 * computed points waiting (from Archive Interfaces).
 */
class ArchiveScreen(menu: ArchiveMenu, inventory: Inventory, title: Component) : ResearchScreen<ArchiveMenu>(menu, inventory, title, HEIGHT) {
    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        val player = minecraft.player ?: return
        val y = focusAndQueue(graphics, BELOW_TREE)
        // gui.femtocraft.archive.status.<ArchiveStatus, lower case>
        line(graphics, Component.translatable("gui.femtocraft.archive.this", Component.translatable("gui.femtocraft.archive.status.${menu.status.name.lowercase()}")), 8, y)
        val own = if (NaniteHost.isHost(player)) Component.translatable("gui.femtocraft.archive.host", NaniteHost.archiveNanites(player), NaniteHost.REGEN_CAP)
        else Component.translatable("gui.femtocraft.archive.not_host")
        line(graphics, own, 8, y + 12)
        line(graphics, Component.translatable("gui.femtocraft.archive.computed", menu.computedPoints), 8, y + 24)
    }

    companion object {
        const val HEIGHT = BELOW_TREE + 30 + 36 + 4
    }
}

/**
 * The Archive Codex: the tech tree and the team's queue, and every Archive of the team.
 */
class CodexScreen(menu: CodexMenu, inventory: Inventory, title: Component) : ResearchScreen<CodexMenu>(menu, inventory, title, HEIGHT) {
    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        archives(graphics, menu.archives, focusAndQueue(graphics, BELOW_TREE), ROWS)
    }

    companion object {
        const val ROWS = 3
        const val HEIGHT = BELOW_TREE + 30 + 12 + (ROWS + 1) * 11 + 2
    }
}
