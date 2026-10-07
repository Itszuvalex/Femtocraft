package com.itszuvalex.femtocraft.core

import com.itszuvalex.femtocraft.Femtocraft
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.handling.IPayloadContext
import net.neoforged.neoforge.network.registration.PayloadRegistrar

/**
 * Femtocraft's network channel: features declare their payloads here during mod construction ([toServer],
 * [toClient]) and they are registered together when NeoForge asks ([register]). Handlers run on the main thread.
 * Menus need none of this: ItszuLib's menu syncs and actions cover them.
 *
 * Also a general "open this screen" request ([OpenScreenPayload]): a feature registers who may open what with
 * [screen], and a key ([com.itszuvalex.femtocraft.client.FemtoKeys]) or button sends [requestScreen].
 */
object FemtoNetwork {
    /** Bump when a payload's wire format changes. */
    const val VERSION = "1"

    private val registrations = ArrayList<(PayloadRegistrar) -> Unit>()
    private val screens = LinkedHashMap<Identifier, (ServerPlayer) -> Unit>()

    /** Declares a client-to-server payload, handled by [handler] on the server. Call during mod construction. */
    @JvmStatic
    fun <T : CustomPacketPayload> toServer(type: CustomPacketPayload.Type<T>, codec: StreamCodec<in RegistryFriendlyByteBuf, T>, handler: (T, IPayloadContext) -> Unit) {
        registrations += { it.playToServer(type, codec, handler) }
    }

    /** Declares a server-to-client payload, handled by [handler] on the client. Call during mod construction. */
    @JvmStatic
    fun <T : CustomPacketPayload> toClient(type: CustomPacketPayload.Type<T>, codec: StreamCodec<in RegistryFriendlyByteBuf, T>, handler: (T, IPayloadContext) -> Unit) {
        registrations += { it.playToClient(type, codec, handler) }
    }

    @JvmStatic
    fun sendToPlayer(player: ServerPlayer, payload: CustomPacketPayload) = PacketDistributor.sendToPlayer(player, payload)

    /**
     * Lets clients ask for screen [id]: [open] runs on the server for the asking player (it decides whether to open
     * anything, usually a menu with `openMenu`). Call during mod construction.
     */
    @JvmStatic
    fun screen(id: Identifier, open: (ServerPlayer) -> Unit) {
        require(screens.putIfAbsent(id, open) == null) { "Screen $id registered twice" }
    }

    /** Server side: opens screen [id] for [player], if it is registered. @return False if it is not. */
    @JvmStatic
    fun openScreen(player: ServerPlayer, id: Identifier): Boolean {
        val open = screens[id] ?: return false
        open(player)
        return true
    }

    @JvmStatic
    fun register(modBus: IEventBus) {
        toServer(OpenScreenPayload.TYPE, OpenScreenPayload.STREAM_CODEC) { payload, context -> (context.player() as? ServerPlayer)?.let { openScreen(it, payload.screen) } }
        modBus.addListener { event: RegisterPayloadHandlersEvent ->
            val registrar = event.registrar(VERSION)
            registrations.forEach { it(registrar) }
        }
    }
}

/** A client asking the server to open screen [screen] ([FemtoNetwork.screen]). */
data class OpenScreenPayload(val screen: Identifier) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<OpenScreenPayload> = TYPE

    companion object {
        @JvmField
        val TYPE: CustomPacketPayload.Type<OpenScreenPayload> = CustomPacketPayload.Type(Identifier.fromNamespaceAndPath(Femtocraft.ID, "open_screen"))

        @JvmField
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, OpenScreenPayload> =
            StreamCodec.composite(Identifier.STREAM_CODEC, OpenScreenPayload::screen, ::OpenScreenPayload)
    }
}
