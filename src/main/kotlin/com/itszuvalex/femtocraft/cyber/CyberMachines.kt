package com.itszuvalex.femtocraft.cyber

import com.itszuvalex.femtocraft.FemtoBlockEntities
import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.FemtoItems
import com.itszuvalex.femtocraft.core.FemtoBlockEntity
import com.itszuvalex.femtocraft.core.FragData
import com.itszuvalex.femtocraft.core.FragMultiBlock
import com.itszuvalex.femtocraft.core.getLoc
import com.itszuvalex.femtocraft.core.putLoc
import com.itszuvalex.femtocraft.core.blockEntity
import com.itszuvalex.femtocraft.industry.IMultiblockPart
import com.itszuvalex.femtocraft.industry.MultiblockGuard
import com.itszuvalex.itszulib.api.adapters.ILevel
import com.itszuvalex.itszulib.api.utility.Loc4
import com.itszuvalex.itszulib.core.frag.InternalBlockEntityFragment
import net.minecraft.core.BlockPos
import net.minecraft.world.Containers
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

/**
 * A machine grown on top of a cyber base, occupying [requiredSlots] vertical slots of a base of size
 * [requiredBaseSize]. Port of 1.7.10 `ICyberMachine`.
 */
open class CyberMachine(
    val name: String,
    val requiredBaseSize: Int,
    val requiredSlots: Int,
    private val blockSupplier: () -> Block,
    val requiredCybermass: Int = 0,
    private val resources: () -> List<ItemStack> = { emptyList() },
    /**
     * Whether breaking the machine drops [getRequiredResources] (the 1.7.10 stub machines did, the growth chamber and
     * grasping vines did not).
     */
    private val dropsResources: Boolean = true,
) {
    fun getRequiredResources(): List<ItemStack> = resources()

    fun block(): Block = blockSupplier()

    fun getTakenLocations(controller: BlockPos): List<BlockPos> =
        (0 until requiredBaseSize).flatMap { x -> (0 until requiredSlots).flatMap { y -> (0 until requiredBaseSize).map { z -> controller.offset(x, y, z) } } }

    /**
     * Replaces the machine's footprint above [base]'s slot [machineIndex] with the machine's blocks.
     */
    fun formAtBaseAndIndex(level: Level, base: CyberBaseBlockEntity, machineIndex: Int) {
        val controller = BlockPos(base.blockPos.x, base.yFromSlot(machineIndex), base.blockPos.z)
        val locs = getTakenLocations(controller)
        MultiblockGuard.run { locs.forEach { level.setBlockAndUpdate(it, block().defaultBlockState()) } }
        locs.forEach { loc ->
            val be = level.getBlockEntity(loc) as? ICyberMachinePart ?: return@forEach
            be.machineIndex = machineIndex
            be.basePos = base.loc
            be.multiblock.form(controller)
        }
    }

    fun breakMachine(level: Level, controller: BlockPos) {
        MultiblockGuard.run { getTakenLocations(controller).forEach { level.removeBlock(it, false) } }
        if (dropsResources) getRequiredResources().forEach {
            Containers.dropItemStack(level, controller.x.toDouble(), controller.y.toDouble(), controller.z.toDouble(), it.copy())
        }
    }
}

object CyberMachineRegistry {
    const val GROWTH_CHAMBER = "Growth Chamber"
    const val BIO_BEACON = "Bio Beacon"
    const val CONDENSATION_ARRAY = "Condensation Array"
    const val CYBERMAT_DISINTEGRATOR = "Cybermat Disintegrator"
    const val GRASPING_VINES = "Grasping Vines"
    const val LASHING_VINES = "Lashing Vines"
    const val METABOLIC_CONVERTER = "Metabolic Converter"
    const val PHOTOSYNTHESIS_TOWER = "Photosynthesis Tower"
    const val SPORE_DISTRIBUTOR = "Spore Distributor"

    private val machineMap = LinkedHashMap<String, CyberMachine>()

    fun getMachine(name: String?): CyberMachine? = name?.let { machineMap[it] }

    fun getMachinesForSize(size: Int): List<CyberMachine> = machineMap.values.filter { it.requiredBaseSize == size }

    fun getMachinesThatFitIn(size: Int, remainingSlots: Int): List<CyberMachine> =
        getMachinesForSize(size).filter { it.requiredSlots <= remainingSlots }

    fun registerMachine(machine: CyberMachine) {
        machineMap[machine.name] = machine
    }

    init {
        registerMachine(
            CyberMachine(GROWTH_CHAMBER, 2, 2, { FemtoBlocks.GROWTH_CHAMBER.get() }, 500, { listOf(ItemStack(FemtoItems.CYBERWEAVE.get(), 20)) }, dropsResources = false),
        )
        registerMachine(CyberMachine(BIO_BEACON, 1, 2, { FemtoBlocks.BIO_BEACON.get() }))
        registerMachine(CyberMachine(CONDENSATION_ARRAY, 1, 2, { FemtoBlocks.CONDENSATION_ARRAY.get() }))
        registerMachine(CyberMachine(CYBERMAT_DISINTEGRATOR, 1, 1, { FemtoBlocks.CYBERMAT_DISINTEGRATOR.get() }))
        registerMachine(CyberMachine(GRASPING_VINES, 1, 2, { FemtoBlocks.GRASPING_VINES.get() }, dropsResources = false))
        registerMachine(CyberMachine(LASHING_VINES, 1, 2, { FemtoBlocks.LASHING_VINES.get() }))
        registerMachine(CyberMachine(METABOLIC_CONVERTER, 1, 2, { FemtoBlocks.METABOLIC_CONVERTER.get() }))
        registerMachine(CyberMachine(PHOTOSYNTHESIS_TOWER, 1, 4, { FemtoBlocks.PHOTOSYNTHESIS_TOWER.get() }))
        registerMachine(CyberMachine(SPORE_DISTRIBUTOR, 1, 2, { FemtoBlocks.SPORE_DISTRIBUTOR.get() }))
    }
}

/**
 * A block of a cyber machine (or of a machine under construction). Port of 1.7.10 `CyberMachineMultiblock`
 * (keys `BasePos`, `Index`).
 */
interface ICyberMachinePart : IMultiblockPart {
    var machineIndex: Int
    var basePos: Loc4?
    val machineName: String?
}

/**
 * Cyber machine block entity. The 1.7.10 tiles for everything but the growth chamber and grasping vines were empty
 * stubs (a 1000 mB tank nobody could fill and a 0-slot inventory); they are plain parts here. Breaking any block
 * breaks the machine and everything stacked above it on the base.
 */
open class CyberMachineBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState, override val machineName: String?) :
    FemtoBlockEntity(type, pos, state), ICyberMachinePart {
    final override val multiblock = FragMultiBlock { blockPos }
    override var machineIndex: Int = -1
    override var basePos: Loc4? = null

    init {
        fragList.addInternalFragment(multiblock)
        fragList.addInternalFragment(FragData("CyberMultiblock", FragData.LEVEL_AND_DESCRIPTION, { _, out ->
            out.putLoc(BASE_POS_KEY, basePos)
            out.putInt(INDEX_KEY, machineIndex)
        }, { _, input ->
            basePos = input.getLoc(BASE_POS_KEY)
            machineIndex = input.getIntOr(INDEX_KEY, -1)
        }))
        fragList.addInternalFragment(object : InternalBlockEntityFragment() {
            override fun name(): String = "BreakMachine"
            override fun onRemove(level: ILevel, pos: BlockPos, blockStatePrev: BlockState) = onMachineBroken(level.toMinecraft())
        })
    }

    fun base(): CyberBaseBlockEntity? = level?.let { l -> basePos?.blockEntity(l, true) } as? CyberBaseBlockEntity

    protected open fun onMachineBroken(level: Level) {
        if (level.isClientSide || MultiblockGuard.breaking || !multiblock.isFormed) return
        base()?.breakMachinesUpwardsFromSlot(machineIndex)
    }

    companion object {
        const val BASE_POS_KEY = "BasePos"
        const val INDEX_KEY = "Index"

        /**
         * Block entity type factory for a stub machine.
         */
        fun stub(type: () -> BlockEntityType<*>, name: String): (BlockPos, BlockState) -> CyberMachineBlockEntity =
            { pos, state -> CyberMachineBlockEntity(type(), pos, state, name) }
    }
}

/**
 * A machine being built; turns into the machine after [buildTime] ticks. Port of 1.7.10 `TileCyberMachineInProgress`
 * (keys `Compound { Machine, BuildTime }`).
 */
class CyberMachineInProgressBlockEntity(pos: BlockPos, state: BlockState) :
    CyberMachineBlockEntity(FemtoBlockEntities.CYBER_MACHINE_IN_PROGRESS.get(), pos, state, null) {
    var machineInProgress: String? = null
    var buildTime = 0
    private var finished = false

    override val machineName: String? get() = machineInProgress

    init {
        fragList.addInternalFragment(FragData("InProgress", FragData.LEVEL_AND_DESCRIPTION, { _, out ->
            val comp = out.child(COMPOUND_KEY)
            comp.putString(MACHINE_KEY, machineInProgress ?: "")
            comp.putInt(BUILD_TIME_KEY, buildTime)
        }, { _, input ->
            input.child(COMPOUND_KEY).ifPresent {
                machineInProgress = it.getStringOr(MACHINE_KEY, "").ifEmpty { null }
                buildTime = it.getIntOr(BUILD_TIME_KEY, 0)
            }
        }))
    }

    override fun serverTick() {
        if (!multiblock.isController) return
        if (buildTime > 0) {
            buildTime--
            setChanged()
            return
        }
        val machine = CyberMachineRegistry.getMachine(machineInProgress) ?: return
        val base = base() ?: return
        finished = true
        machine.formAtBaseAndIndex(level!!, base, machineIndex)
    }

    override fun onMachineBroken(level: Level) {
        if (finished) return
        super.onMachineBroken(level)
    }

    companion object {
        const val COMPOUND_KEY = "Compound"
        const val MACHINE_KEY = "Machine"
        const val BUILD_TIME_KEY = "BuildTime"
    }
}
