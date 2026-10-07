package com.itszuvalex.femtocraft.host

import com.itszuvalex.femtocraft.nanite.NaniteRegistry
import net.minecraft.network.chat.Component

/**
 * One of the nanite archetypes every host has (2020 "Nanites" notes): a kind of nanite with its own talent tree
 * ([Talents]). A host's unlocked talents in an archetype are their strain of it ([HostStrains]), carried by the nanites
 * they breed.
 *
 * @param id The archetype's name on nanite stacks ([com.itszuvalex.femtocraft.nanite.NaniteStack.archetype]).
 * @param color RGB, for screens and nanite particles.
 */
data class NaniteArchetype(val id: String, val color: Int) {
    val displayName: Component get() = Component.translatable("nanite.femtocraft.archetype.${id.lowercase()}")

    val description: Component get() = Component.translatable("nanite.femtocraft.archetype.${id.lowercase()}.desc")
}

/**
 * The archetypes, in screen order: the Archive's own tier 0 nanites, then the 2020 notes' six (Industry, Energy,
 * Growth, Utility, Fauna, Military; their colours are the notes'). Only Archive nanites are bred so far (by the host's
 * body, [com.itszuvalex.femtocraft.archive.NaniteHost]); the others have trees to fill and no source yet. The notes'
 * grey Dumb nanites are not upgradeable and have no tree.
 */
object NaniteArchetypes {
    @JvmField val ARCHIVE = NaniteArchetype(NaniteRegistry.ARCHIVE, 0x5AE6FF)
    @JvmField val INDUSTRY = NaniteArchetype("Industry", 0xA05AE6)
    @JvmField val ENERGY = NaniteArchetype("Energy", 0x3C8CFF)
    @JvmField val GROWTH = NaniteArchetype("Growth", 0x5AD25A)
    @JvmField val UTILITY = NaniteArchetype("Utility", 0xF0D23C)
    @JvmField val FAUNA = NaniteArchetype("Fauna", 0xF08C3C)
    @JvmField val MILITARY = NaniteArchetype("Military", 0xE63C3C)

    @JvmField
    val ALL: List<NaniteArchetype> = listOf(ARCHIVE, INDUSTRY, ENERGY, GROWTH, UTILITY, FAUNA, MILITARY)

    fun byId(id: String): NaniteArchetype? = ALL.firstOrNull { it.id.equals(id, ignoreCase = true) }

    /** Registers each archetype, with a strain of its own name, so stacks of them are known ([NaniteRegistry.isKnown]). */
    fun init() {
        for (a in ALL) {
            NaniteRegistry.addArchetype(a.id)
            NaniteRegistry.addStrain(a.id, a.id)
        }
    }
}
