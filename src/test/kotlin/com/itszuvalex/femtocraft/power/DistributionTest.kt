package com.itszuvalex.femtocraft.power

import com.itszuvalex.itszulib.api.storage.PowerBattery
import com.itszuvalex.itszulib.api.utility.Loc4
import net.minecraft.core.BlockPos
import net.minecraft.resources.Identifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

private fun battery(max: Double, stored: Double) = PowerBattery(max).also { it.setStorage(stored) }

private fun res(b: PowerBattery, rate: Double = 1000.0) = DistributableBattery(b) { rate }

class DistributionAlgorithmTest {
    @Test
    fun Distribute_ProducerToConsumer_RespectsTransferRates() {
        val producer = battery(1000.0, 500.0)
        val consumer = battery(1000.0, 0.0)
        val result = DistributionAlgorithm(listOf(res(producer, 50.0)), listOf(), listOf(res(consumer, 30.0))).distribute()
        assertEquals(30.0, consumer.storage())
        assertEquals(470.0, producer.storage())
        assertEquals(30.0, result.toConsumers)
        assertEquals(30.0, result.fromProducers)
    }

    @Test
    fun Distribute_Surplus_GoesToStorage() {
        val producer = battery(1000.0, 500.0)
        val storage = battery(1000.0, 0.0)
        val consumer = battery(1000.0, 990.0)
        DistributionAlgorithm(listOf(res(producer, 100.0)), listOf(res(storage)), listOf(res(consumer))).distribute()
        assertEquals(1000.0, consumer.storage())
        assertEquals(90.0, storage.storage())
        assertEquals(400.0, producer.storage())
    }

    @Test
    fun Distribute_Shortfall_StorageTopsUpConsumers() {
        val producer = battery(1000.0, 10.0)
        val storage = battery(1000.0, 500.0)
        val consumer = battery(1000.0, 0.0)
        DistributionAlgorithm(listOf(res(producer)), listOf(res(storage)), listOf(res(consumer, 100.0))).distribute()
        assertEquals(100.0, consumer.storage())
        assertEquals(0.0, producer.storage())
        assertEquals(410.0, storage.storage())
    }

    @Test
    fun Distribute_StorageOnly_DoesNotShuffleBetweenStorages() {
        val a = battery(1000.0, 500.0)
        val b = battery(1000.0, 0.0)
        val result = DistributionAlgorithm(listOf(), listOf(res(a), res(b)), listOf()).distribute()
        assertEquals(0.0, result.total)
        assertEquals(500.0, a.storage())
    }

    /**
     * v3 removed from the source before adding to the sink and ignored what the sink accepted.
     */
    @Test
    fun Distribute_SinkAcceptsLess_SourceKeepsTheRest() {
        val producer = battery(1000.0, 500.0)
        val stingy = object : DistributableResource {
            var got = 0.0
            override val max = 100.0
            override val amount get() = 0.0
            override val transferMax = 100.0
            override fun add(amount: Double): Double = minOf(amount, 10.0).also { got += it }
            override fun remove(amount: Double): Double = 0.0
        }
        DistributionAlgorithm(listOf(res(producer)), listOf(), listOf(stingy)).distribute()
        assertEquals(10.0, stingy.got)
        assertEquals(490.0, producer.storage())
    }

    @Test
    fun Distribute_NothingToGive_ReturnsNone() {
        val result = DistributionAlgorithm(listOf(res(battery(100.0, 0.0))), listOf(), listOf(res(battery(100.0, 0.0)))).distribute()
        assertEquals(DistributionAlgorithm.Result.NONE, result)
    }
}

class MinimalSpanningTreeTest {
    private val dim = Identifier.parse("test")
    private fun l(x: Int) = Loc4.of(dim, BlockPos(x, 0, 0))

    private fun graph(vararg edges: Pair<Loc4, Loc4>): Map<Loc4, Set<Loc4>> {
        val m = HashMap<Loc4, MutableSet<Loc4>>()
        edges.forEach { (a, b) ->
            m.getOrPut(a, ::HashSet).add(b)
            m.getOrPut(b, ::HashSet).add(a)
        }
        return m
    }

    @Test
    fun Calculate_Triangle_DropsLongestEdge() {
        val tree = MinimalSpanningTree.calculate(graph(l(0) to l(1), l(1) to l(3), l(0) to l(3)))
        assertEquals(setOf(l(1)), tree[l(0)])
        assertEquals(setOf(l(0), l(3)), tree[l(1)])
        assertEquals(setOf(l(1)), tree[l(3)])
    }

    /**
     * v3 crashed (`.get` on an empty find) on graphs its walk could not finish, e.g. disconnected parts.
     */
    @Test
    fun Calculate_Disconnected_GivesForest() {
        val tree = MinimalSpanningTree.calculate(graph(l(0) to l(1), l(10) to l(11)))
        assertEquals(setOf(l(1)), tree[l(0)])
        assertEquals(setOf(l(11)), tree[l(10)])
        assertTrue(tree[l(0)]!!.none { it == l(10) })
    }

    @Test
    fun Calculate_Empty_GivesEmpty() {
        assertTrue(MinimalSpanningTree.calculate(emptyMap()).isEmpty())
    }
}
