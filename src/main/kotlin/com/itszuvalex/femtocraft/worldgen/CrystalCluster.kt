package com.itszuvalex.femtocraft.worldgen

import com.itszuvalex.femtocraft.core.FemtoBlockEntity
import com.itszuvalex.femtocraft.core.FemtoEntityBlock
import com.itszuvalex.femtocraft.core.FragData
import com.itszuvalex.femtocraft.industry.IndustryContent
import com.itszuvalex.femtocraft.power.PowerContent
import com.itszuvalex.femtocraft.power.PowerCrystals
import com.itszuvalex.itszulib.api.adapters.ILevel
import com.itszuvalex.itszulib.api.utility.NBTSerializationScope
import com.itszuvalex.itszulib.core.frag.InternalBlockEntityFragment
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import net.minecraft.world.Containers
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

/**
 * A crystal cluster from rift worldgen. Breaking it drops 2-7 random power crystals of its color and 3-5 crackling
 * dust. Port of v3's `BlockCrystalsWorldgen`/`TileCrystalsWorldgen` (the per-crystal color offsets only fed the
 * renderer, which is follow-up work).
 */
class CrystalClusterBlockEntity(pos: BlockPos, state: BlockState) : FemtoBlockEntity(WorldgenContent.CRYSTAL_CLUSTER_BE.get(), pos, state) {
    /**
     * v3: opaque, each channel 115-239.
     */
    @JvmField
    var color: Int = randomColor(RandomSource.create())

    init {
        fragList.addInternalFragment(FragData("Color", setOf(NBTSerializationScope.LEVEL, NBTSerializationScope.DESCRIPTION), { _, o -> o.putInt(COLOR_KEY, color) }, { _, i -> color = i.getIntOr(COLOR_KEY, color) }))
        fragList.addInternalFragment(object : InternalBlockEntityFragment() {
            override fun name(): String = "Drops"
            override fun onRemove(level: ILevel, pos: BlockPos, blockStatePrev: BlockState) = drops(level.toMinecraft(), pos)
        })
    }

    private fun drops(level: Level, pos: BlockPos) {
        val random = level.random
        for (stack in rollDrops(random, color)) Containers.dropItemStack(level, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), stack)
    }

    companion object {
        const val COLOR_KEY = "Color"

        fun randomColor(random: RandomSource): Int =
            (0xFF shl 24) or ((115 + random.nextInt(125)) shl 16) or ((115 + random.nextInt(125)) shl 8) or (115 + random.nextInt(125))

        const val CRYSTALS_MIN = 2
        const val CRYSTALS_MAX = 7
        const val SMALL_WEIGHT = 10
        const val MEDIUM_WEIGHT = 5
        const val LARGE_WEIGHT = 2
        const val PASSIVE_GEN_MIN = 0f
        const val PASSIVE_GEN_MAX = 1f
        const val STORAGE_MIN = 1000.0
        const val STORAGE_MAX = 5000.0
        const val TRANSFER_MIN = 50
        const val TRANSFER_MAX = 500
        const val DUST_MIN = 3
        const val DUST_MAX = 5

        /**
         * v3's `breakBlock` drops.
         */
        fun rollDrops(random: RandomSource, color: Int): List<ItemStack> {
            val drops = ArrayList<ItemStack>()
            repeat(random.nextIntBetweenInclusive(CRYSTALS_MIN, CRYSTALS_MAX)) {
                val t = random.nextInt(SMALL_WEIGHT + MEDIUM_WEIGHT + LARGE_WEIGHT)
                val type = when {
                    t < SMALL_WEIGHT -> PowerCrystals.TYPE_SMALL
                    t < SMALL_WEIGHT + MEDIUM_WEIGHT -> PowerCrystals.TYPE_MEDIUM
                    else -> PowerCrystals.TYPE_LARGE
                }
                val passiveGen = random.nextFloat() * (PASSIVE_GEN_MAX - PASSIVE_GEN_MIN) + PASSIVE_GEN_MIN
                val storage = Math.floor(random.nextDouble() * (STORAGE_MAX - STORAGE_MIN)) + STORAGE_MIN
                val transfer = random.nextIntBetweenInclusive(TRANSFER_MIN, TRANSFER_MAX).toDouble()
                drops += PowerCrystals.initialize(ItemStack(PowerContent.POWER_CRYSTAL.get()), "Power Crystal", type, color, storage, passiveGen, transfer)
            }
            drops += ItemStack(IndustryContent.CRACKLING_DUST.get(), random.nextIntBetweenInclusive(DUST_MIN, DUST_MAX))
            return drops
        }
    }
}

class CrystalClusterBlock(properties: BlockBehaviour.Properties) : FemtoEntityBlock<CrystalClusterBlockEntity>(properties, { WorldgenContent.CRYSTAL_CLUSTER_BE.get() }) {
    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = SHAPE

    companion object {
        private val SHAPE: VoxelShape = box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0)
    }
}
