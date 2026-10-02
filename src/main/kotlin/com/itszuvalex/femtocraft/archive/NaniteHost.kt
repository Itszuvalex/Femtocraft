package com.itszuvalex.femtocraft.archive

import com.itszuvalex.femtocraft.FemtoRegistries
import com.itszuvalex.femtocraft.nanite.NaniteRegistry
import com.itszuvalex.femtocraft.nanite.NaniteStack
import com.itszuvalex.femtocraft.nanite.PlayerNanites
import com.mojang.serialization.Codec
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.tick.PlayerTickEvent
import net.neoforged.neoforge.registries.DeferredHolder

/**
 * The player as a nanite host (2016 "Back to Magic", 2018 opening): a player who touches a rift's crystal cluster is
 * joined by the stranded Archive ("WE ARE THE ARCHIVE", Draft 1, 2015) and from then on carries its tier 0 Archive
 * nanites in their own tank ([PlayerNanites]). A host's body breeds them back slowly, paid for in hunger, up to
 * [REGEN_CAP]; machines such as the Archive draw them from a host standing nearby.
 *
 * Being a host is permanent (kept on death); the nanites in the tank are lost on death, as before. The numbers are
 * first guesses, to tune in play.
 */
object NaniteHost {
    /** Archive nanites a host's body keeps itself stocked with. */
    const val REGEN_CAP = 10

    /** Ticks between regenerated nanites. */
    const val REGEN_INTERVAL = 200

    /** Hunger cost of one regenerated nanite (4 exhaustion is one food point). */
    const val REGEN_EXHAUSTION = 6f

    /** No regeneration while the food bar is below this. */
    const val REGEN_MIN_FOOD = 7

    @JvmField
    val ATTACHMENT: DeferredHolder<AttachmentType<*>, AttachmentType<Boolean>> = FemtoRegistries.ATTACHMENT_TYPES.register("nanite_host") { ->
        AttachmentType.builder<Boolean> { -> false }
            .serialize(Codec.BOOL.fieldOf("host"))
            .copyOnDeath()
            .sync(net.minecraft.network.codec.ByteBufCodecs.BOOL)
            .build()
    }

    fun init() {
        NeoForge.EVENT_BUS.addListener { event: PlayerTickEvent.Post ->
            val player = event.entity
            if (!player.level().isClientSide && player.tickCount % REGEN_INTERVAL == 0) regenerate(player)
        }
    }

    fun isHost(player: Player): Boolean = player.getData(ATTACHMENT.get())

    /** Archive nanites in [player]'s tank. */
    fun archiveNanites(player: Player): Int = PlayerNanites.tank(player).contents().filter { it.isSameStrain(NaniteRegistry.archive(1)) }.sumOf { it.amount }

    /**
     * First contact: makes [player] a host and stocks them with [REGEN_CAP] Archive nanites. Nothing happens for a
     * player who already is one.
     *
     * @return True if [player] became a host.
     */
    fun contact(player: Player): Boolean {
        if (isHost(player)) return false
        player.setData(ATTACHMENT.get(), true)
        give(player, REGEN_CAP)
        if (player is ServerPlayer) {
            player.connection.send(ClientboundSetTitleTextPacket(Component.translatable("archive.femtocraft.contact.title").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)))
            player.sendSystemMessage(Component.translatable("archive.femtocraft.contact").withStyle(ChatFormatting.AQUA))
        }
        return true
    }

    /**
     * One regeneration step: a fed host below [REGEN_CAP] gains one Archive nanite and pays [REGEN_EXHAUSTION].
     *
     * @return True if a nanite was bred.
     */
    fun regenerate(player: Player): Boolean {
        if (!isHost(player) || archiveNanites(player) >= REGEN_CAP || player.foodData.foodLevel < REGEN_MIN_FOOD) return false
        if (give(player, 1) == 0) return false
        player.causeFoodExhaustion(REGEN_EXHAUSTION)
        return true
    }

    /**
     * Puts up to [amount] Archive nanites into [player]'s tank.
     *
     * @return Nanites added.
     */
    fun give(player: Player, amount: Int): Int {
        val tank = PlayerNanites.tank(player)
        val left = tank.fill(NaniteRegistry.archive(amount), true)
        val added = amount - left.amount
        if (added > 0) PlayerNanites.save(player, tank)
        return added
    }

    /**
     * Takes up to [amount] Archive nanites from [player]'s tank.
     *
     * @return Nanites taken.
     */
    fun draw(player: Player, amount: Int): Int {
        val tank = PlayerNanites.tank(player)
        val drained: NaniteStack = tank.drain(NaniteRegistry.archive(amount), true)
        if (drained.amount > 0) PlayerNanites.save(player, tank)
        return drained.amount
    }

    /**
     * Hosts within [radius] blocks of [center] that carry Archive nanites, nearest first.
     */
    fun nearbyHosts(level: ServerLevel, center: BlockPos, radius: Double): List<Player> =
        level.players()
            .filter { !it.isSpectator && it.distanceToSqr(center.center) <= radius * radius && isHost(it) && archiveNanites(it) > 0 }
            .sortedBy { it.distanceToSqr(center.center) }
}
