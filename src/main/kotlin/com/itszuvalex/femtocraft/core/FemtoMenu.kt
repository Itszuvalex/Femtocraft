package com.itszuvalex.femtocraft.core

import com.itszuvalex.femtocraft.industry.ConfiguratorItem
import com.itszuvalex.femtocraft.industry.ConfiguratorMode
import com.itszuvalex.itszulib.api.adapters.IBattery
import com.itszuvalex.itszulib.api.adapters.IBlockEntity
import com.itszuvalex.itszulib.core.BlockEntityCore
import com.itszuvalex.itszulib.menu.BlockMenus
import com.itszuvalex.itszulib.menu.MenuCore
import com.itszuvalex.itszulib.menu.MenuSyncs
import net.minecraft.core.Direction
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
     * Handles [ACTION_CONFIGURE] (the side configuration panel, [configureData]) and passes every other action to
     * [handleScreenAction].
     */
    final override fun handleAction(player: Player, action: Int, data: Int): Boolean {
        if (action != ACTION_CONFIGURE) return handleScreenAction(player, action, data)
        val be = blockEntity as? IBlockEntity ?: return false
        val face = data and 7
        if (face >= Direction.entries.size) return false
        val mode = ConfiguratorMode.entries.getOrNull((data shr 3) and 15) ?: return false
        val config = ConfiguratorItem.configModule(mode)?.let { be.getModule(it, null) } ?: return false
        ConfiguratorItem.cycle(config, Direction.from3DDataValue(face), data and BACKWARD != 0)
        (be as? BlockEntityCore)?.markDirtyAndSync()
        return true
    }

    /**
     * Server side: a control of this menu's own screen was used (see [handleAction]).
     */
    protected open fun handleScreenAction(player: Player, action: Int, data: Int): Boolean = false

    /**
     * Syncs a battery's charge and capacity into [view] (the client copy, read by the screen).
     */
    protected fun syncBattery(battery: () -> IBattery, view: BatteryView) {
        addSync(MenuSyncs.double({ battery().storage() }, { view.storage = it }))
        addSync(MenuSyncs.double({ battery().maxStorage() }, { view.max = it }))
    }

    companion object {
        /**
         * Cycles one face of a sided configuration as the configurator does; data from [configureData].
         */
        const val ACTION_CONFIGURE = 100

        private const val BACKWARD = 1 shl 7

        fun configureData(face: Direction, mode: ConfiguratorMode, backward: Boolean): Int =
            face.get3DDataValue() or (mode.ordinal shl 3) or (if (backward) BACKWARD else 0)

        /**
         * The configurator modes [be] has a sided configuration for, in [ConfiguratorMode] order.
         */
        fun configurationModes(be: BlockEntity?): List<ConfiguratorMode> {
            val provider = be as? IBlockEntity ?: return emptyList()
            return ConfiguratorMode.entries.filter { mode -> ConfiguratorItem.configModule(mode)?.let { provider.getModule(it, null) } != null }
        }
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
