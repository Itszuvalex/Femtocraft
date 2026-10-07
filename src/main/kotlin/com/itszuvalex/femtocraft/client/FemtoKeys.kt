package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.core.OpenScreenPayload
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.resources.Identifier
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent
import net.neoforged.neoforge.client.network.ClientPacketDistributor
import net.neoforged.neoforge.client.settings.KeyConflictContext
import net.neoforged.neoforge.common.NeoForge

/**
 * Femtocraft's key bindings, under their own category in the controls screen (`key.category.femtocraft.femtocraft`).
 * Features add a key with [key] (or [screenKey] for one that opens a screen through
 * [com.itszuvalex.femtocraft.core.FemtoNetwork.screen]) during client setup; presses are handled once per client tick,
 * in game only (not while a screen is open).
 */
object FemtoKeys {
    @JvmField
    val CATEGORY = KeyMapping.Category(Identifier.fromNamespaceAndPath(Femtocraft.ID, Femtocraft.ID))

    private val keys = ArrayList<Pair<KeyMapping, () -> Unit>>()

    /** The host screen: strains, talents, nanites carried ([com.itszuvalex.femtocraft.host.HostMenu]). */
    @JvmField
    val HOST = screenKey("host", InputConstants.KEY_N, com.itszuvalex.femtocraft.host.HostMenu.SCREEN)

    /**
     * A key named `key.femtocraft.<name>`, bound to [defaultKey] (a GLFW key code), running [onPress] for each press.
     */
    @JvmStatic
    fun key(name: String, defaultKey: Int, onPress: () -> Unit): KeyMapping {
        val mapping = KeyMapping("key.${Femtocraft.ID}.$name", KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, defaultKey, CATEGORY)
        keys += mapping to onPress
        return mapping
    }

    /** A key that asks the server to open screen [screen]. */
    @JvmStatic
    fun screenKey(name: String, defaultKey: Int, screen: Identifier): KeyMapping =
        key(name, defaultKey) { ClientPacketDistributor.sendToServer(OpenScreenPayload(screen)) }

    fun register(modBus: IEventBus) {
        modBus.addListener { event: RegisterKeyMappingsEvent ->
            event.registerCategory(CATEGORY)
            keys.forEach { event.register(it.first) }
        }
        NeoForge.EVENT_BUS.addListener { _: ClientTickEvent.Post ->
            if (Minecraft.getInstance().player == null) return@addListener
            for ((mapping, onPress) in keys) while (mapping.consumeClick()) onPress()
        }
    }
}
