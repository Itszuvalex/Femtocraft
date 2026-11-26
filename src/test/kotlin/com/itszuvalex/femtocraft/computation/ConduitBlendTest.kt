package com.itszuvalex.femtocraft.computation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ConduitBlendTest {
    private val red = 0xFFFF0000.toInt()
    private val blue = 0xFF0000FF.toInt()

    @Test
    fun Average_ChannelByChannel() {
        assertEquals(0xFF800080.toInt(), ConduitBlend.average(listOf(red, blue)))
        assertEquals(red, ConduitBlend.average(listOf(red)))
    }

    @Test
    fun Next_AttachedBlocksWinOverNeighbours() {
        assertEquals(ConduitBlend(red, 0), ConduitBlend.next(listOf(red), listOf(ConduitBlend(blue, 0))))
    }

    @Test
    fun Next_UnattachedTakesNeighboursOneHopFurther() {
        assertEquals(ConduitBlend(0xFF800080.toInt(), 1), ConduitBlend.next(emptyList(), listOf(ConduitBlend(red, 0), ConduitBlend(blue, 3), null)))
        assertNull(ConduitBlend.next(emptyList(), listOf(null)))
    }

    /** A line of conduits with red at one end and blue at the other settles with red and blue at the ends, blending between. */
    @Test
    fun Line_SpreadsAndBlends() {
        val n = 5
        var line = arrayOfNulls<ConduitBlend>(n)
        repeat(50) {
            line = Array(n) { i ->
                when (i) {
                    0 -> ConduitBlend.next(listOf(red), emptyList())
                    n - 1 -> ConduitBlend.next(listOf(blue), emptyList())
                    else -> ConduitBlend.next(emptyList(), listOf(line[i - 1], line[i + 1]))
                }
            }
        }
        val middle = line[2]!!.color
        assertEquals(red, line[0]!!.color)
        assertEquals(blue, line[n - 1]!!.color)
        assertEquals(2, line[2]!!.hops)
        assertEquals(((middle ushr 16) and 0xFF).toDouble(), (middle and 0xFF).toDouble(), 2.0)
    }

    /** A loop of conduits cut off from every coloured block loses its colour. */
    @Test
    fun CutOffLoop_LosesColour() {
        val n = 4
        var loop = Array<ConduitBlend?>(n) { ConduitBlend(red, 0) }
        repeat(ConduitBlend.MAX_HOPS + 2) {
            loop = Array(n) { i -> ConduitBlend.next(emptyList(), listOf(loop[(i + n - 1) % n], loop[(i + 1) % n])) }
        }
        loop.forEach { assertNull(it) }
    }
}
