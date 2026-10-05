package com.itszuvalex.femtocraft.logistics

import net.minecraft.core.Direction
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

/**
 * Where a conduit shows its chips in the world: a small cube per chip at a corner of the square end of its face's arm,
 * touching the neighbouring block (or, on a face without an arm, touching the core), each face showing the chip. The
 * renderer draws them, the block's shape includes them so they can be aimed at, and using one opens that chip's own
 * menu ([ChipMenu]).
 *
 * A conduit's chips are summed up in a layout: 2 bits per slot (face * [LogisticsConduit.CHIPS_PER_FACE] + index),
 * 0 for an empty slot, otherwise the index of the chip's kind in [Chips.KINDS] plus one. Servers keep it current and
 * sync it to clients.
 */
object ChipNodes {
    const val SLOTS = 6 * LogisticsConduit.CHIPS_PER_FACE

    /** The arm's square spans [SQUARE_MIN, SQUARE_MAX] across the face (pixels), the core the same along it. */
    private const val SQUARE_MIN = 6.0
    private const val SQUARE_MAX = 10.0
    private const val HALF = 1.25
    private const val DEPTH = 2.5

    /** The kind in [slot] of [layout]: 0 for none, else its index in [Chips.KINDS] plus one. */
    fun kindAt(layout: Long, slot: Int): Int = ((layout ushr (slot * 2)) and 3L).toInt()

    fun withKind(layout: Long, slot: Int, kind: Int): Long = (layout and (3L shl (slot * 2)).inv()) or ((kind.toLong() and 3L) shl (slot * 2))

    /** The layout entry for chip [stack] (0 if it is no chip). */
    fun kindOf(stack: net.minecraft.world.item.ItemStack): Int = Chips.kindOf(stack)?.let { Chips.KINDS.indexOf(it) + 1 } ?: 0

    /**
     * Chip [slot]'s cube in block coordinates (0 to 1): index 0 to 3 are the square's corners, the first axis across
     * the face low then high, then the second. With [arm] it touches the neighbouring block, else the core.
     */
    fun box(slot: Int, arm: Boolean): AABB {
        val face = Direction.from3DDataValue(slot / LogisticsConduit.CHIPS_PER_FACE)
        val index = slot % LogisticsConduit.CHIPS_PER_FACE
        val a = if (index and 1 == 0) SQUARE_MIN else SQUARE_MAX
        val b = if (index and 2 == 0) SQUARE_MIN else SQUARE_MAX
        // Along the face's axis: from the block edge (arm) or the core's face, a cube's depth outwards.
        val (near, far) = if (arm) Pair(0.0, DEPTH) else Pair(SQUARE_MIN - DEPTH, SQUARE_MIN)
        val (lo, hi) = if (face.axisDirection == Direction.AxisDirection.NEGATIVE) Pair(near, far) else Pair(16.0 - far, 16.0 - near)
        val (x, y, z) = when (face.axis) {
            Direction.Axis.X -> Triple(lo to hi, a - HALF to a + HALF, b - HALF to b + HALF)
            Direction.Axis.Y -> Triple(a - HALF to a + HALF, lo to hi, b - HALF to b + HALF)
            Direction.Axis.Z -> Triple(a - HALF to a + HALF, b - HALF to b + HALF, lo to hi)
        }
        return AABB(x.first / 16, y.first / 16, z.first / 16, x.second / 16, y.second / 16, z.second / 16)
    }

    /** The cubes of every chip in [layout], as a shape; [arms] says which faces have arms. */
    fun shape(layout: Long, arms: (Direction) -> Boolean): VoxelShape {
        var shape = Shapes.empty()
        for (slot in 0 until SLOTS) {
            if (kindAt(layout, slot) == 0) continue
            shape = Shapes.or(shape, Shapes.create(box(slot, arms(Direction.from3DDataValue(slot / LogisticsConduit.CHIPS_PER_FACE)))))
        }
        return shape
    }

    /** The chip slot whose cube [local] (a hit point relative to the block's corner) is on or in, or -1. */
    fun slotAt(layout: Long, arms: (Direction) -> Boolean, local: Vec3): Int = (0 until SLOTS).firstOrNull { slot ->
        kindAt(layout, slot) != 0 && box(slot, arms(Direction.from3DDataValue(slot / LogisticsConduit.CHIPS_PER_FACE))).inflate(EPSILON).contains(local)
    } ?: -1

    private const val EPSILON = 1e-3
}
