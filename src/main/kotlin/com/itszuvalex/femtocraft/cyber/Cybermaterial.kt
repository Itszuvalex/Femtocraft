package com.itszuvalex.femtocraft.cyber

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.FemtoTags
import net.minecraft.core.BlockPos
import net.minecraft.core.particles.DustParticleOptions
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.block.state.BlockState

/**
 * Which blocks "cyberize" into which: logs -> cyberwood, leaves -> cyberleaf, stone/grass/dirt -> cyberweave. Port of
 * 1.7.10 `CybermaterialRegistry.getReplacement`, driven by block tags (`femtocraft:converts_to_*`) instead of the ore
 * dictionary and hard-coded blocks.
 */
object CybermaterialRegistry {
    /**
     * @return The replacement state, or null if [state] does not convert (or already is its replacement).
     */
    fun getReplacement(state: BlockState): BlockState? {
        val target = when {
            state.`is`(FemtoTags.Blocks.CONVERTS_TO_CYBERWOOD) -> FemtoBlocks.CYBERWOOD.get()
            state.`is`(FemtoTags.Blocks.CONVERTS_TO_CYBERLEAF) -> FemtoBlocks.CYBERLEAF.get()
            state.`is`(FemtoTags.Blocks.CONVERTS_TO_CYBERWEAVE) -> FemtoBlocks.CYBERWEAVE.get()
            else -> return null
        }
        if (state.`is`(target)) return null
        return target.defaultBlockState()
    }
}

/**
 * Converts the block it is used on into its cybermaterial. Port of 1.7.10 `ItemDumbDust`.
 */
class DumbDustItem(properties: Properties) : Item(properties) {
    override fun useOn(context: UseOnContext): InteractionResult {
        val level = context.level
        val pos: BlockPos = context.clickedPos
        val state = level.getBlockState(pos)
        if (state.isAir) return InteractionResult.PASS
        val replacement = CybermaterialRegistry.getReplacement(state) ?: return InteractionResult.PASS
        if (level is ServerLevel) {
            level.setBlockAndUpdate(pos, replacement)
            context.itemInHand.shrink(1)
            level.sendParticles(DustParticleOptions(0x80FFA0, 1f), pos.x + .5, pos.y + 1.0, pos.z + .5, 4, .3, .3, .3, 0.0)
        }
        return InteractionResult.SUCCESS
    }
}
