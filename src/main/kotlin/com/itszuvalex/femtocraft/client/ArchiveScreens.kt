package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.archive.ArchiveContent
import com.itszuvalex.femtocraft.archive.ArchiveMenu
import com.itszuvalex.femtocraft.archive.CodexMenu
import com.itszuvalex.femtocraft.archive.NaniteHost
import com.itszuvalex.itszulib.client.screen.TechTreeView
import com.itszuvalex.itszulib.menu.MenuActionPayload
import com.itszuvalex.itszulib.research.TechTree
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.neoforged.neoforge.client.network.ClientPacketDistributor

/**
 * The Archive: Femtocraft's tech tree (click an available technology to research it), what is being researched and
 * how, and the player's own Archive nanites.
 */
class ArchiveScreen(menu: ArchiveMenu, inventory: Inventory, title: Component) : FemtoScreen<ArchiveMenu>(menu, inventory, title, WIDTH, HEIGHT) {
    override fun addComponents() {
        addComponent(TechTreeView(WIDTH - 16, TREE_HEIGHT, ArchiveContent.TREE, selected = { menu.technology }, onSelect = ::choose), 8, 18)
    }

    private fun choose(technology: net.minecraft.resources.Identifier) {
        val access = minecraft.level?.registryAccess() ?: return
        val id = ArchiveMenu.networkId(access, technology)
        if (id >= 0) ClientPacketDistributor.sendToServer(MenuActionPayload(menu.containerId, ArchiveMenu.ACTION_CHOOSE, id))
    }

    override fun extractContents(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        val y = 18 + TREE_HEIGHT + 6
        val player = minecraft.player ?: return
        val tech = menu.technology
        val techs = TechTree.of(player.level().registryAccess())
        val line = if (tech == null) Component.translatable("gui.femtocraft.archive.choose") else {
            val name = techs[tech]?.displayName(tech) ?: Component.literal(tech.toString())
            Component.translatable("gui.femtocraft.archive.researching", name)
        }
        text(graphics, line, 8, y)
        if (tech != null) {
            val cost = techs[tech]?.cost ?: 0L
            val progress = TechTree.research(player).progressOf(tech)
            if (cost > 0) progress(graphics, 8, y + 12, WIDTH - 16, 4, progress.toDouble() / cost)
        }
        // gui.femtocraft.archive.status.<ArchiveStatus, lower case>
        text(graphics, Component.translatable("gui.femtocraft.archive.status.${menu.status.name.lowercase()}"), 8, y + 20)
        val own = if (NaniteHost.isHost(player)) Component.translatable("gui.femtocraft.archive.host", NaniteHost.archiveNanites(player), NaniteHost.REGEN_CAP)
        else Component.translatable("gui.femtocraft.archive.not_host")
        text(graphics, own, 8, y + 32)
    }

    override fun extractLabels(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        graphics.text(font, title, titleLabelX, titleLabelY, TEXT, false)
    }

    companion object {
        const val WIDTH = 256
        const val HEIGHT = 210
        const val TREE_HEIGHT = 136
    }
}

/**
 * The Archive Codex: Femtocraft's tech tree, read-only, with the player's team's progress in the tooltips.
 */
class CodexScreen(menu: CodexMenu, inventory: Inventory, title: Component) : FemtoScreen<CodexMenu>(menu, inventory, title, ArchiveScreen.WIDTH, HEIGHT) {
    override fun addComponents() {
        addComponent(TechTreeView(ArchiveScreen.WIDTH - 16, HEIGHT - 26, ArchiveContent.TREE), 8, 18)
    }

    override fun extractLabels(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        graphics.text(font, title, titleLabelX, titleLabelY, TEXT, false)
    }

    companion object {
        const val HEIGHT = 186
    }
}
