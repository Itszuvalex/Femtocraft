package com.itszuvalex.femtocraft.power

import com.itszuvalex.itszulib.api.utility.Loc4

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
