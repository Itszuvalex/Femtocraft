package com.itszuvalex.femtocraft.nanite

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.itszulib.api.adapters.IModule
import com.itszuvalex.itszulib.api.adapters.Module
import com.itszuvalex.itszulib.core.SidedStorageConfiguration
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.Direction
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.Identifier
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.neoforged.neoforge.common.util.ValueIOSerializable

/**
 * A strain version. Port of v3's `NaniteStrainVersion` (keys `Ma`/`Mi`).
 */
data class NaniteStrainVersion(val major: Int, val minor: Int) : Comparable<NaniteStrainVersion> {
    fun bumpMajor() = NaniteStrainVersion(major + 1, 0)
    fun bumpMinor() = NaniteStrainVersion(major, minor + 1)
    override fun compareTo(other: NaniteStrainVersion): Int = compareValuesBy(this, other, { it.major }, { it.minor })

    companion object {
        @JvmField
        val ZERO = NaniteStrainVersion(0, 0)

        @JvmField
        val CODEC: Codec<NaniteStrainVersion> = RecordCodecBuilder.create { i ->
            i.group(Codec.INT.fieldOf("Ma").forGetter(NaniteStrainVersion::major), Codec.INT.fieldOf("Mi").forGetter(NaniteStrainVersion::minor))
                .apply(i, ::NaniteStrainVersion)
        }
    }
}

/**
 * An amount of one strain of nanites. Port of v3's `INaniteStack`/`NaniteStack` (keys `arch`, `strain`, `v`,
 * `amount`). Archetypes and strains are named; [NaniteRegistry] knows which exist.
 */
data class NaniteStack(val archetype: String, val strain: String, val version: NaniteStrainVersion, val amount: Int) {
    val isEmpty: Boolean get() = amount <= 0

    fun withAmount(amount: Int): NaniteStack = if (amount <= 0) EMPTY else copy(amount = amount)

    /**
     * Same archetype, strain (case-insensitive, as v3) and version.
     */
    fun isSameNanite(other: NaniteStack): Boolean = isSameStrain(other) && version == other.version

    fun isSameStrain(other: NaniteStack): Boolean =
        archetype.equals(other.archetype, ignoreCase = true) && strain.equals(other.strain, ignoreCase = true)

    companion object {
        @JvmField
        val EMPTY = NaniteStack("Empty", "Empty", NaniteStrainVersion.ZERO, 0)

        @JvmField
        val CODEC: Codec<NaniteStack> = RecordCodecBuilder.create { i ->
            i.group(
                Codec.STRING.fieldOf("arch").forGetter(NaniteStack::archetype),
                Codec.STRING.fieldOf("strain").forGetter(NaniteStack::strain),
                NaniteStrainVersion.CODEC.optionalFieldOf("v", NaniteStrainVersion.ZERO).forGetter(NaniteStack::version),
                Codec.INT.fieldOf("amount").forGetter(NaniteStack::amount),
            ).apply(i, ::NaniteStack)
        }

        @JvmField
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, NaniteStack> = ByteBufCodecs.fromCodecWithRegistries(CODEC)
    }
}

/**
 * The known archetypes and strains. Port of v3's `NaniteRegistry`. v3's only real nanites were the old API's "Dumb"
 * strain, which becomes archetype `Dumb` / strain `Dumb` (DECISIONS D5).
 */
object NaniteRegistry {
    const val DUMB = "Dumb"

    private val strains = LinkedHashMap<String, MutableSet<String>>()

    init {
        addArchetype(DUMB)
        addStrain(DUMB, DUMB)
    }

    fun addArchetype(name: String) {
        strains.getOrPut(name, ::LinkedHashSet)
    }

    /**
     * @throws IllegalArgumentException if the archetype is not registered (as v3).
     */
    fun addStrain(archetype: String, strain: String) {
        val set = strains[archetype] ?: throw IllegalArgumentException("Register archetype $archetype before its strain $strain")
        set += strain
    }

    fun isKnown(stack: NaniteStack): Boolean = strains[stack.archetype]?.contains(stack.strain) == true

    fun dumb(amount: Int): NaniteStack = NaniteStack(DUMB, DUMB, NaniteStrainVersion.ZERO, amount)
}

/**
 * Nanite storage. Port of v3's `INaniteTank`: [fill] returns what did not fit, [drain] what was drained.
 */
interface INaniteTank : ValueIOSerializable {
    val capacity: Int
    val amount: Int
    val room: Int get() = capacity - amount
    fun contents(): List<NaniteStack>
    fun canFill(stack: NaniteStack): Boolean
    fun canDrain(stack: NaniteStack): Boolean
    fun fill(stack: NaniteStack, doFill: Boolean): NaniteStack
    fun drain(stack: NaniteStack, doDrain: Boolean): NaniteStack

    companion object {
        @JvmField
        val EMPTY: INaniteTank = object : INaniteTank {
            override val capacity = 0
            override val amount = 0
            override fun contents(): List<NaniteStack> = listOf()
            override fun canFill(stack: NaniteStack) = false
            override fun canDrain(stack: NaniteStack) = false
            override fun fill(stack: NaniteStack, doFill: Boolean) = stack
            override fun drain(stack: NaniteStack, doDrain: Boolean) = NaniteStack.EMPTY
            override fun serialize(output: ValueOutput) {}
            override fun deserialize(input: ValueInput) {}
        }
    }
}

/**
 * A tank holding any number of strains up to a shared [capacity]. Port of v3's `NaniteTank` (saved as a list under
 * `Nanites`).
 *
 * @param accepts Extra filter on what may be filled (the nanite repository only takes the strain it already holds).
 */
open class NaniteTank @JvmOverloads constructor(
    override val capacity: Int,
    private val onChanged: Runnable = Runnable {},
    private val accepts: (NaniteTank, NaniteStack) -> Boolean = { _, _ -> true },
) : INaniteTank {
    private val nanites = ArrayList<NaniteStack>()

    override val amount: Int get() = nanites.sumOf { it.amount }

    override fun contents(): List<NaniteStack> = nanites.toList()

    override fun canFill(stack: NaniteStack): Boolean = !stack.isEmpty && accepts(this, stack)

    override fun canDrain(stack: NaniteStack): Boolean = nanites.any { it.isSameNanite(stack) }

    override fun fill(stack: NaniteStack, doFill: Boolean): NaniteStack {
        if (!canFill(stack)) return stack
        val toFill = minOf(stack.amount, room)
        if (toFill <= 0) return stack
        if (doFill) {
            val index = nanites.indexOfFirst { it.isSameNanite(stack) }
            if (index >= 0) nanites[index] = nanites[index].withAmount(nanites[index].amount + toFill) else nanites += stack.withAmount(toFill)
            onChanged.run()
        }
        return stack.withAmount(stack.amount - toFill)
    }

    override fun drain(stack: NaniteStack, doDrain: Boolean): NaniteStack {
        val index = nanites.indexOfFirst { it.isSameNanite(stack) }
        if (index < 0 || stack.amount <= 0) return NaniteStack.EMPTY
        val held = nanites[index]
        val drained = minOf(held.amount, stack.amount)
        if (doDrain) {
            if (held.amount - drained <= 0) nanites.removeAt(index) else nanites[index] = held.withAmount(held.amount - drained)
            onChanged.run()
        }
        return held.withAmount(drained)
    }

    fun setContents(stacks: List<NaniteStack>) {
        nanites.clear()
        nanites.addAll(stacks.filter { !it.isEmpty })
    }

    override fun serialize(output: ValueOutput) = output.store(NANITES_KEY, LIST, nanites.toList())

    override fun deserialize(input: ValueInput) = setContents(input.read(NANITES_KEY, LIST).orElse(listOf()))

    companion object {
        const val NANITES_KEY = "Nanites"

        @JvmField
        val LIST: Codec<List<NaniteStack>> = NaniteStack.CODEC.listOf()
    }
}

/**
 * Which nanite tank each face uses. Port of v3's `SidedNaniteStorageConfiguration`.
 */
open class SidedNaniteStorageConfiguration(
    defaults: (Direction) -> String,
    storages: Map<String, INaniteTank>,
    front: () -> Direction,
) : SidedStorageConfiguration<INaniteTank>(defaults, storages, front)

object NaniteModules {
    @JvmField
    val NANITE_TANK: IModule<INaniteTank> = Module.registerModule(id("nanite_tank"), null)

    @JvmField
    val NANITE_STORAGE_CONFIGURABLE: IModule<SidedNaniteStorageConfiguration> = Module.registerModule(id("nanite_storage_configurable"), null)

    private fun id(path: String) = Identifier.fromNamespaceAndPath(Femtocraft.ID, path)

    fun init() {}
}
