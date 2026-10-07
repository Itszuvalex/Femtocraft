package com.itszuvalex.femtocraft.host

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.itszulib.research.TechTreeLayout
import com.mojang.logging.LogUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Registry
import net.minecraft.core.RegistryAccess
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.server.ServerStartedEvent
import net.neoforged.neoforge.registries.DataPackRegistryEvent
import java.util.Optional

/**
 * A talent in one archetype's tree: what a host can unlock in their strain of that archetype ([HostStrains]). A
 * synced datapack entry, `data/<ns>/femtocraft/talent/<path>.json`.
 *
 * @param archetype The tree it is in ([NaniteArchetypes]).
 * @param prerequisites Talents of the same tree to unlock first.
 * @param cost Talent points it takes (points come from the team's research, [HostStrains.earned]).
 * @param name Defaults to the translation `talent.<ns>.<path>` (slashes as dots), the description to `...desc`.
 * @param position Where to draw it in its tree, in layout cells; unset to place it automatically.
 * @param effects What it adds to [TalentStat]s, by stat id (e.g. `"femtocraft:regen_cap": 10`).
 */
data class Talent @JvmOverloads constructor(
    val archetype: String,
    val prerequisites: List<Identifier> = emptyList(),
    val cost: Int = 1,
    val icon: Item = Items.AMETHYST_SHARD,
    val name: Optional<Component> = Optional.empty(),
    val description: Optional<Component> = Optional.empty(),
    val position: Optional<TechTreeLayout.Point> = Optional.empty(),
    val effects: Map<Identifier, Double> = emptyMap(),
) {
    init {
        require(cost >= 0) { "Talent cost must not be negative: $cost" }
    }

    fun iconStack(): ItemStack = ItemStack(icon)

    fun displayName(id: Identifier): Component = name.orElseGet { Component.translatable(translationKey(id)) }

    fun displayDescription(id: Identifier): Component = description.orElseGet { Component.translatable(translationKey(id) + ".desc") }

    companion object {
        @JvmStatic
        fun translationKey(id: Identifier): String = "talent.${id.namespace}.${id.path.replace('/', '.')}"

        private val POINT_CODEC: Codec<TechTreeLayout.Point> = RecordCodecBuilder.create { i ->
            i.group(
                Codec.FLOAT.fieldOf("x").forGetter(TechTreeLayout.Point::x),
                Codec.FLOAT.fieldOf("y").forGetter(TechTreeLayout.Point::y),
            ).apply(i, TechTreeLayout::Point)
        }

        @JvmField
        val CODEC: Codec<Talent> = RecordCodecBuilder.create { i ->
            i.group(
                Codec.STRING.fieldOf("archetype").forGetter(Talent::archetype),
                Identifier.CODEC.listOf().optionalFieldOf("prerequisites", emptyList()).forGetter(Talent::prerequisites),
                Codec.INT.validate { if (it >= 0) DataResult.success(it) else DataResult.error { "cost must not be negative: $it" } }
                    .optionalFieldOf("cost", 1).forGetter(Talent::cost),
                BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("icon", Items.AMETHYST_SHARD).forGetter(Talent::icon),
                ComponentSerialization.CODEC.optionalFieldOf("name").forGetter(Talent::name),
                ComponentSerialization.CODEC.optionalFieldOf("description").forGetter(Talent::description),
                POINT_CODEC.optionalFieldOf("position").forGetter(Talent::position),
                Codec.unboundedMap(Identifier.CODEC, Codec.DOUBLE).optionalFieldOf("effects", emptyMap()).forGetter(Talent::effects),
            ).apply(i, ::Talent)
        }
    }
}

/**
 * A number talents change: [base] plus what each counted talent adds ([Talents.stat]). A [TalentStat.Scope.HOST] stat
 * describes the host's body and counts every talent the host has unlocked, in any tree ([HostStats]); a
 * [TalentStat.Scope.NANITE] stat describes what nanites do where they end up and counts the talents a nanite stack
 * carries ([com.itszuvalex.femtocraft.nanite.NaniteStack.talents]). Values never go below [min].
 *
 * @param percent Shown as a percentage of 1 (a multiplier) rather than a plain number.
 */
data class TalentStat @JvmOverloads constructor(
    val id: Identifier,
    val scope: Scope,
    val base: Double,
    val min: Double = 0.0,
    val percent: Boolean = false,
) {
    enum class Scope { HOST, NANITE }

    val displayName: Component get() = Component.translatable("stat.${id.namespace}.${id.path}")

    /** [value] as the stat shows it: a percentage for multipliers, else the number. */
    fun format(value: Double): String = if (percent) "${Math.round(value * 100)}%" else if (value == Math.floor(value)) value.toLong().toString() else "%.1f".format(java.util.Locale.ROOT, value)

    /** What a talent adds, as the stat shows it ("+10", "+25%"). */
    fun formatEffect(amount: Double): String = (if (amount >= 0) "+" else "") + format(amount)
}

/**
 * The stats talents can change, registered in code (talents name them in data). Mods and later features add their
 * own with [register].
 */
object TalentStats {
    private val stats = LinkedHashMap<Identifier, TalentStat>()

    private fun id(path: String) = Identifier.fromNamespaceAndPath(Femtocraft.ID, path)

    /** Archive nanites a host's body keeps itself stocked with. */
    @JvmField val REGEN_CAP = register(TalentStat(id("regen_cap"), TalentStat.Scope.HOST, 10.0, min = 1.0))

    /** How fast the body breeds them (1 = one every 10 seconds). */
    @JvmField val REGEN_SPEED = register(TalentStat(id("regen_speed"), TalentStat.Scope.HOST, 1.0, min = 0.1, percent = true))

    /** Hunger paid per bred nanite, as a share of the base cost. */
    @JvmField val REGEN_HUNGER = register(TalentStat(id("regen_hunger"), TalentStat.Scope.HOST, 1.0, percent = true))

    /** Blocks added to how far machines reach to draw from the host. */
    @JvmField val HOST_REACH = register(TalentStat(id("host_reach"), TalentStat.Scope.HOST, 0.0))

    /** Nanites the host's own tank holds. */
    @JvmField val TANK_CAPACITY = register(TalentStat(id("tank_capacity"), TalentStat.Scope.HOST, 100.0, min = 1.0))

    /** Research points an Archive gets for one of these nanites. */
    @JvmField val RESEARCH_PER_NANITE = register(TalentStat(id("research_per_nanite"), TalentStat.Scope.NANITE, 10.0, min = 1.0))

    @JvmStatic
    fun register(stat: TalentStat): TalentStat {
        require(stats.putIfAbsent(stat.id, stat) == null) { "Talent stat ${stat.id} registered twice" }
        return stat
    }

    operator fun get(id: Identifier): TalentStat? = stats[id]

    val all: Collection<TalentStat> get() = stats.values
}

/** Where a talent stands for a host. */
enum class TalentState {
    UNLOCKED,

    /** Every prerequisite is unlocked (it may still cost more points than the host has). */
    AVAILABLE,
    LOCKED,
}

/**
 * The talents, usually every one in the [Talents.KEY] registry ([Talents.of]). Pure: the rules for what a host can
 * unlock, and what talents add to stats, testable without a game.
 */
class Talents(val all: Map<Identifier, Talent>) {
    operator fun get(id: Identifier): Talent? = all[id]

    fun inArchetype(archetype: String): Map<Identifier, Talent> = all.filterValues { it.archetype.equals(archetype, ignoreCase = true) }

    private val layouts = HashMap<String, TechTreeLayout.Result>()

    /** [archetype]'s tree laid out, computed once. */
    fun layout(archetype: String): TechTreeLayout.Result = synchronized(layouts) {
        layouts.getOrPut(archetype.lowercase()) {
            val tree = inArchetype(archetype)
            TechTreeLayout.layoutGraph(tree.mapValues { it.value.prerequisites }, tree.mapNotNull { (id, t) -> t.position.orElse(null)?.let { id to it } }.toMap())
        }
    }

    /** @return Null for an unknown id. */
    fun state(id: Identifier, strain: HostStrain): TalentState? {
        val talent = all[id] ?: return null
        val unlocked = strain.talents(talent.archetype)
        return when {
            id in unlocked -> TalentState.UNLOCKED
            talent.prerequisites.all { it in unlocked && all[it]?.archetype.equals(talent.archetype, ignoreCase = true) } -> TalentState.AVAILABLE
            else -> TalentState.LOCKED
        }
    }

    /** Points [strain] has spent: the cost of each unlocked talent that still exists. */
    fun spent(strain: HostStrain): Int = strain.unlocked.values.sumOf { ids -> ids.sumOf { all[it]?.cost ?: 0 } }

    /** Why [strain] cannot unlock [id] with [earned] points, or null if it can. */
    fun refusal(id: Identifier, strain: HostStrain, earned: Int): Refusal? {
        val talent = all[id] ?: return Refusal.UNKNOWN
        if (NaniteArchetypes.byId(talent.archetype) == null) return Refusal.UNKNOWN
        return when (state(id, strain)) {
            TalentState.UNLOCKED -> Refusal.ALREADY_UNLOCKED
            TalentState.LOCKED, null -> Refusal.LOCKED
            TalentState.AVAILABLE -> if (earned - spent(strain) < talent.cost) Refusal.NO_POINTS else null
        }
    }

    enum class Refusal { UNKNOWN, ALREADY_UNLOCKED, LOCKED, NO_POINTS }

    /** [strain] with [id] unlocked, or null if it cannot be ([refusal]). */
    fun unlock(id: Identifier, strain: HostStrain, earned: Int): HostStrain? =
        if (refusal(id, strain, earned) != null) null else strain.with(all.getValue(id).archetype, strain.talents(all.getValue(id).archetype) + id)

    /**
     * [stat] counting [talents]: its base plus each one's effect on it (talents that no longer exist add nothing),
     * at least the stat's minimum.
     */
    fun stat(stat: TalentStat, talents: Collection<Identifier>): Double =
        maxOf(stat.min, stat.base + talents.sumOf { all[it]?.effects?.get(stat.id) ?: 0.0 })

    /** Data errors: unknown archetypes, stats or prerequisites, prerequisites in another tree, and cycles. */
    fun problems(): List<String> {
        val out = ArrayList<String>()
        for ((id, t) in all) {
            if (NaniteArchetypes.byId(t.archetype) == null) out += "$id: unknown archetype ${t.archetype}"
            for (s in t.effects.keys) if (TalentStats[s] == null) out += "$id: unknown stat $s"
            for (p in t.prerequisites) {
                val pre = all[p]
                if (pre == null) out += "$id: unknown prerequisite $p"
                else if (!pre.archetype.equals(t.archetype, ignoreCase = true)) out += "$id: prerequisite $p is in another tree (${pre.archetype})"
            }
        }
        val visiting = HashSet<Identifier>()
        val done = HashSet<Identifier>()
        fun visit(id: Identifier): Boolean {
            if (id in done) return false
            if (!visiting.add(id)) return true
            val cycle = all[id]?.prerequisites?.any(::visit) == true
            visiting -= id
            done += id
            return cycle
        }
        for (id in all.keys.sorted()) if (id !in done && visit(id)) out += "$id: prerequisite cycle"
        return out
    }

    companion object {
        @JvmField
        val EMPTY = Talents(emptyMap())

        @JvmField
        val KEY: ResourceKey<Registry<Talent>> = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(Femtocraft.ID, "talent"))

        private val LOGGER = LogUtils.getLogger()

        @JvmStatic
        fun register(modBus: IEventBus) {
            modBus.addListener { event: DataPackRegistryEvent.NewRegistry -> event.dataPackRegistry(KEY, Talent.CODEC, Talent.CODEC) }
            NeoForge.EVENT_BUS.addListener { event: ServerStartedEvent -> of(event.server.registryAccess()).problems().forEach { LOGGER.error("Talents: {}", it) } }
        }

        private var cachedRegistry: Registry<Talent>? = null
        private var cached: Talents = EMPTY

        /** The talents in [access] (the server's, or a client level's). Rebuilt only when the registry changes. */
        @JvmStatic
        fun of(access: RegistryAccess): Talents = synchronized(this) {
            val registry = access.lookup(KEY).orElse(null) ?: return EMPTY
            if (registry !== cachedRegistry) {
                cachedRegistry = registry
                cached = Talents(registry.entrySet().associate { it.key.identifier() to it.value })
            }
            cached
        }
    }
}
