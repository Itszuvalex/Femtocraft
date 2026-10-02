package com.itszuvalex.femtocraft.archive

import com.itszuvalex.itszulib.menu.MenuCore
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.level.Level

/**
 * The Archive Codex: Femtocraft's tech tree anywhere. Shows what is researched, the team's queue and focus, and every
 * Archive of the team; clicking a technology queues it, right-clicking takes it off the queue ([ArchiveResearch]).
 */
class CodexItem(properties: Properties) : Item(properties) {
    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResult {
        if (player is ServerPlayer) open(player)
        return InteractionResult.SUCCESS
    }

    companion object {
        /** Opens the Codex screen for [player], with or without the item. */
        fun open(player: ServerPlayer) {
            player.openMenu(SimpleMenuProvider({ id, inventory, _ -> CodexMenu(id, inventory) }, Component.translatable("item.femtocraft.codex")))
        }
    }
}

/**
 * The Codex's menu: no slots; syncs the team's Archives and takes the research queue actions. The screen reads the
 * synced technologies and team research.
 */
class CodexMenu(containerId: Int, inventory: Inventory) : MenuCore(ArchiveContent.CODEX_MENU.get(), containerId, inventory.player) {
    /** Client copy. */
    var archives: List<ArchiveRecord> = emptyList()
        private set

    init {
        addSync(ArchiveResearch.archivesSync(inventory.player) { archives = it })
    }

    override fun stillValid(player: Player): Boolean = true

    override fun handleAction(player: Player, action: Int, data: Int): Boolean = ArchiveResearch.handleAction(player, action, data)
}
