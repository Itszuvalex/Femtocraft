package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.FemtoBlockEntities
import com.itszuvalex.femtocraft.core.FemtoBlockEntity
import com.itszuvalex.femtocraft.core.FluidTanks
import com.itszuvalex.femtocraft.core.FragData
import com.itszuvalex.femtocraft.core.FragMultiBlock
import com.itszuvalex.femtocraft.power.FragPowerNode
import com.itszuvalex.femtocraft.power.OwnPowerStorage
import com.itszuvalex.femtocraft.power.PowerNodeRules
import com.itszuvalex.femtocraft.power.PowerNodeTypes
import com.itszuvalex.itszulib.api.adapters.ILevel
import com.itszuvalex.itszulib.core.frag.InternalBlockEntityFragment
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.capabilities.Capabilities

/**
 * Removes the whole multiblock when one of its blocks is broken (unless the multiblock is removing itself).
 */
class FragBreakMultiblock(private val part: () -> FragMultiBlock, private val multiblock: () -> IFrameMultiblock?) :
    InternalBlockEntityFragment() {
    override fun name(): String = "BreakMultiblock"

    override fun onRemove(level: ILevel, pos: BlockPos, blockStatePrev: BlockState) {
        if (MultiblockGuard.breaking) return
        val mb = part()
        if (!mb.isFormed) return
        multiblock()?.onMultiblockBroken(level.toMinecraft(), mb.controllerPos)
    }
}

/**
 * A block of a formed frame machine with no behaviour of its own yet (centrifuge, crystallization chamber: their
 * 1.7.10 tiles were empty stubs).
 */
open class MultiblockPartBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState, multiblockType: () -> IFrameMultiblock) :
    FemtoBlockEntity(type, pos, state), IMultiblockPart {
    final override val multiblock = FragMultiBlock { blockPos }

    init {
        fragList.addInternalFragment(multiblock)
        fragList.addInternalFragment(FragBreakMultiblock({ this.multiblock }, multiblockType))
    }
}

class CentrifugeBlockEntity(pos: BlockPos, state: BlockState) :
    MultiblockPartBlockEntity(FemtoBlockEntities.CENTRIFUGE.get(), pos, state, { MultiblockCentrifuge })

class CrystallizationChamberBlockEntity(pos: BlockPos, state: BlockState) :
    MultiblockPartBlockEntity(FemtoBlockEntities.CRYSTALLIZATION_CHAMBER.get(), pos, state, { MultiblockCrystallizationChamber })

/**
 * Arc furnace part: a 2000 mB output tank and a diffusion-target power node, both on the controller. Port of
 * 1.7.10 `TileArcFurnace`, whose processing logic was never written (no arc furnace recipes existed).
 */
class ArcFurnaceBlockEntity(pos: BlockPos, state: BlockState) :
    MultiblockPartBlockEntity(FemtoBlockEntities.ARC_FURNACE.get(), pos, state, { MultiblockArcFurnace }) {
    val tank = FluidTanks(intArrayOf(FLUID_TANK_SIZE), { setChanged() })
    val powerNode = FragPowerNode(PowerNodeRules.plain(PowerNodeTypes.DIFFUSION_TARGET_NODE), OwnPowerStorage(0.0) { setChanged() })

    init {
        fragList.addFragment(powerNode)
        fragList.addInternalFragment(FragData("Tank", FragData.LEVEL, { _, out -> tank.serialize(out) }, { _, input -> tank.deserialize(input) }))
        // Drain-only from outside, and only once formed (1.7.10 canFill=false, canDrain=isValidMultiBlock).
        fragList.addCapability(Capabilities.Fluid.BLOCK) { if (multiblock.isFormed) controllerTank() else null }
    }

    private fun controllerTank() = multiblock.controller<ArcFurnaceBlockEntity>(level)?.tank

    override fun onServerLoad() {
        if (multiblock.isController) powerNode.onServerLoad()
    }

    override fun onServerUnload() = powerNode.onServerUnload()

    companion object {
        const val FLUID_TANK_SIZE = 2000
    }
}
