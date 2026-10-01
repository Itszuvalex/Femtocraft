package com.itszuvalex.femtocraft.worldgen

import com.itszuvalex.femtocraft.FemtoRegistries
import com.itszuvalex.femtocraft.FemtoSounds
import net.minecraft.sounds.SoundEvents
import net.neoforged.neoforge.common.util.DeferredSoundType
import java.util.function.UnaryOperator

/**
 * Worldgen registrations: the crystal cluster and the rift feature. The feature is configured, placed and added to
 * overworld biomes by data files (`worldgen/configured_feature/rift.json`, `worldgen/placed_feature/rift.json`,
 * `neoforge/biome_modifier/rift.json`).
 */
object WorldgenContent {
    private val R = FemtoRegistries

    private val CRYSTAL_SOUND = DeferredSoundType(1f, 1f, FemtoSounds.CRYSTAL_BREAK, { SoundEvents.GLASS_STEP }, FemtoSounds.CRYSTAL_BREAK, { SoundEvents.GLASS_HIT }, { SoundEvents.GLASS_FALL })

    @JvmField val CRYSTAL_CLUSTER = R.BLOCKS.registerBlock("crystal_cluster", ::CrystalClusterBlock, UnaryOperator { it.strength(.5f).sound(CRYSTAL_SOUND).noOcclusion().lightLevel { 7 } })
    @JvmField val CRYSTAL_CLUSTER_ITEM = R.ITEMS.registerSimpleBlockItem("crystal_cluster", CRYSTAL_CLUSTER)
    @JvmField val CRYSTAL_CLUSTER_BE = R.blockEntity("crystal_cluster", ::CrystalClusterBlockEntity, CRYSTAL_CLUSTER::get)

    @JvmField val RIFT = R.FEATURES.register("rift") { -> RiftFeature() }

    fun init() {}
}
