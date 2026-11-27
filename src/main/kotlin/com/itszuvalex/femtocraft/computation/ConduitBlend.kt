package com.itszuvalex.femtocraft.computation

/**
 * The colour a computation conduit's circuit layer takes (DECISIONS D31), worked out on the client each tick: the
 * average colour of the blocks it is attached to, or, attached to none, the average of its neighbouring conduits'
 * colours, so colours spread along a line and blend where they meet. [hops] counts conduits back to a coloured
 * block; a conduit only takes colour from neighbours within [MAX_HOPS], so a line cut off from every coloured block
 * loses its colour again (its hops climb past [MAX_HOPS] a tick at a time) instead of keeping it forever. Pure, for unit
 * tests; [ComputationConduitBlockEntity] feeds it.
 *
 * @param color Opaque ARGB.
 */
data class ConduitBlend(val color: Int, val hops: Int) {
    companion object {
        const val MAX_HOPS = 32

        /**
         * The next blend of a conduit attached to blocks coloured [attached], beside conduits blended [neighbours] (null
         * where a neighbour has no colour). Null for no colour.
         */
        @JvmStatic
        fun next(attached: List<Int>, neighbours: List<ConduitBlend?>): ConduitBlend? {
            if (attached.isNotEmpty()) return ConduitBlend(average(attached), 0)
            val near = neighbours.filterNotNull().filter { it.hops < MAX_HOPS }
            if (near.isEmpty()) return null
            return ConduitBlend(average(near.map { it.color }), near.minOf { it.hops } + 1)
        }

        /** The average of opaque ARGB colours, channel by channel (opaque black for none). */
        @JvmStatic
        fun average(colors: List<Int>): Int {
            if (colors.isEmpty()) return 0xFF000000.toInt()
            fun channel(shift: Int) = (colors.sumOf { (it ushr shift) and 0xFF } + colors.size / 2) / colors.size
            return (0xFF shl 24) or (channel(16) shl 16) or (channel(8) shl 8) or channel(0)
        }
    }
}
