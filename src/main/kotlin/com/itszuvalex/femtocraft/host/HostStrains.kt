package com.itszuvalex.femtocraft.host

import com.itszuvalex.femtocraft.FemtoRegistries
import com.itszuvalex.femtocraft.archive.NaniteHost
import com.itszuvalex.femtocraft.nanite.NaniteStack
import com.itszuvalex.femtocraft.nanite.NaniteStrainVersion
import com.itszuvalex.itszulib.research.TechTree
import com.mojang.serialization.Codec
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Player
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.registries.DeferredHolder

/**
 * A host's strains: the talents they have unlocked in each archetype's tree, by archetype id. Immutable; archetypes
 * with nothing unlocked are left out, and each list is sorted without duplicates.
 */
data class HostStrain(val unlocked: Map<String, List<Identifier>> = emptyMap()) {
    /** The talents unlocked in [archetype]'s tree. */
    fun talents(archetype: String): List<Identifier> = unlocked.entries.firstOrNull { it.key.equals(archetype, ignoreCase = true) }?.value ?: emptyList()

    /** Every talent unlocked, in any tree. */
    val all: List<Identifier> get() = unlocked.values.flatten()

    /** With [archetype]'s talents set to [talents] (none removes it). */
    fun with(archetype: String, talents: Collection<Identifier>): HostStrain {
        val rest = unlocked.filterKeys { !it.equals(archetype, ignoreCase = true) }
        val list = talents.distinct().sorted()
        return HostStrain(if (list.isEmpty()) rest else rest + (archetype to list))
    }

    companion object {
        @JvmField
        val EMPTY = HostStrain()

        @JvmField
        val CODEC: Codec<HostStrain> = Codec.unboundedMap(Codec.STRING, Identifier.CODEC.listOf())
            .xmap({ m -> HostStrain(m.filterValues { it.isNotEmpty() }.mapValues { it.value.distinct().sorted() }) }, HostStrain::unlocked)

        @JvmField
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, HostStrain> = ByteBufCodecs.fromCodecWithRegistries(CODEC)
    }
}

/**
 * Each host's strains ([HostStrain]): every host has the same archetypes ([NaniteArchetypes]), and each archetype a
 * talent tree ([Talents]); what a host unlocks in a tree is their strain of that archetype. The nanites a host breeds
 * carry their strain's talents ([nanites]), so they act as that host's talents say wherever they go; nanites bred
 * before a change keep the talents they were bred with.
 *
 * Talent points: one for each technology the host's team has researched ([earned]), less what unlocked talents cost.
 * A reset ([reset]) gives an archetype's points back. Stored in a synced player attachment that survives death.
 */
object HostStrains {
    @JvmField
    val ATTACHMENT: DeferredHolder<AttachmentType<*>, AttachmentType<HostStrain>> = FemtoRegistries.ATTACHMENT_TYPES.register("host_strain") { ->
        AttachmentType.builder<HostStrain> { -> HostStrain.EMPTY }
            .serialize(HostStrain.CODEC.fieldOf("strain"))
            .copyOnDeath()
            .sync(HostStrain.STREAM_CODEC)
            .build()
    }

    fun of(player: Player): HostStrain = player.getData(ATTACHMENT.get())

    private fun set(player: Player, strain: HostStrain) = player.setData(ATTACHMENT.get(), strain)

    fun talents(player: Player): Talents = Talents.of(player.level().registryAccess())

    /** Talent points [player] has earned: one per technology their team has researched (on the client, their own team's). */
    fun earned(player: Player): Int = TechTree.research(player).unlocked.size

    fun spent(player: Player): Int = talents(player).spent(of(player))

    fun available(player: Player): Int = earned(player) - spent(player)

    /** Why [player] cannot unlock [talent], or null if they can: they must be a host, then [Talents.refusal]. */
    fun refusal(player: Player, talent: Identifier): Refusal? {
        if (!NaniteHost.isHost(player)) return Refusal.NOT_HOST
        return when (talents(player).refusal(talent, of(player), earned(player))) {
            null -> null
            Talents.Refusal.UNKNOWN -> Refusal.UNKNOWN
            Talents.Refusal.ALREADY_UNLOCKED -> Refusal.ALREADY_UNLOCKED
            Talents.Refusal.LOCKED -> Refusal.LOCKED
            Talents.Refusal.NO_POINTS -> Refusal.NO_POINTS
        }
    }

    enum class Refusal { NOT_HOST, UNKNOWN, ALREADY_UNLOCKED, LOCKED, NO_POINTS }

    /** Unlocks [talent] for [player] (server side). @return Why not, or null if it was unlocked. */
    fun unlock(player: Player, talent: Identifier): Refusal? {
        refusal(player, talent)?.let { return it }
        set(player, talents(player).unlock(talent, of(player), earned(player)) ?: return Refusal.LOCKED)
        return null
    }

    /** Forgets everything [player] unlocked in [archetype]'s tree, giving its points back. @return False if there was nothing. */
    fun reset(player: Player, archetype: String): Boolean {
        val strain = of(player)
        if (strain.talents(archetype).isEmpty()) return false
        set(player, strain.with(archetype, emptyList()))
        return true
    }

    /**
     * [amount] nanites of [archetype] as [player] breeds them now: their strain's talents (those that still exist), at
     * version 0.<talent count>.
     */
    fun nanites(player: Player, archetype: NaniteArchetype, amount: Int): NaniteStack {
        val known = talents(player)
        val carried = of(player).talents(archetype.id).filter { known[it] != null }
        return NaniteStack(archetype.id, archetype.id, NaniteStrainVersion(0, carried.size), amount, carried)
    }
}

/**
 * A host's body as their talents make it ([TalentStat.Scope.HOST] stats over every talent they have unlocked): how
 * many Archive nanites it keeps stocked and how fast and dear it breeds them, how far machines reach for it, and how
 * much its tank holds.
 */
data class HostStats(
    val regenCap: Int,
    val regenInterval: Int,
    val regenExhaustion: Float,
    val reachBonus: Double,
    val tankCapacity: Int,
) {
    companion object {
        /** Ticks between bred nanites at speed 1. */
        const val BASE_REGEN_INTERVAL = 200

        /** Hunger cost of one bred nanite at 100% (4 exhaustion is one food point). */
        const val BASE_REGEN_EXHAUSTION = 6f

        @JvmField
        val BASE = of(Talents.EMPTY, emptyList())

        /** The stats [talents] give, by [Talents.stat]. Pure. */
        @JvmStatic
        fun of(talents: Talents, unlocked: Collection<Identifier>): HostStats = HostStats(
            regenCap = talents.stat(TalentStats.REGEN_CAP, unlocked).toInt(),
            regenInterval = maxOf(1, Math.round(BASE_REGEN_INTERVAL / talents.stat(TalentStats.REGEN_SPEED, unlocked)).toInt()),
            regenExhaustion = (BASE_REGEN_EXHAUSTION * talents.stat(TalentStats.REGEN_HUNGER, unlocked)).toFloat(),
            reachBonus = talents.stat(TalentStats.HOST_REACH, unlocked),
            tankCapacity = talents.stat(TalentStats.TANK_CAPACITY, unlocked).toInt(),
        )

        /** [player]'s stats (either side: the strain and talents are synced). */
        @JvmStatic
        fun of(player: Player): HostStats = of(HostStrains.talents(player), HostStrains.of(player).all)
    }
}
