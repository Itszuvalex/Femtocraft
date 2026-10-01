package com.itszuvalex.femtocraft.core

import com.itszuvalex.itszulib.api.adapters.IBattery
import com.itszuvalex.itszulib.menu.BlockMenus
import com.itszuvalex.itszulib.menu.MenuCore
import com.itszuvalex.itszulib.menu.MenuSyncs
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.level.block.entity.BlockEntity

/**
 * Base for Femtocraft's block menus: an ItszuLib [MenuCore] (vanilla slots, shift-click, typed syncs; DECISIONS D6)
 * over a block entity, valid while the player is in reach of it. Port of v3's `ContainerInv` subclasses.
 *
 * @param blockEntity Null only if the client could not find the block entity the server named.
 */
abstract class FemtoMenu<T : BlockEntity>(type: MenuType<*>, containerId: Int, inventory: Inventory, @JvmField val blockEntity: T?) :
    MenuCore(type, containerId, inventory.player) {

    override fun stillValid(player: Player): Boolean = BlockMenus.stillValid(blockEntity, player)

    /**
     * Syncs a battery's charge and capacity into [view] (the client copy, read by the screen).
     */
    protected fun syncBattery(battery: () -> IBattery, view: BatteryView) {
        addSync(MenuSyncs.double({ battery().storage() }, { view.storage = it }))
        addSync(MenuSyncs.double({ battery().maxStorage() }, { view.max = it }))
    }

    /**
     * Client-side copy of a synced battery.
     */
    class BatteryView {
        var storage = 0.0
        var max = 0.0

        val fraction: Double get() = if (max <= 0) 0.0 else (storage / max).coerceIn(0.0, 1.0)
    }
}
