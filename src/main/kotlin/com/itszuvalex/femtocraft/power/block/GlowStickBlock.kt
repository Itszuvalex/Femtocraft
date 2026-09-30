package com.itszuvalex.femtocraft.power.block

import com.itszuvalex.femtocraft.FemtoBlockEntities
import com.itszuvalex.femtocraft.core.FemtoEntityBlock
import com.itszuvalex.femtocraft.power.tile.GlowStickBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

/**
 * Thin post of light, no collision. Port of 1.7.10 `BlockGlowStick`.
 */
class GlowStickBlock(properties: BlockBehaviour.Properties) :
    FemtoEntityBlock<GlowStickBlockEntity>(properties, { FemtoBlockEntities.GLOW_STICK.get() }) {
    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = SHAPE

    companion object {
        private val SHAPE: VoxelShape = Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0)
    }
}
