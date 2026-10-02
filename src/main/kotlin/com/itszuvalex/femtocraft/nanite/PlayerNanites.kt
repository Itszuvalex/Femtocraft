package com.itszuvalex.femtocraft.nanite

import com.itszuvalex.femtocraft.FemtoRegistries
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.world.entity.player.Player
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.registries.DeferredHolder

/**
 * The player's own nanite tank (100 nanites), a synced data attachment. Port of v3's `PlayerNaniteCapability`; the
 * HUD overlay (`PlayerNaniteCapabilitiesOverlay`) is client rendering, follow-up work. As in v3 the nanites are lost on
 * death.
 */
object PlayerNanites {
    const val CAPACITY = 100

    @JvmField
    val ATTACHMENT: DeferredHolder<AttachmentType<*>, AttachmentType<List<NaniteStack>>> = FemtoRegistries.ATTACHMENT_TYPES.register("player_nanites") { ->
        AttachmentType.builder<List<NaniteStack>> { -> listOf() }
            .serialize(NaniteTank.LIST.fieldOf("tank"))
            .sync(NaniteStack.STREAM_CODEC.apply(ByteBufCodecs.list()))
            .build()
    }

    /**
     * A working copy of the player's tank. Call [save] after changing it (that also syncs it to the player).
     */
    fun tank(player: Player): NaniteTank = NaniteTank(CAPACITY).also { it.setContents(player.getData(ATTACHMENT.get())) }

    fun save(player: Player, tank: NaniteTank) {
        player.setData(ATTACHMENT.get(), tank.contents())
    }

    /**
     * Moves one nanite of each strain from the player into [tank] (v3's `MessageFillNanite`).
     *
     * @return Nanites moved.
     */
    fun fill(player: Player, tank: INaniteTank): Int {
        val own = tank(player)
        val moved = own.contents().sumOf { FragNaniteAutoIO.move(singleStrain(own, it), tank, 1) }
        if (moved > 0) save(player, own)
        return moved
    }

    /**
     * Moves one nanite of each strain from [tank] to the player (v3's `MessageDrainNanite`).
     */
    fun drain(player: Player, tank: INaniteTank): Int {
        val own = tank(player)
        val moved = tank.contents().sumOf { FragNaniteAutoIO.move(singleStrain(tank, it), own, 1) }
        if (moved > 0) save(player, own)
        return moved
    }

    /**
     * A view of [tank] that only offers [stack]'s strain.
     */
    private fun singleStrain(tank: INaniteTank, stack: NaniteStack): INaniteTank = object : INaniteTank by tank {
        override fun contents(): List<NaniteStack> = tank.contents().filter { it.isSameNanite(stack) }
    }
}
