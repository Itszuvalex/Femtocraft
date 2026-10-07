package com.itszuvalex.femtocraft.logistics

import com.itszuvalex.itszulib.api.adapters.IFluidStack
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.resources.Identifier
import net.neoforged.neoforge.fluids.FluidStack
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** A fluid stack with no registry behind it. */
private class Fluid(private val id: Identifier, private var amount: Int) : IFluidStack {
    override fun fluid(): Identifier = id
    override fun amount(): Int = amount
    override fun setAmount(amount: Int) {
        this.amount = amount
    }

    override fun components(): DataComponentPatch = DataComponentPatch.EMPTY
    override fun toMinecraft(): FluidStack = throw UnsupportedOperationException()
    override fun isEmpty(): Boolean = amount <= 0
    override fun copy(): IFluidStack = Fluid(id, amount)
    override fun isFluidEqual(other: IFluidStack): Boolean = !other.isEmpty() && other.fluid() == id
}

private val WATER = Identifier.parse("test:water")
private val LAVA = Identifier.parse("test:lava")

private fun water(amount: Int): IFluidStack = Fluid(WATER, amount)
private fun lava(amount: Int): IFluidStack = Fluid(LAVA, amount)

class ReservoirGroupsTest {
    @Test
    fun GroupsOf_NoLinks_OneTankPerCell() {
        assertEquals(listOf(0..0, 1..1, 2..2, 3..3), ReservoirTanks.groupsOf(0, 4))
    }

    @Test
    fun GroupsOf_LinkedBoundaries_JoinNeighbours() {
        assertEquals(listOf(0..1, 2..3), ReservoirTanks.groupsOf(0b101, 4))
        assertEquals(listOf(0..3), ReservoirTanks.groupsOf(0b111, 4))
        assertEquals(listOf(0..0, 1..2, 3..3), ReservoirTanks.groupsOf(0b010, 4))
    }

    @Test
    fun GroupsOf_SingleCell_OneTank() {
        assertEquals(listOf(0..0), ReservoirTanks.groupsOf(0, 1))
    }
}

class ReservoirTanksTest {
    private fun tanks() = ReservoirTanks(4, 1000)

    @Test
    fun SetQuietly_OverCellCapacity_SpillsIntoLinkedCellsInOrder() {
        val tanks = tanks()
        assertTrue(tanks.link(0))
        tanks.setQuietly(0, water(1500))
        assertEquals(1000, tanks.cell(0).amount())
        assertEquals(500, tanks.cell(1).amount())
        assertEquals(1500, tanks.get(0).amount())
        assertEquals(2000, tanks.capacity(0))
    }

    @Test
    fun Link_TwoTanksOfTheSameFluid_MergeTheirContents() {
        val tanks = tanks()
        tanks.setQuietly(0, water(400))
        tanks.setQuietly(1, water(300))
        assertTrue(tanks.link(0))
        assertEquals(3, tanks.size())
        assertEquals(700, tanks.get(0).amount())
    }

    @Test
    fun CanLink_DifferentFluids_Refused() {
        val tanks = tanks()
        tanks.setQuietly(0, water(10))
        tanks.setQuietly(1, lava(10))
        assertFalse(tanks.canLink(0))
        assertFalse(tanks.link(0))
        assertEquals(4, tanks.size())
    }

    @Test
    fun CanLink_LockAgainstOtherFluid_Refused() {
        val tanks = tanks()
        tanks.setQuietly(0, water(10))
        assertTrue(tanks.toggleLock(1, LAVA))
        assertFalse(tanks.canLink(0))
    }

    @Test
    fun CanLink_EmptyTanksOrSameLock_Allowed() {
        val tanks = tanks()
        assertTrue(tanks.canLink(0))
        tanks.toggleLock(2, WATER)
        tanks.toggleLock(3, WATER)
        assertTrue(tanks.canLink(2))
    }

    @Test
    fun CanLink_OutOfRangeOrAlreadyLinked_Refused() {
        val tanks = tanks()
        assertFalse(tanks.canLink(-1))
        assertFalse(tanks.canLink(3))
        tanks.link(1)
        assertFalse(tanks.canLink(1))
    }

    @Test
    fun Link_KeepsTheLockOfEitherSide() {
        val tanks = tanks()
        tanks.toggleLock(1, WATER)
        tanks.link(0)
        assertEquals(WATER, tanks.lockOf(0))
    }

    @Test
    fun Unlink_SplitsKeepingEachCellsFluidAndTheLock() {
        val tanks = tanks()
        tanks.link(0)
        tanks.setQuietly(0, water(1500))
        tanks.toggleLock(0, null)
        assertTrue(tanks.unlink(0))
        assertEquals(4, tanks.size())
        assertEquals(1000, tanks.get(0).amount())
        assertEquals(500, tanks.get(1).amount())
        assertEquals(WATER, tanks.lockOf(0))
        assertEquals(WATER, tanks.lockOf(1))
    }

    @Test
    fun Unlink_NotLinked_Refused() {
        assertFalse(tanks().unlink(0))
    }

    @Test
    fun ToggleLock_EmptyNoFallback_Refused() {
        assertFalse(tanks().toggleLock(0, null))
    }

    @Test
    fun ToggleLock_LocksToHeldFluidThenUnlocks() {
        val tanks = tanks()
        tanks.setQuietly(0, water(10))
        assertTrue(tanks.toggleLock(0, LAVA))
        assertEquals(WATER, tanks.lockOf(0))
        assertTrue(tanks.toggleLock(0, LAVA))
        assertNull(tanks.lockOf(0))
    }

    @Test
    fun CanFillFluidType_LockedTank_OnlyItsFluid() {
        val tanks = tanks()
        tanks.toggleLock(0, WATER)
        assertTrue(tanks.canFillFluidType(0, water(1)))
        assertFalse(tanks.canFillFluidType(0, lava(1)))
        assertTrue(tanks.canFillFluidType(1, lava(1)))
        assertFalse(tanks.canFillFluidType(1, water(0)))
    }

    @Test
    fun Changes_NotifyExceptQuietOnes() {
        var changes = 0
        val tanks = ReservoirTanks(2, 1000) { changes++ }
        tanks.setQuietly(0, water(1))
        tanks.mirrorCell(1, water(1))
        assertEquals(0, changes)
        tanks.set(0, water(2))
        tanks.link(0)
        assertEquals(2, changes)
    }

    @Test
    fun MirrorLinks_RebuildsTheTanks() {
        val tanks = tanks()
        tanks.mirrorLinks(0b110)
        assertEquals(listOf(0..0, 1..3), tanks.groups)
        assertEquals(0b110, tanks.linkMask())
        assertEquals(1, tanks.tankOfCell(2))
    }
}
