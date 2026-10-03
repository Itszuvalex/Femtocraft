package com.itszuvalex.femtocraft.computation

import com.itszuvalex.femtocraft.archive.ArchiveBlockEntity
import com.itszuvalex.femtocraft.archive.ArchiveState
import com.itszuvalex.femtocraft.core.FemtoBlockEntity
import com.itszuvalex.femtocraft.core.FemtoHorizontalEntityBlock
import com.itszuvalex.femtocraft.core.FragData
import com.itszuvalex.itszulib.core.Distributable
import com.itszuvalex.itszulib.core.DistributionRole
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import kotlin.math.floor
import kotlin.math.min

/**
 * A computation job feeding an Archive: takes FLOPS while the Archive has a focus and room for computed points, and
 * turns every [FLOPS_PER_POINT] into one ([ArchiveState.addComputed]). Fractions of a point wait in [remainder]. Pure,
 * for unit tests; [ArchiveInterfaceBlockEntity] finds the Archive.
 */
class ArchiveJob(private val archive: () -> ArchiveState?) : Distributable {
    /** FLOPS short of a whole point. */
    var remainder = 0.0

    private fun room(state: ArchiveState): Double = state.computedRoom() * FLOPS_PER_POINT - remainder

    override val max: Double get() = archive()?.let { room(it) + remainder } ?: 0.0
    override val amount: Double get() = remainder
    override val transferMax: Double get() = MAX_PER_TICK

    override fun add(amount: Double): Double {
        val state = archive() ?: return 0.0
        val taken = min(amount, room(state)).coerceAtLeast(0.0)
        if (taken <= 0.0) return 0.0
        remainder += taken
        val points = floor(remainder / FLOPS_PER_POINT).toLong()
        if (points > 0L) remainder -= state.addComputed(points) * FLOPS_PER_POINT
        return taken
    }

    override fun remove(amount: Double): Double = 0.0

    companion object {
        /** FLOPS per research point. A host's nanite gives 10 points; four Micro Logic Cores make about 8 a second. */
        const val FLOPS_PER_POINT = 200.0

        /** At most this many FLOPS a tick. */
        const val MAX_PER_TICK = 400.0
    }
}

/**
 * The Archive Interface: placed against an Archive, it is a computation job that turns FLOPS into research points for
 * that Archive (DECISIONS D19). It takes FLOPS only while the Archive is claimed and its team has a focus.
 */
class ArchiveInterfaceBlockEntity(pos: BlockPos, state: BlockState) : FemtoBlockEntity(ComputationContent.ARCHIVE_INTERFACE_BE.get(), pos, state) {
    @JvmField
    val job = ArchiveJob(::archive)

    init {
        fragList.addFragment(FragComputationLeaf { if (archive() != null) ComputationParticipant(DistributionRole.CONSUMER, job) else null })
        fragList.addInternalFragment(FragData("ArchiveInterface", FragData.LEVEL, { _, o -> o.putDouble(REMAINDER_KEY, job.remainder) }, { _, i ->
            job.remainder = i.getDoubleOr(REMAINDER_KEY, 0.0)
        }))
    }

    /**
     * The state of an Archive touching this block that has something to research, if any (loaded only).
     */
    fun archive(): ArchiveState? {
        val level = level as? ServerLevel ?: return null
        for (face in Direction.entries) {
            val at = blockPos.relative(face)
            if (!level.isLoaded(at)) continue
            val state = (level.getBlockEntity(at) as? ArchiveBlockEntity)?.state() ?: continue
            if (state.hasFocus(level.server)) return state
        }
        return null
    }

    companion object {
        const val REMAINDER_KEY = "Remainder"
    }
}

class ArchiveInterfaceBlock(properties: BlockBehaviour.Properties) :
    FemtoHorizontalEntityBlock<ArchiveInterfaceBlockEntity>(properties, { ComputationContent.ARCHIVE_INTERFACE_BE.get() })
