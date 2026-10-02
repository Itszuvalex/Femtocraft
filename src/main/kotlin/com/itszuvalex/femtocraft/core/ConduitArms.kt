package com.itszuvalex.femtocraft.core

import net.minecraft.core.Direction
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.PipeBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

/**
 * The arms of a conduit's model: one boolean block state property per face (vanilla's `north`...`down`), true where the
 * conduit connects. The block entity owns the connections; [sync] copies them into the block state from its server
 * tick (never during chunk loading), so the multipart blockstate draws the arms and the shape follows them.
 */
object ConduitArms {
    @JvmField
    val PROPERTIES: Map<Direction, BooleanProperty> = PipeBlock.PROPERTY_BY_DIRECTION

    fun addProperties(builder: StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState>) {
        PROPERTIES.values.forEach { builder.add(it) }
    }

    fun withoutArms(state: BlockState): BlockState = PROPERTIES.values.fold(state) { s, p -> s.setValue(p, false) }

    /**
     * Sets [be]'s block state arms to [connected], if they differ (server side).
     */
    fun sync(be: BlockEntity, connected: (Direction) -> Boolean) {
        val level = be.level ?: return
        if (level.isClientSide) return
        val current = be.blockState
        val wanted = PROPERTIES.entries.fold(current) { s, (face, p) -> if (p in s.properties) s.setValue(p, connected(face)) else s }
        if (wanted != current) level.setBlock(be.blockPos, wanted, Block.UPDATE_CLIENTS)
    }

    private val CORE: VoxelShape = Block.box(6.0, 6.0, 6.0, 10.0, 10.0, 10.0)
    private val ARMS: Map<Direction, VoxelShape> = mapOf(
        Direction.NORTH to Block.box(6.0, 6.0, 0.0, 10.0, 10.0, 6.0),
        Direction.SOUTH to Block.box(6.0, 6.0, 10.0, 10.0, 10.0, 16.0),
        Direction.WEST to Block.box(0.0, 6.0, 6.0, 6.0, 10.0, 10.0),
        Direction.EAST to Block.box(10.0, 6.0, 6.0, 16.0, 10.0, 10.0),
        Direction.DOWN to Block.box(6.0, 0.0, 6.0, 10.0, 6.0, 10.0),
        Direction.UP to Block.box(6.0, 10.0, 6.0, 10.0, 16.0, 10.0),
    )
    private val SHAPES = HashMap<Int, VoxelShape>()

    /**
     * The core plus the arms [state] shows.
     */
    fun shape(state: BlockState): VoxelShape {
        var mask = 0
        PROPERTIES.forEach { (face, p) -> if (p in state.properties && state.getValue(p)) mask = mask or (1 shl face.ordinal) }
        return SHAPES.getOrPut(mask) {
            Direction.entries.filter { mask and (1 shl it.ordinal) != 0 }.fold(CORE) { s, d -> Shapes.or(s, ARMS.getValue(d)) }
        }
    }
}
