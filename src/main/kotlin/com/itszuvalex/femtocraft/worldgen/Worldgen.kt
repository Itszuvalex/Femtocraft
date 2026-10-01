package com.itszuvalex.femtocraft.worldgen

import com.itszuvalex.femtocraft.FemtoBlockEntities
import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.FemtoItems
import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.core.FemtoBlockEntity
import com.itszuvalex.femtocraft.core.FemtoEntityBlock
import com.itszuvalex.femtocraft.core.FragData
import com.itszuvalex.femtocraft.cyber.CybermaterialRegistry
import com.itszuvalex.femtocraft.power.item.CrystalData
import com.itszuvalex.femtocraft.power.item.PowerCrystalItem
import com.itszuvalex.itszulib.api.adapters.ILevel
import com.itszuvalex.itszulib.core.frag.InternalBlockEntityFragment
import com.itszuvalex.itszulib.util.Color
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.util.RandomSource
import net.minecraft.world.Containers
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import kotlin.random.Random

object FemtoWorldgen {
    @JvmField
    val FEATURES: DeferredRegister<Feature<*>> = DeferredRegister.create(Registries.FEATURE, Femtocraft.ID)

    @JvmField
    val CRYSTAL_CLUSTER: DeferredHolder<Feature<*>, CrystalClusterFeature> = FEATURES.register("crystal_cluster") { -> CrystalClusterFeature() }

    fun register(bus: IEventBus) = FEATURES.register(bus)
}

/**
 * Cyberizes a sphere of terrain and sprinkles power crystal clusters into it. Port of 1.7.10
 * `FemtocraftOreGenerator`; how often it runs (1.7.10 `CHANCE_PER_CHUNK = .015`) is the placed feature's
 * `rarity_filter` (`data/femtocraft/worldgen/placed_feature/crystal_cluster.json`).
 */
class CrystalClusterFeature : Feature<NoneFeatureConfiguration>(NoneFeatureConfiguration.CODEC) {
    enum class Size(val weight: Int, val distMin: Int, val distMax: Int, val crystMin: Int, val crystMax: Int) {
        SMALL(50, 6, 10, 1, 4),
        MEDIUM(30, 12, 16, 3, 8),
        LARGE(20, 20, 32, 6, 12),
    }

    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val random = context.random()
        val origin = context.origin()
        var y = random.nextInt(Y_MAX - Y_MIN) + Y_MIN
        while (y > level.minY && level.isEmptyBlock(BlockPos(origin.x, y, origin.z))) y--
        val center = BlockPos(origin.x, y, origin.z)

        val size = pickSize(random.nextInt(Size.entries.sumOf { it.weight }))
        // Features may only write within one chunk of their origin chunk; larger 1.7.10 spheres are capped.
        val dist = minOf(random.nextInt(size.distMax - size.distMin) + size.distMin, MAX_RADIUS)
        val cryst = random.nextInt(size.crystMax - size.crystMin) + size.crystMin

        // Replace in sphere
        val mutable = BlockPos.MutableBlockPos()
        for (lx in -dist..dist) for (ly in -dist..dist) for (lz in -dist..dist) {
            if (lx * lx + ly * ly + lz * lz >= dist * dist) continue
            mutable.set(center.x + lx, center.y + ly, center.z + lz)
            if (!level.ensureCanWrite(mutable)) continue
            val state = level.getBlockState(mutable)
            if (state.isAir) continue
            CybermaterialRegistry.getReplacement(state)?.let { level.setBlock(mutable, it, Block.UPDATE_CLIENTS) }
        }

        // Sprinkle in crystals
        repeat(cryst) {
            val c = center.offset(
                random.nextInt(2 * CRYSTAL_SPAWN_DIST_MAX) - CRYSTAL_SPAWN_DIST_MAX,
                random.nextInt(2 * CRYSTAL_SPAWN_DIST_MAX) - CRYSTAL_SPAWN_DIST_MAX,
                random.nextInt(2 * CRYSTAL_SPAWN_DIST_MAX) - CRYSTAL_SPAWN_DIST_MAX,
            )
            if (level.ensureCanWrite(c)) level.setBlock(c, FemtoBlocks.CRYSTAL_CLUSTER.get().defaultBlockState(), Block.UPDATE_CLIENTS)
        }
        return true
    }

    companion object {
        const val Y_MIN = 20
        const val Y_MAX = 100
        const val CRYSTAL_SPAWN_DIST_MAX = 5

        /**
         * Largest sphere radius that stays inside the 3x3 chunks a feature may write to.
         */
        const val MAX_RADIUS = 16

        /**
         * Weighted pick of a cluster size. [roll] is uniform in [0, total weight).
         */
        @JvmStatic
        fun pickSize(roll: Int): Size = when {
            roll < Size.SMALL.weight -> Size.SMALL
            roll < Size.MEDIUM.weight -> Size.MEDIUM
            else -> Size.LARGE
        }
    }
}

/**
 * A glowing crystal cluster; breaking it drops 2-6 random power crystals of its color. Port of 1.7.10
 * `BlockCrystalsWorldgen`.
 */
class CrystalClusterBlock(properties: BlockBehaviour.Properties) :
    FemtoEntityBlock<CrystalClusterBlockEntity>(properties, { FemtoBlockEntities.CRYSTAL_CLUSTER.get() }) {
    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = SHAPE

    companion object {
        private val SHAPE: VoxelShape = Block.box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0)
    }
}

class CrystalClusterBlockEntity(pos: BlockPos, state: BlockState) :
    FemtoBlockEntity(FemtoBlockEntities.CRYSTAL_CLUSTER.get(), pos, state) {
    var color: Int = Color(255.toByte(), (Random.nextInt(125) + 115).toByte(), (Random.nextInt(125) + 115).toByte(), (Random.nextInt(125) + 115).toByte()).toInt()

    init {
        fragList.addInternalFragment(FragData("ColorSettings", FragData.LEVEL_AND_DESCRIPTION, { _, out ->
            out.child(COLOR_COMPOUND_KEY).putInt(COLOR_KEY, color)
        }, { _, input ->
            input.child(COLOR_COMPOUND_KEY).ifPresent { color = it.getIntOr(COLOR_KEY, color) }
        }))
        fragList.addInternalFragment(object : InternalBlockEntityFragment() {
            override fun name(): String = "Drops"
            override fun onRemove(level: ILevel, pos: BlockPos, blockStatePrev: BlockState) {
                val lvl = level.toMinecraft()
                dropCrystals(lvl.random, color).forEach { Containers.dropItemStack(lvl, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), it) }
            }
        })
    }

    companion object {
        const val COLOR_KEY = "Color"
        const val COLOR_COMPOUND_KEY = "ColorSettings"

        const val DROP_CRYSTALS_MIN = 2
        const val DROP_CRYSTALS_MAX = 7
        const val DROP_SMALL_WEIGHT = 10
        const val DROP_MEDIUM_WEIGHT = 5
        const val DROP_LARGE_WEIGHT = 2
        const val DROP_PASSIVE_GEN_MIN = 0f
        const val DROP_PASSIVE_GEN_MAX = 1f
        const val DROP_STORAGE_MAX_MIN = 1000L
        const val DROP_STORAGE_MAX_MAX = 5000L
        const val DROP_TRANSFER_MIN = 50
        const val DROP_TRANSFER_MAX = 500

        /**
         * The crystals a cluster of [color] drops. Port of `BlockCrystalsWorldgen.breakBlock`.
         */
        @JvmStatic
        fun dropCrystals(random: RandomSource, color: Int): List<ItemStack> =
            (0 until random.nextInt(DROP_CRYSTALS_MAX - DROP_CRYSTALS_MIN) + DROP_CRYSTALS_MIN).map {
                val roll = random.nextInt(DROP_SMALL_WEIGHT + DROP_MEDIUM_WEIGHT + DROP_LARGE_WEIGHT)
                val type = when {
                    roll < DROP_SMALL_WEIGHT -> CrystalData.TYPE_SMALL
                    roll < DROP_MEDIUM_WEIGHT + DROP_SMALL_WEIGHT -> CrystalData.TYPE_MEDIUM
                    else -> CrystalData.TYPE_LARGE
                }
                val passive = random.nextFloat() * (DROP_PASSIVE_GEN_MAX - DROP_PASSIVE_GEN_MIN) + DROP_PASSIVE_GEN_MIN
                val storage = (random.nextDouble() * (DROP_STORAGE_MAX_MAX - DROP_STORAGE_MAX_MIN)).toLong() + DROP_STORAGE_MAX_MIN
                val transfer = random.nextInt(DROP_TRANSFER_MAX - DROP_TRANSFER_MIN) + DROP_TRANSFER_MIN
                PowerCrystalItem.initialize(ItemStack(FemtoItems.POWER_CRYSTAL.get()), "Power Crystal", type, color, storage.toDouble(), passive, transfer)
            }
    }
}
