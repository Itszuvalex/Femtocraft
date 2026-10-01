package com.itszuvalex.femtocraft.core

import com.itszuvalex.itszulib.api.adapters.ILevel
import com.itszuvalex.itszulib.core.EntityBlockCore
import com.itszuvalex.itszulib.core.IBlockEntityTickable
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

/**
 * Block with a [FemtoBlockEntity]: creates it, ticks it on both sides and forwards activation and placement to it.
 * Removal side effects go through the block entity's fragments (`BlockEntityCore.preRemoveSideEffects`).
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
        val be = level.getBlockEntity(pos) as? FemtoBlockEntity ?: return InteractionResult.PASS
        return be.onUse(player)
    }

    override fun setPlacedBy(level: Level, pos: BlockPos, state: BlockState, by: LivingEntity?, itemStack: ItemStack) {
        super.setPlacedBy(level, pos, state, by, itemStack)
        (level.getBlockEntity(pos) as? FemtoBlockEntity)?.onPlaced(by, itemStack)
    }

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL
}
