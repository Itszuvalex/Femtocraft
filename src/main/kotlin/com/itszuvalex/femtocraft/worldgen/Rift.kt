package com.itszuvalex.femtocraft.worldgen

import com.itszuvalex.femtocraft.cyber.Cybermaterials
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import net.minecraft.world.level.WorldGenLevel
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration

/**
 * A crystal rift: a cylinder of terrain, top to bottom, turned into cybermaterials, with crystal clusters sprinkled
 * near its center. Port of v3's `FemtocraftOreGenerator`.
 */
object Rift {
    /** v3 rolled once per chunk at this chance; the placed feature's rarity filter is 1 in 333. */
    const val CHANCE_PER_CHUNK = .003

    const val SMALL_WEIGHT = 50
    const val MEDIUM_WEIGHT = 30
    const val LARGE_WEIGHT = 20

    /**
     * A feature may only write within the 3x3 chunks around its own, so a rift centered in its chunk reaches at most
     * 23 blocks out (DECISIONS D13). v3's radii went up to 40.
     */
    const val MAX_RADIUS = 24

    const val CRYSTAL_SPAWN_DIST_MAX = 5

    /**
     * Radius range and crystal count range of a rift size.
     */
    data class Size(val distMin: Int, val distMax: Int, val crystalsMin: Int, val crystalsMax: Int) {
        fun roll(random: RandomSource): Pair<Int, Int> =
            minOf(random.nextIntBetweenInclusive(distMin, distMax), MAX_RADIUS) to random.nextIntBetweenInclusive(crystalsMin, crystalsMax)
    }

    @JvmField val SMALL = Size(10, 15, 8, 10)
    @JvmField val MEDIUM = Size(15, 25, 12, 16)
    @JvmField val LARGE = Size(25, 40, 20, 24)

    /**
     * Picks a size by weight. v3 compared the roll with the medium weight alone (`rand < MEDIUM_WEIGHT` after
     * `rand < SMALL_WEIGHT` failed), which can never be true, so medium rifts never spawned.
     */
    fun pickSize(roll: Int): Size = when {
        roll < SMALL_WEIGHT -> SMALL
        roll < SMALL_WEIGHT + MEDIUM_WEIGHT -> MEDIUM
        else -> LARGE
    }

    fun pickSize(random: RandomSource): Size = pickSize(random.nextInt(SMALL_WEIGHT + MEDIUM_WEIGHT + LARGE_WEIGHT))

    /**
     * Converts every replaceable block strictly within [radius] of [center] (horizontally), from [minY] to [maxY].
     *
     * @return Blocks converted.
     */
    fun convert(level: WorldGenLevel, center: BlockPos, radius: Int, minY: Int, maxY: Int): Int {
        var converted = 0
        val pos = BlockPos.MutableBlockPos()
        for (dx in -radius..radius) for (dz in -radius..radius) {
            if (dx * dx + dz * dz >= radius * radius) continue
            for (y in minY..maxY) {
                pos.set(center.x + dx, y, center.z + dz)
                val state = level.getBlockState(pos)
                if (state.isAir) continue
                val replacement = Cybermaterials.replacement(state) ?: continue
                level.setBlock(pos, replacement, Block.UPDATE_CLIENTS)
                converted++
            }
        }
        return converted
    }

    /**
     * Places [count] crystal clusters within [CRYSTAL_SPAWN_DIST_MAX] of [center], each resting on the first solid block
     * below a random height up to y 100. v3 placed them at that height even inside solid ground, overwriting the
     * block; here a cluster that starts inside the ground climbs to the air above it first.
     */
    fun sprinkleCrystals(level: WorldGenLevel, center: BlockPos, count: Int, random: RandomSource, minY: Int, maxY: Int): Int {
        var placed = 0
        val top = minOf(100, maxY)
        repeat(count) {
            val x = center.x + random.nextInt(2 * CRYSTAL_SPAWN_DIST_MAX) - CRYSTAL_SPAWN_DIST_MAX
            val z = center.z + random.nextInt(2 * CRYSTAL_SPAWN_DIST_MAX) - CRYSTAL_SPAWN_DIST_MAX
            val at = landingSpot(level, x, z, random.nextIntBetweenInclusive(minY + 1, top), minY, maxY) ?: return@repeat
            level.setBlock(at, WorldgenContent.CRYSTAL_CLUSTER.get().defaultBlockState(), Block.UPDATE_CLIENTS)
            placed++
        }
        return placed
    }

    /**
     * The air block at or above [startY] resting on solid ground, or null if the column has none.
     */
    fun landingSpot(level: WorldGenLevel, x: Int, z: Int, startY: Int, minY: Int, maxY: Int): BlockPos? {
        val pos = BlockPos.MutableBlockPos(x, startY, z)
        while (pos.y <= maxY && !level.getBlockState(pos).isAir) pos.move(0, 1, 0)
        if (pos.y > maxY) return null
        while (pos.y > minY + 1 && level.getBlockState(pos.below()).isAir) pos.move(0, -1, 0)
        return if (level.getBlockState(pos.below()).isAir) null else pos.immutable()
    }
}

/**
 * The rift as a world feature. Placed once per chunk that passes the rarity filter (`placed_feature/rift.json`), it
 * centers itself in the chunk so the cylinder stays within the region a feature may write to.
 */
class RiftFeature : Feature<NoneFeatureConfiguration>(NoneFeatureConfiguration.CODEC) {
    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val random = context.random()
        val origin = context.origin()
        val center = BlockPos((origin.x and 15.inv()) + 8, 0, (origin.z and 15.inv()) + 8)
        val (radius, crystals) = Rift.pickSize(random).roll(random)
        val minY = level.minY + 1
        val maxY = level.maxY
        Rift.convert(level, center, radius, minY, maxY)
        Rift.sprinkleCrystals(level, center, crystals, random, minY, maxY)
        return true
    }
}
