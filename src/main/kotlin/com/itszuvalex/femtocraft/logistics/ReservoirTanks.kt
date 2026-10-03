package com.itszuvalex.femtocraft.logistics

import com.itszuvalex.itszulib.api.adapters.IFluidStack
import com.itszuvalex.itszulib.api.storage.IFluidStorage
import com.itszuvalex.itszulib.api.storage.IndexedFluidStorage
import net.minecraft.resources.Identifier
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

/**
 * The fluid reservoir's tanks: [cellCount] cells of [cellCapacity] mB in a row. Neighbouring cells can be linked, and a
 * run of linked cells is one tank of their combined capacity until unlinked again ([link], [unlink]). A tank can be
 * locked to one fluid ([toggleLock]): it then takes only that fluid, even while empty, so several fluids can share a
 * reservoir without one spilling into the tanks meant for another.
 *
 * As an [IFluidStorage] it shows one tank per run of linked cells. A tank's fluid fills its cells in order (the first
 * full before the next), so unlinking leaves each part holding what its cells held; every cell of a tank holds the
 * same fluid and the same lock.
 *
 * Saved as the cells (keyed "0", "1", ... as a plain fluid array, so saves from before linking still load), the locks
 * (`Lock<i>`) and the links (`Links`, a bit per boundary).
 */
class ReservoirTanks @JvmOverloads constructor(
    val cellCount: Int,
    val cellCapacity: Int,
    private val onChanged: Runnable = Runnable {},
) : IFluidStorage {
    private val cells = Array(cellCount) { IFluidStack.Empty }
    private val locks = arrayOfNulls<Identifier>(cellCount)
    private var links = 0

    /** The tanks, as runs of cells. */
    var groups: List<IntRange> = groupsOf(links, cellCount)
        private set

    fun cell(i: Int): IFluidStack = cells[i]

    /** The fluid tank [tank] is locked to, or null. */
    fun lockOf(tank: Int): Identifier? = locks[groups[tank].first]

    /** The links as a bit per boundary (bit i links cell i and i + 1). */
    fun linkMask(): Int = links

    fun isLinked(boundary: Int): Boolean = (links shr boundary) and 1 != 0

    /** The tank holding [cell]. */
    fun tankOfCell(cell: Int): Int = groups.indexOfFirst { cell in it }

    override fun size(): Int = groups.size

    override fun get(index: Int): IFluidStack {
        val g = groups[index]
        val first = g.firstOrNull { !cells[it].isEmpty() } ?: return IFluidStack.Empty
        return cells[first].copyWithAmount(g.sumOf { cells[it].amount() })
    }

    override fun set(index: Int, stack: IFluidStack) {
        setQuietly(index, stack)
        onChanged.run()
    }

    /** Spreads [stack] over the tank's cells, filling them in order. */
    override fun setQuietly(index: Int, stack: IFluidStack) {
        var left = if (stack.isEmpty()) 0 else stack.amount()
        for (c in groups[index]) {
            val part = minOf(left, cellCapacity)
            cells[c] = if (part <= 0) IFluidStack.Empty else stack.copyWithAmount(part)
            left -= part
        }
    }

    override fun capacity(index: Int): Int = groups[index].count() * cellCapacity

    override fun canFillFluidType(index: Int, resource: IFluidStack): Boolean =
        !resource.isEmpty() && (lockOf(index)?.let { it == resource.fluid() } ?: true)

    /**
     * Whether the cells either side of [boundary] can be linked: not linked yet, and their tanks hold no two different
     * fluids and no two different locks (a lock and another fluid count as different).
     */
    fun canLink(boundary: Int): Boolean {
        if (boundary !in 0 until cellCount - 1 || isLinked(boundary)) return false
        val a = tankOfCell(boundary)
        val b = tankOfCell(boundary + 1)
        val fa = get(a).takeUnless { it.isEmpty() }?.fluid()
        val fb = get(b).takeUnless { it.isEmpty() }?.fluid()
        val la = lockOf(a)
        val lb = lockOf(b)
        val kinds = listOfNotNull(fa, fb, la, lb).toSet()
        return kinds.size <= 1
    }

    /** Links the cells either side of [boundary] into one tank. @return False if [canLink] refuses. */
    fun link(boundary: Int): Boolean {
        if (!canLink(boundary)) return false
        val a = tankOfCell(boundary)
        val b = tankOfCell(boundary + 1)
        val merged = listOf(get(a), get(b)).firstOrNull { !it.isEmpty() }?.copyWithAmount(get(a).amount() + get(b).amount()) ?: IFluidStack.Empty
        val lock = lockOf(a) ?: lockOf(b)
        links = links or (1 shl boundary)
        groups = groupsOf(links, cellCount)
        val tank = tankOfCell(boundary)
        setQuietly(tank, merged)
        for (c in groups[tank]) locks[c] = lock
        onChanged.run()
        return true
    }

    /** Splits the tank at [boundary]; each part keeps its cells' fluid and the lock. @return False if not linked. */
    fun unlink(boundary: Int): Boolean {
        if (boundary !in 0 until cellCount - 1 || !isLinked(boundary)) return false
        links = links and (1 shl boundary).inv()
        groups = groupsOf(links, cellCount)
        onChanged.run()
        return true
    }

    /**
     * Unlocks [tank] if locked; otherwise locks it to the fluid it holds or, while empty, to [fallback] (e.g. the fluid
     * in the container the player holds). @return False if there was nothing to lock it to.
     */
    fun toggleLock(tank: Int, fallback: Identifier?): Boolean {
        val lock = if (lockOf(tank) != null) null else get(tank).takeUnless { it.isEmpty() }?.fluid() ?: fallback ?: return false
        for (c in groups[tank]) locks[c] = lock
        onChanged.run()
        return true
    }

    /** Client copies: set a cell, a cell's lock or the links as synced, without notifying. */
    fun mirrorCell(i: Int, stack: IFluidStack) {
        cells[i] = stack
    }

    fun mirrorLock(i: Int, lock: Identifier?) {
        locks[i] = lock
    }

    fun mirrorLinks(mask: Int) {
        links = mask
        groups = groupsOf(links, cellCount)
    }

    override fun serialize(output: ValueOutput) {
        for (i in 0 until cellCount) {
            if (!cells[i].isEmpty()) output.store(i.toString(), IFluidStack.codec(), cells[i])
            locks[i]?.let { output.store("Lock$i", Identifier.CODEC, it) }
        }
        output.putInt("Links", links)
    }

    override fun deserialize(input: ValueInput) {
        for (i in 0 until cellCount) {
            cells[i] = input.read(i.toString(), IFluidStack.codec()).orElse(IFluidStack.Empty)
            locks[i] = input.read("Lock$i", Identifier.CODEC).orElse(null)
        }
        mirrorLinks(input.getIntOr("Links", 0) and ((1 shl (cellCount - 1)) - 1))
        // Keep the invariants whatever was saved: each tank one fluid, filled in order, one lock.
        for (t in groups.indices) {
            val g = groups[t]
            val first = g.firstOrNull { !cells[it].isEmpty() }
            val fluid = first?.let { cells[it] }
            val total = g.sumOf { c -> if (fluid != null && cells[c].isFluidEqual(fluid)) cells[c].amount() else 0 }
            setQuietly(t, fluid?.copyWithAmount(total) ?: IFluidStack.Empty)
            val lock = locks[g.first]
            for (c in g) locks[c] = lock
        }
    }

    companion object {
        /** The tanks for a link mask over [cells] cells: runs of cells joined by set bits. */
        @JvmStatic
        fun groupsOf(links: Int, cells: Int): List<IntRange> {
            val groups = ArrayList<IntRange>()
            var start = 0
            for (i in 0 until cells) {
                if (i == cells - 1 || (links shr i) and 1 == 0) {
                    groups += start..i
                    start = i + 1
                }
            }
            return groups
        }
    }
}

/**
 * The reservoir's tanks behind an ItszuLib index, filling empty tanks locked to a fluid before free ones, so a locked
 * tank takes its fluid first and only the overflow goes elsewhere.
 */
class ReservoirIndexedStorage(val cells: ReservoirTanks) : IndexedFluidStorage(cells) {
    override fun fillOrder(resource: IFluidStack): List<Int> =
        tanksOf(resource.fluid()).toList() + emptyTanks().sortedBy { if (cells.lockOf(it) == resource.fluid()) 0 else 1 }
}
