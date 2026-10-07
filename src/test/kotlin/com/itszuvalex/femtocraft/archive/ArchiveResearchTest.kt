package com.itszuvalex.femtocraft.archive

import net.minecraft.resources.Identifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

/** A team's research and hosts, in memory. [keeps] is how many points the technology still accepts. */
private class FakePort(
    var focus: Identifier? = Identifier.parse("test:focus"),
    var needsItems: Boolean = false,
    var nanites: Int = 0,
    var keeps: Long = Long.MAX_VALUE,
) : ResearchPort {
    var progress = 0L
    var changed = 0

    override fun focus(): Identifier? = focus
    override fun needsItems(tech: Identifier): Boolean = needsItems
    override fun drawNanite(): Boolean = if (nanites > 0) true.also { nanites-- } else false
    override fun addProgress(tech: Identifier, amount: Long): Long = minOf(amount, keeps - progress).also { progress += it }
}

class ArchiveResearchTest {
    private val team = UUID.randomUUID()
    private var changed = 0
    private val state = ArchiveState { changed++ }

    @Test
    fun Research_NoTeam_Idle() {
        assertEquals(0L, state.research(null, FakePort()))
        assertEquals(ArchiveStatus.IDLE, state.status)
    }

    @Test
    fun Research_NoFocus_Idle() {
        state.status = ArchiveStatus.RESEARCHING
        assertEquals(0L, state.research(team, FakePort(focus = null, nanites = 1)))
        assertEquals(ArchiveStatus.IDLE, state.status)
    }

    @Test
    fun Research_WaitingForItems_DrawsNothing() {
        val port = FakePort(needsItems = true, nanites = 3)
        assertEquals(0L, state.research(team, port))
        assertEquals(ArchiveStatus.NEEDS_ITEMS, state.status)
        assertEquals(3, port.nanites)
    }

    @Test
    fun Research_NoNanitesNoPoints_NoHost() {
        assertEquals(0L, state.research(team, FakePort()))
        assertEquals(ArchiveStatus.NO_HOST, state.status)
    }

    @Test
    fun Research_OneNanite_BuysPointsAndSpendsAStep() {
        val port = FakePort(nanites = 1)
        assertEquals(ArchiveState.RATE, state.research(team, port))
        assertEquals(ArchiveStatus.RESEARCHING, state.status)
        assertEquals(ArchiveState.POINTS_PER_NANITE - ArchiveState.RATE, state.points)
        assertEquals(ArchiveState.RATE, port.progress)
    }

    @Test
    fun Research_PointsLeft_SpendsThemWithoutDrawing() {
        val port = FakePort(nanites = 5)
        state.research(team, port) // 10 bought, 5 spent
        state.research(team, port) // 5 left is not fewer than RATE: no draw
        assertEquals(4, port.nanites)
        assertEquals(0L, state.points)
        state.research(team, port) // empty: draws again
        assertEquals(3, port.nanites)
    }

    @Test
    fun Research_TechnologyKeepsLess_KeepsTheRestBuffered() {
        val port = FakePort(nanites = 1, keeps = 2L)
        assertEquals(2L, state.research(team, port))
        assertEquals(ArchiveState.POINTS_PER_NANITE - 2L, state.points)
    }

    @Test
    fun Research_ComputedPointsAlone_ResearchWithoutAHost() {
        state.addComputed(30L)
        val port = FakePort()
        assertEquals(ArchiveState.COMPUTED_RATE, state.research(team, port))
        assertEquals(ArchiveStatus.RESEARCHING, state.status)
        assertEquals(10L, state.computedPoints)
    }

    @Test
    fun Research_ComputedPointsGoToTheNextFocus() {
        val first = Identifier.parse("test:first")
        val second = Identifier.parse("test:second")
        val spent = ArrayList<Pair<Identifier, Long>>()
        val port = object : ResearchPort {
            var current = first
            override fun focus(): Identifier = current
            override fun needsItems(tech: Identifier) = false
            override fun drawNanite() = true
            override fun addProgress(tech: Identifier, amount: Long): Long {
                spent += tech to amount
                if (tech == first) current = second // the nanite points finish the first
                return amount
            }
        }
        state.addComputed(20L)
        state.research(team, port)
        assertEquals(listOf(first to ArchiveState.RATE, second to ArchiveState.COMPUTED_RATE), spent)
    }

    @Test
    fun AddComputed_KeepsAtMostTheCap() {
        assertEquals(ArchiveState.COMPUTED_CAP, state.addComputed(ArchiveState.COMPUTED_CAP + 50))
        assertEquals(0L, state.computedRoom())
        assertEquals(0L, state.addComputed(10L))
        assertEquals(0L, state.addComputed(-5L))
    }

    @Test
    fun Claim_OnlyTheFirstPlayerWins() {
        val first = UUID.randomUUID()
        assertTrue(state.claim(first))
        assertEquals(false, state.claim(UUID.randomUUID()))
        assertEquals(first, state.owner)
        assertEquals(1, changed)
    }

    @Test
    fun Research_ChangesNotifyOnlyWhenSomethingMoved() {
        state.research(team, FakePort())
        assertEquals(0, changed)
        state.research(team, FakePort(nanites = 1))
        assertTrue(changed > 0)
    }
}
