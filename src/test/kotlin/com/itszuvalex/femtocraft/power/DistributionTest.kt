package com.itszuvalex.femtocraft.power

import com.itszuvalex.itszulib.api.utility.Loc4
import net.minecraft.core.BlockPos
import net.minecraft.resources.Identifier
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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
