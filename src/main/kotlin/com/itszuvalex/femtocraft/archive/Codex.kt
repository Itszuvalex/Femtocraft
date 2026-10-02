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
 * The Archive Codex: opens Femtocraft's tech tree anywhere, read-only (what is researched, what is next, progress).
 * Research is chosen at an Archive.
 */
class CodexItem(properties: Properties) : Item(properties) {
    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResult {
        if (player is ServerPlayer) {
            player.openMenu(SimpleMenuProvider({ id, inventory, _ -> CodexMenu(id, inventory) }, Component.translatable("item.femtocraft.codex")))
        }
        return InteractionResult.SUCCESS
    }
}

/**
 * The Codex's menu: no slots and nothing to sync; the screen reads the synced technologies and team research.
 */
class CodexMenu(containerId: Int, inventory: Inventory) : MenuCore(ArchiveContent.CODEX_MENU.get(), containerId, inventory.player) {
    override fun stillValid(player: Player): Boolean = true
}
