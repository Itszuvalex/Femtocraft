package com.itszuvalex.femtocraft.host

import com.itszuvalex.femtocraft.FemtoRegistries
import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.core.FemtoNetwork
import com.itszuvalex.itszulib.menu.MenuCore
import net.minecraft.core.RegistryAccess
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.inventory.MenuType
import net.neoforged.bus.api.IEventBus

/**
 * The host screen's menu: no slots. The screen reads the player's synced attachments (host, tank, strain) and the
 * synced talents directly; this menu only carries its actions: [ACTION_UNLOCK] a talent (data: its network id,
 * [networkId]) and [ACTION_RESET] an archetype's tree (data: its index in [NaniteArchetypes.ALL]).
 */
class HostMenu(containerId: Int, inventory: Inventory) : MenuCore(HostContent.MENU.get(), containerId, inventory.player) {
    override fun stillValid(player: Player): Boolean = true

    override fun handleAction(player: Player, action: Int, data: Int): Boolean = when (action) {
        ACTION_UNLOCK -> byNetworkId(player.level().registryAccess(), data)?.let { HostStrains.unlock(player, it) == null } == true
        ACTION_RESET -> NaniteArchetypes.ALL.getOrNull(data)?.let { HostStrains.reset(player, it.id) } == true
        else -> false
    }

    companion object {
        const val ACTION_UNLOCK = 0
        const val ACTION_RESET = 1

        /** The screen id clients ask for ([FemtoNetwork.screen]). */
        @JvmField
        val SCREEN: Identifier = Identifier.fromNamespaceAndPath(Femtocraft.ID, "host")

        /** [talent]'s id in the synced talent registry (the same on both sides), or -1. */
        fun networkId(access: RegistryAccess, talent: Identifier?): Int {
            val registry = access.lookup(Talents.KEY).orElse(null) ?: return -1
            return talent?.let(registry::getValue)?.let(registry::getId) ?: -1
        }

        fun byNetworkId(access: RegistryAccess, id: Int): Identifier? {
            if (id < 0) return null
            val registry = access.lookup(Talents.KEY).orElse(null) ?: return null
            return registry.byId(id)?.let(registry::getKey)
        }
    }
}

/**
 * The host framework's registrations: the archetypes, the talent registry, the strain attachment, the host menu and
 * its screen request (any player may open it; one who is not a host yet sees how to become one).
 */
object HostContent {
    @JvmField
    val MENU = FemtoRegistries.MENUS.register("host") { -> MenuType(::HostMenu, FeatureFlags.VANILLA_SET) }

    fun init(modBus: IEventBus) {
        NaniteArchetypes.init()
        Talents.register(modBus)
        HostStrains.ATTACHMENT
        FemtoNetwork.screen(HostMenu.SCREEN) { player ->
            player.openMenu(SimpleMenuProvider({ id, inventory, _ -> HostMenu(id, inventory) }, Component.translatable("gui.femtocraft.host.title")))
        }
    }
}
