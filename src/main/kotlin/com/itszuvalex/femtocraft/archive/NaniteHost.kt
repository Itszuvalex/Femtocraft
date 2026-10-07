package com.itszuvalex.femtocraft.archive

import com.itszuvalex.femtocraft.FemtoRegistries
import com.itszuvalex.femtocraft.core.FemtoParticles
import com.itszuvalex.femtocraft.host.HostStats
import com.itszuvalex.femtocraft.host.HostStrains
import com.itszuvalex.femtocraft.host.NaniteArchetypes
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
import net.minecraft.world.phys.Vec3
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
 * first guesses, to tune in play. The host's talents change them ([com.itszuvalex.femtocraft.host.HostStats]), and the
 * nanites they breed carry their Archive strain's talents
 * ([com.itszuvalex.femtocraft.host.HostStrains.nanites]).
 */
object NaniteHost {
    /** Archive nanites a host's body keeps itself stocked with, before talents ([HostStats.regenCap]). */
    const val REGEN_CAP = 10

    /** Ticks between regenerated nanites, before talents ([HostStats.regenInterval]). */
    const val REGEN_INTERVAL = HostStats.BASE_REGEN_INTERVAL

    /** Hunger cost of one regenerated nanite before talents (4 exhaustion is one food point). */
    const val REGEN_EXHAUSTION = HostStats.BASE_REGEN_EXHAUSTION

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
            if (!player.level().isClientSide && isHost(player) && player.tickCount % HostStats.of(player).regenInterval == 0) regenerate(player)
        }
    }

    fun isHost(player: Player): Boolean = player.getData(ATTACHMENT.get())

    /** Archive nanites in [player]'s tank. */
    fun archiveNanites(player: Player): Int = PlayerNanites.tank(player).contents().filter { it.isSameStrain(NaniteRegistry.archive(1)) }.sumOf { it.amount }

    /**
     * First contact: makes [player] a host and stocks them with as many Archive nanites as their body keeps
     * ([HostStats.regenCap]). Nothing happens for a player who already is one.
     *
     * @return True if [player] became a host.
     */
    fun contact(player: Player): Boolean {
        if (isHost(player)) return false
        player.setData(ATTACHMENT.get(), true)
        give(player, HostStats.of(player).regenCap)
        if (player is ServerPlayer) {
            player.connection.send(ClientboundSetTitleTextPacket(Component.translatable("archive.femtocraft.contact.title").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)))
            player.sendSystemMessage(Component.translatable("archive.femtocraft.contact").withStyle(ChatFormatting.AQUA))
        }
        return true
    }

    /**
     * One regeneration step: a fed host below their cap ([HostStats.regenCap]) gains one Archive nanite and pays its
     * hunger ([HostStats.regenExhaustion]).
     *
     * @return True if a nanite was bred.
     */
    fun regenerate(player: Player): Boolean {
        if (!isHost(player) || player.foodData.foodLevel < REGEN_MIN_FOOD) return false
        val stats = HostStats.of(player)
        if (archiveNanites(player) >= stats.regenCap) return false
        if (give(player, 1) == 0) return false
        player.causeFoodExhaustion(stats.regenExhaustion)
        return true
    }

    /**
     * Puts up to [amount] Archive nanites into [player]'s tank, bred with their strain's talents.
     *
     * @return Nanites added.
     */
    fun give(player: Player, amount: Int): Int {
        val tank = PlayerNanites.tank(player)
        val left = tank.fill(HostStrains.nanites(player, NaniteArchetypes.ARCHIVE, amount), true)
        val added = amount - left.amount
        if (added > 0) PlayerNanites.save(player, tank)
        return added
    }

    /**
     * Takes up to [amount] Archive nanites from [player]'s tank, whatever talents they carry (oldest first).
     *
     * @return Nanites taken.
     */
    fun draw(player: Player, amount: Int): Int = drawStacks(player, amount).sumOf { it.amount }

    /**
     * Takes up to [amount] Archive nanites from [player]'s tank, oldest first, as the stacks taken: a consumer reads
     * what they do from their talents ([com.itszuvalex.femtocraft.host.Talents.stat]).
     */
    fun drawStacks(player: Player, amount: Int): List<NaniteStack> {
        val tank = PlayerNanites.tank(player)
        val taken = ArrayList<NaniteStack>()
        var left = amount
        for (stack in tank.contents().filter { it.isSameStrain(NaniteRegistry.archive(1)) }) {
            if (left <= 0) break
            val drained = tank.drain(stack.withAmount(minOf(left, stack.amount)), true)
            if (drained.isEmpty) continue
            taken += drained
            left -= drained.amount
        }
        if (taken.isNotEmpty()) PlayerNanites.save(player, tank)
        return taken
    }

    /**
     * Takes up to [amount] Archive nanites from [player] for a consumer at [target] and shows them flowing there
     * ([FemtoParticles.naniteFlow]), as any machine fed by a host should.
     *
     * @return Nanites taken.
     */
    fun drawTo(player: Player, amount: Int, level: ServerLevel, target: Vec3): Int = drawStacksTo(player, amount, level, target).sumOf { it.amount }

    /** [drawTo], returning the stacks taken (see [drawStacks]). */
    fun drawStacksTo(player: Player, amount: Int, level: ServerLevel, target: Vec3): List<NaniteStack> {
        val taken = drawStacks(player, amount)
        val count = taken.sumOf { it.amount }
        if (count > 0) FemtoParticles.naniteFlow(level, player.position().add(0.0, player.bbHeight * .6, 0.0), target, ARCHIVE_COLOR, 4 + 2 * count)
        return taken
    }

    /** Colour of Archive nanites in the world. */
    const val ARCHIVE_COLOR = 0x5AE6FF

    /**
     * Hosts within [radius] blocks of [center] (plus each host's reach bonus, [HostStats.reachBonus]) that carry
     * Archive nanites, nearest first.
     */
    fun nearbyHosts(level: ServerLevel, center: BlockPos, radius: Double): List<Player> =
        level.players()
            .filter {
                if (it.isSpectator || !isHost(it) || archiveNanites(it) <= 0) return@filter false
                val reach = radius + HostStats.of(it).reachBonus
                it.distanceToSqr(center.center) <= reach * reach
            }
            .sortedBy { it.distanceToSqr(center.center) }
}
