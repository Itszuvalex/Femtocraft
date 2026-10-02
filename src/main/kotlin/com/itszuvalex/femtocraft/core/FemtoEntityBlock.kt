package com.itszuvalex.femtocraft.core

import com.itszuvalex.itszulib.api.adapters.ILevel
import com.itszuvalex.itszulib.core.EntityBlockCore
import com.itszuvalex.itszulib.core.HorizontalFacing
import com.itszuvalex.itszulib.core.IBlockEntityTickable
import com.itszuvalex.itszulib.api.utility.DirectionUtil
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.phys.BlockHitResult

/**
 * Block with a [FemtoBlockEntity]: creates it, ticks it on both sides, opens its menu (ItszuLib `FragMenu`) or forwards
 * activation, and forwards placement. Removal side effects go through the block entity's fragments
 * (`BlockEntityCore.preRemoveSideEffects`).
 */
open class FemtoEntityBlock<T : FemtoBlockEntity>(
    properties: BlockBehaviour.Properties,
    typeSupplier: () -> BlockEntityType<T>,
) : EntityBlockCore<T>(properties, typeSupplier) {

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity? = typeSupplier().create(pos, state)

    override fun <E : BlockEntity> getTicker(level: Level, state: BlockState, type: BlockEntityType<E>): BlockEntityTicker<E>? {
        if (type !== typeSupplier()) return null
        return BlockEntityTicker { lvl, pos, st, be -> (be as IBlockEntityTickable).tick(ILevel.of(lvl), pos, st) }
    }

    override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hitResult: BlockHitResult): InteractionResult {
        val opened = super.useWithoutItem(state, level, pos, player, hitResult)
        if (opened.consumesAction()) return opened
        val be = level.getBlockEntity(pos) as? FemtoBlockEntity ?: return InteractionResult.PASS
        return be.onUse(player)
    }

    override fun setPlacedBy(level: Level, pos: BlockPos, state: BlockState, by: LivingEntity?, itemStack: ItemStack) {
        super.setPlacedBy(level, pos, state, by, itemStack)
        (level.getBlockEntity(pos) as? FemtoBlockEntity)?.onPlaced(by, itemStack)
    }

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL
}

/**
 * A [FemtoEntityBlock] with a horizontal `facing` that faces the placing player (v3's `BlockBehaviors.FACING_HORIZONTAL`).
 */
open class FemtoHorizontalEntityBlock<T : FemtoBlockEntity>(
    properties: BlockBehaviour.Properties,
    typeSupplier: () -> BlockEntityType<T>,
) : FemtoEntityBlock<T>(properties, typeSupplier) {
    init {
        registerDefaultState(stateDefinition.any().setValue(HorizontalFacing.FACING, DirectionUtil.DEFAULT_HORIZONTAL_FACING))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(HorizontalFacing.FACING)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState? = HorizontalFacing.placementState(defaultBlockState(), context)

    override fun rotate(state: BlockState, rotation: Rotation): BlockState = HorizontalFacing.rotate(state, rotation)

    override fun mirror(state: BlockState, mirror: Mirror): BlockState = HorizontalFacing.mirror(state, mirror)
}
