package com.itszuvalex.femtocraft.core

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.industry.ConfiguratorItem
import com.itszuvalex.femtocraft.industry.ConfiguratorMode
import com.itszuvalex.itszulib.menu.BlockMenus
import com.itszuvalex.itszulib.menu.MenuCore
import com.itszuvalex.itszulib.menu.SideConfigCyclers
import com.itszuvalex.itszulib.menu.SideConfigMode
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.level.block.entity.BlockEntity

/**
 * Base for Femtocraft's block menus: an ItszuLib [MenuCore] (vanilla slots, shift-click, typed syncs; DECISIONS D6)
 * over a block entity, valid while the player is in reach of it. Port of v3's `ContainerInv` subclasses.
 *
 * Every menu offers side configuration ([MenuCore.enableSideConfig]) for the configurator's modes the block entity has
 * ([sideConfigModes]), so screens show ItszuLib's 3D side configuration panel.
 *
 * @param blockEntity Null only if the client could not find the block entity the server named.
 */
abstract class FemtoMenu<T : BlockEntity>(type: MenuType<*>, containerId: Int, inventory: Inventory, @JvmField val blockEntity: T?) :
    MenuCore(type, containerId, inventory.player) {

    init {
        enableSideConfig(blockEntity, sideConfigModes())
    }

    override fun stillValid(player: Player): Boolean = BlockMenus.stillValid(blockEntity, player)

    companion object {
        /**
         * The configurator's modes ([ConfiguratorItem.MODULES], including the nanite area's), cycled as the
         * configurator cycles a face (IO, then storage on wrap).
         */
        fun sideConfigModes(): List<SideConfigMode> = ConfiguratorMode.entries.mapNotNull { mode ->
            val module = ConfiguratorItem.MODULES[mode] ?: return@mapNotNull null
            val name = mode.name.lowercase()
            SideConfigMode(Identifier.fromNamespaceAndPath(Femtocraft.ID, name), Component.translatable("tooltip.femtocraft.configurator.$name"), module, SideConfigCyclers.IO_THEN_STORAGE)
        }
    }
}
