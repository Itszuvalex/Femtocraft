package com.itszuvalex.femtocraft.power

import com.itszuvalex.itszulib.api.adapters.IBattery
import com.itszuvalex.itszulib.api.utility.Loc4
import kotlin.math.min

/**
 * Something power can be moved into or out of, with a per-tick limit. Port of v3's `DistributableResource`.
 */
interface DistributableResource {
    val max: Double
    val amount: Double
    val room: Double get() = max - amount
    val transferMax: Double

    /**
     * @return Amount actually added.
     */
    fun add(amount: Double): Double

    /**
     * @return Amount actually removed.
     */
    fun remove(amount: Double): Double
}

class DistributableBattery(private val battery: IBattery, private val transfer: () -> Double) : DistributableResource {
    override val max: Double get() = battery.maxStorage()
    override val amount: Double get() = battery.storage()
    override val transferMax: Double get() = transfer()
    override fun add(amount: Double): Double = battery.fill(amount)
    override fun remove(amount: Double): Double = battery.drain(amount)
}

/**
 * Moves power from producers (then storage) to consumers (then storage), each within its transfer limit. Port of v3's
 * `DistributionAlgorithm`.
 *
 * Priorities as in v3: producers with the least room give first (so generators do not fill up), storage with the
 * least room gives first, storage with the least power fills first, consumers with the least power fill first. If
 * producers make at least what consumers can take, the surplus goes to storage; otherwise storage tops consumers up.
 *
 * Difference from v3: each step removes from the source only what the sink actually accepted (v3 removed first and
 * ignored what `add` returned, so a sink that took less destroyed the rest).
 */
class DistributionAlgorithm(
    private val producers: Collection<DistributableResource>,
    private val storage: Collection<DistributableResource>,
    private val consumers: Collection<DistributableResource>,
) {
    /**
     * Totals moved by one [distribute] call.
     */
    data class Result(val fromProducers: Double, val fromStorage: Double, val toConsumers: Double, val toStorage: Double) {
        val total: Double get() = toConsumers + toStorage

        companion object {
            val NONE = Result(0.0, 0.0, 0.0, 0.0)
        }
    }

    private class Entry(val node: DistributableResource, var remaining: Double, val primary: Boolean)

    fun distribute(): Result {
        val producerEntries = producers.map { Entry(it, min(it.amount, it.transferMax).coerceAtLeast(0.0), true) }.sortedBy { it.node.room }
        val storageTake = storage.map { Entry(it, min(it.amount, it.transferMax).coerceAtLeast(0.0), false) }.sortedBy { it.node.room }
        val storageGive = storage.map { Entry(it, min(it.room, it.transferMax).coerceAtLeast(0.0), false) }.sortedBy { it.node.amount }
        val consumerEntries = consumers.map { Entry(it, min(it.room, it.transferMax).coerceAtLeast(0.0), true) }.sortedBy { it.node.amount }

        val produced = producerEntries.sumOf { it.remaining }
        val stored = storageTake.sumOf { it.remaining }
        val storageRoom = storageGive.sumOf { it.remaining }
        val consumerRoom = consumerEntries.sumOf { it.remaining }

        if (produced <= 0 && stored <= 0) return Result.NONE
        if (consumerRoom <= 0 && storageRoom <= 0) return Result.NONE

        val toDistribute = if (produced >= consumerRoom) min(produced, storageRoom + consumerRoom) else min(consumerRoom, produced + stored)

        val sources = (producerEntries + storageTake).iterator()
        val sinks = (consumerEntries + storageGive).iterator()
        var source = sources.nextOrNull()
        var sink = sinks.nextOrNull()
        var distributed = 0.0
        var fromProducers = 0.0
        var fromStorage = 0.0
        var toConsumers = 0.0
        var toStorage = 0.0
        while (distributed < toDistribute && source != null && sink != null) {
            // A storage battery is never both the source and the sink of one step.
            if (source.node === sink.node) {
                sink = sinks.nextOrNull()
                continue
            }
            val shift = minOf(source.remaining, sink.remaining, toDistribute - distributed)
            val moved = if (shift > 0) source.node.remove(sink.node.add(shift)) else 0.0
            source.remaining -= shift
            sink.remaining -= shift
            distributed += moved
            if (source.primary) fromProducers += moved else fromStorage += moved
            if (sink.primary) toConsumers += moved else toStorage += moved
            if (moved < shift) {
                // The sink took less than offered: it is full.
                sink.remaining = 0.0
            }
            if (source.remaining <= 0) source = sources.nextOrNull()
            if (sink.remaining <= 0) sink = sinks.nextOrNull()
        }
        return Result(fromProducers, fromStorage, toConsumers, toStorage)
    }

    private fun <T> Iterator<T>.nextOrNull(): T? = if (hasNext()) next() else null
}

/**
 * Minimal spanning forest over a network's connections, by distance. Port of v3's `MinimalSpanningTree`, used to pick
 * which node pairs draw power beams.
 *
 * v3 ran a Prim-style walk that could revisit nodes and threw on a network whose connection map missed a node; this is
 * Kruskal's algorithm with a union-find, which handles any graph (disconnected parts get their own trees).
 */
object MinimalSpanningTree {
    fun calculate(connections: Map<Loc4, Set<Loc4>>): Map<Loc4, Set<Loc4>> {
        val edges = connections.flatMap { (a, bs) -> bs.filter { a < it }.map { a to it } }
            .sortedWith(compareBy<Pair<Loc4, Loc4>> { it.first.distSqr(it.second) }.thenBy { it.first }.thenBy { it.second })
        val parent = HashMap<Loc4, Loc4>()
        fun find(x: Loc4): Loc4 {
            var root = x
            while (true) {
                val p = parent[root] ?: return root
                if (p == root) return root
                root = p
            }
        }
        val tree = HashMap<Loc4, MutableSet<Loc4>>()
        for ((a, b) in edges) {
            val ra = find(a)
            val rb = find(b)
            if (ra == rb) continue
            parent[ra] = rb
            tree.getOrPut(a, ::HashSet).add(b)
            tree.getOrPut(b, ::HashSet).add(a)
        }
        return tree
    }
}
