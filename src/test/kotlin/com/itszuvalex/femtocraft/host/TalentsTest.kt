package com.itszuvalex.femtocraft.host

import com.itszuvalex.femtocraft.nanite.NaniteStack
import com.itszuvalex.femtocraft.nanite.NaniteStrainVersion
import com.mojang.serialization.JsonOps
import net.minecraft.SharedConstants
import net.minecraft.resources.Identifier
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

private fun id(path: String) = Identifier.fromNamespaceAndPath("femtocraft_test", path)

class TalentsTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrap() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    private val archive = NaniteArchetypes.ARCHIVE.id
    private val cap = TalentStats.REGEN_CAP.id

    private fun talents() = Talents(mapOf(
        id("a") to Talent(archive, cost = 1, effects = mapOf(cap to 10.0)),
        id("b") to Talent(archive, listOf(id("a")), cost = 2, effects = mapOf(cap to 5.0, TalentStats.RESEARCH_PER_NANITE.id to 5.0)),
        id("x") to Talent(NaniteArchetypes.INDUSTRY.id, cost = 1),
    ))

    @Test
    fun Unlock_NeedsPrerequisitesAndPoints() {
        val t = talents()
        val none = HostStrain.EMPTY
        assertEquals(Talents.Refusal.NO_POINTS, t.refusal(id("a"), none, 0))
        assertEquals(Talents.Refusal.LOCKED, t.refusal(id("b"), none, 10))
        assertEquals(Talents.Refusal.UNKNOWN, t.refusal(id("nope"), none, 10))
        val withA = t.unlock(id("a"), none, 1)!!
        assertEquals(listOf(id("a")), withA.talents(archive))
        assertEquals(Talents.Refusal.ALREADY_UNLOCKED, t.refusal(id("a"), withA, 10))
        assertEquals(Talents.Refusal.NO_POINTS, t.refusal(id("b"), withA, 2), "1 of 2 earned points is spent")
        assertNull(t.refusal(id("b"), withA, 3))
        assertEquals(TalentState.AVAILABLE, t.state(id("b"), withA))
        assertEquals(TalentState.LOCKED, t.state(id("b"), none))
    }

    @Test
    fun Spent_IgnoresTalentsNoLongerInData() {
        val strain = HostStrain(mapOf(archive to listOf(id("a"), id("gone"))))
        assertEquals(1, talents().spent(strain), "a removed talent gives its points back")
    }

    @Test
    fun Trees_AreSeparate() {
        val t = talents()
        val strain = t.unlock(id("x"), HostStrain.EMPTY, 5)!!
        assertEquals(listOf(id("x")), strain.talents(NaniteArchetypes.INDUSTRY.id))
        assertTrue(strain.talents(archive).isEmpty())
        assertEquals(setOf(id("a"), id("b")), t.inArchetype(archive).keys)
    }

    @Test
    fun Stat_AddsEffectsOverBase_AndKeepsTheMinimum() {
        val t = talents()
        assertEquals(10.0, t.stat(TalentStats.REGEN_CAP, emptyList()))
        assertEquals(25.0, t.stat(TalentStats.REGEN_CAP, listOf(id("a"), id("b"))))
        val harsh = Talents(mapOf(id("h") to Talent(archive, effects = mapOf(cap to -50.0))))
        assertEquals(TalentStats.REGEN_CAP.min, harsh.stat(TalentStats.REGEN_CAP, listOf(id("h"))))
    }

    @Test
    fun HostStats_FollowTheTalents() {
        val t = Talents(mapOf(
            id("speed") to Talent(archive, effects = mapOf(TalentStats.REGEN_SPEED.id to 1.0)),
            id("hunger") to Talent(archive, effects = mapOf(TalentStats.REGEN_HUNGER.id to -0.5)),
            id("tank") to Talent(archive, effects = mapOf(TalentStats.TANK_CAPACITY.id to 50.0, TalentStats.HOST_REACH.id to 3.0)),
        ))
        assertEquals(HostStats(10, 200, 6f, 0.0, 100), HostStats.BASE)
        val s = HostStats.of(t, t.all.keys)
        assertEquals(100, s.regenInterval, "twice as fast")
        assertEquals(3f, s.regenExhaustion, "half the hunger")
        assertEquals(150, s.tankCapacity)
        assertEquals(3.0, s.reachBonus)
    }

    @Test
    fun Problems_ReportBadData() {
        val bad = Talents(mapOf(
            id("arch") to Talent("Nonsense"),
            id("stat") to Talent(archive, effects = mapOf(id("no_stat") to 1.0)),
            id("pre") to Talent(archive, listOf(id("missing"))),
            id("cross") to Talent(archive, listOf(id("other"))),
            id("other") to Talent(NaniteArchetypes.ENERGY.id),
            id("c1") to Talent(archive, listOf(id("c2"))),
            id("c2") to Talent(archive, listOf(id("c1"))),
        ))
        val problems = bad.problems().joinToString("\n")
        for (fragment in listOf("unknown archetype", "unknown stat", "unknown prerequisite", "another tree", "cycle")) assertTrue(fragment in problems, "reports $fragment:\n$problems")
        assertTrue(talents().problems().isEmpty())
    }

    @Test
    fun HostStrain_NormalisesAndRoundTrips() {
        val s = HostStrain.EMPTY.with(archive, listOf(id("b"), id("a"), id("a")))
        assertEquals(listOf(id("a"), id("b")), s.talents(archive))
        assertTrue(s.with(archive, emptyList()).unlocked.isEmpty(), "an empty tree is left out")
        val json = HostStrain.CODEC.encodeStart(JsonOps.INSTANCE, s).getOrThrow()
        assertEquals(s, HostStrain.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow())
    }

    @Test
    fun NaniteStack_TalentsDecideStacking_AndOldSavesLoad() {
        val plain = NaniteStack(archive, archive, NaniteStrainVersion.ZERO, 5)
        val talented = plain.copy(talents = listOf(id("a")))
        assertTrue(plain.isSameStrain(talented))
        assertFalse(plain.isSameNanite(talented), "different talents do not stack")
        val json = NaniteStack.CODEC.encodeStart(JsonOps.INSTANCE, talented).getOrThrow()
        assertEquals(talented, NaniteStack.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow())
        val old = com.google.gson.JsonParser.parseString("""{"arch":"Archive","strain":"Archive","amount":3}""")
        assertEquals(plain.withAmount(3), NaniteStack.CODEC.parse(JsonOps.INSTANCE, old).getOrThrow(), "a save from before talents loads talentless")
    }
}
