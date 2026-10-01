package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.core.FemtoBlockEntity
import com.itszuvalex.femtocraft.core.FemtoEntityBlock
import com.itszuvalex.femtocraft.core.FragDerivedColor
import com.itszuvalex.femtocraft.power.DistributableBattery
import com.itszuvalex.femtocraft.power.DistributionAlgorithm
import com.itszuvalex.femtocraft.power.FragPowerStorage
import com.itszuvalex.femtocraft.power.FragWirelessPowerLeafNode
import com.itszuvalex.femtocraft.power.PowerCrystals
import com.itszuvalex.femtocraft.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.power.addWirelessLeaf
import com.itszuvalex.femtocraft.power.parentColor
import com.itszuvalex.itszulib.api.Modules
import com.itszuvalex.itszulib.api.adapters.IBattery
import com.itszuvalex.itszulib.api.adapters.IFluidStack
import com.itszuvalex.itszulib.api.adapters.IItemStack
import com.itszuvalex.itszulib.api.adapters.ILevel
import com.itszuvalex.itszulib.api.multiblock.MultiblockSidedFluidStorageConfiguration
import com.itszuvalex.itszulib.api.multiblock.MultiblockSidedItemStorageConfiguration
import com.itszuvalex.itszulib.api.storage.DynamicIBattery
import com.itszuvalex.itszulib.api.storage.DynamicIFluidStorage
import com.itszuvalex.itszulib.api.storage.DynamicIItemStorage
import com.itszuvalex.itszulib.api.storage.FluidStorageArray
import com.itszuvalex.itszulib.api.storage.IFluidStorage
import com.itszuvalex.itszulib.api.storage.IItemStorage
import com.itszuvalex.itszulib.api.storage.ItemStorageArray
import com.itszuvalex.itszulib.api.storage.ItemStorageSlice
import com.itszuvalex.itszulib.api.storage.PowerBattery
import com.itszuvalex.itszulib.core.SidedFluidStorageConfiguration
import com.itszuvalex.itszulib.core.SidedItemStorageConfiguration
import com.itszuvalex.itszulib.core.frag.FragFluidAutoIO
import com.itszuvalex.itszulib.core.frag.FragFluidStorage
import com.itszuvalex.itszulib.core.frag.FragItemAutoIO
import com.itszuvalex.itszulib.core.frag.FragItemStorage
import com.itszuvalex.itszulib.core.frag.FragMenu
import com.itszuvalex.itszulib.core.frag.FragMultiBlockInfo
import com.itszuvalex.itszulib.core.frag.FragMultiblockState
import com.itszuvalex.itszulib.core.frag.FragMultiblockTickable
import com.itszuvalex.itszulib.core.frag.FragSidedConfiguration
import com.itszuvalex.itszulib.core.frag.addFluidStorage
import com.itszuvalex.itszulib.core.frag.addItemStorage
import com.itszuvalex.itszulib.core.frag.addTickableFragment
import com.itszuvalex.itszulib.util.Task
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.neoforged.neoforge.common.util.ValueIOSerializable
import net.neoforged.neoforge.fluids.FluidStack
import kotlin.random.Random

// --- Germination chamber ---------------------------------------------------------------------------------------------

/**
 * Shared state of a germination chamber, held by its controller. Port of v3's `GerminationChamberState` and
 * `GerminationTask`: a 30k battery, a 10 000 mB tank, an input slot and three output slots, and the task.
 *
 * Difference from v3: rolled results wait in [pending] until they all fit (v3 kept them in the task and discarded them
 * when it started the next seed, so a full output lost the harvest).
 */
class GerminationState(onChanged: Runnable) : ValueIOSerializable {
    @JvmField
    val battery = PowerBattery(BATTERY_SIZE.toDouble(), onChanged)

    @JvmField
    val tank = FluidStorageArray(1, TANK_SIZE, onChanged)

    @JvmField
    val storage = object : ItemStorageArray(4, onChanged) {
        override fun canInsert(index: Int, stack: IItemStack): Boolean = index == 0 && GerminationRecipes.find(stack.toMinecraft()) != null
    }

    @JvmField
    val input: IItemStorage = ItemStorageSlice(storage, intArrayOf(0))

    @JvmField
    val output: IItemStorage = ItemStorageSlice(storage, intArrayOf(1, 2, 3))

    @JvmField
    val task = Task(1.0, 1)

    var seed: ItemStack = ItemStack.EMPTY

    @JvmField
    val pending = ArrayList<ItemStack>()

    fun tick(random: Random) {
        if (pending.isNotEmpty()) {
            flush()
            return
        }
        if (seed.isEmpty) {
            val recipe = GerminationRecipes.find(input.get(0).toMinecraft()) ?: return
            seed = input.split(0, 1).toMinecraft()
            task.reset()
            task.minTicks = recipe.ticks
            task.baseGoal = recipe.ticks.toDouble() * recipe.powerPerTick
            return
        }
        val recipe = GerminationRecipes.find(seed)
        if (recipe == null) {
            seed = ItemStack.EMPTY
            task.reset()
            return
        }
        val water = IFluidStack.of(FluidStack(recipe.fluid, recipe.fluidPerTick))
        if (tank.drain(water, false).amount() < recipe.fluidPerTick) return
        tank.drain(water, true)
        task.contributeFrom(battery, 0.0, 0.0)
        if (task.completed(0.0)) {
            pending += recipe.roll(random)
            seed = ItemStack.EMPTY
            task.reset()
            flush()
        }
    }

    private fun flush() {
        val it = pending.listIterator()
        while (it.hasNext()) {
            var stack = it.next()
            for (slot in 0 until output.size()) {
                if (stack.isEmpty) break
                stack = output.insert(slot, IItemStack.of(stack)).toMinecraft()
            }
            if (stack.isEmpty) it.remove() else it.set(stack)
        }
    }

    override fun serialize(output: ValueOutput) {
        battery.serialize(output.child("Battery"))
        tank.serialize(output.child("Tank"))
        storage.serialize(output.child("Items"))
        task.serialize(output.child("Task"))
        output.store("Seed", ItemStack.OPTIONAL_CODEC, seed)
        output.store("Pending", ItemStack.OPTIONAL_CODEC.listOf(), pending.toList())
    }

    override fun deserialize(input: ValueInput) {
        input.child("Battery").ifPresent(battery::deserialize)
        input.child("Tank").ifPresent(tank::deserialize)
        input.child("Items").ifPresent(storage::deserialize)
        input.child("Task").ifPresent(task::deserialize)
        seed = input.read("Seed", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY)
        pending.clear()
        pending.addAll(input.read("Pending", ItemStack.OPTIONAL_CODEC.listOf()).orElse(listOf()))
    }

    companion object {
        const val BATTERY_SIZE = 30000
        const val TANK_SIZE = 10000
        const val LEAF_TRANSFER_RATE = 40.0
    }
}

/**
 * Base for blocks of a frame-built multiblock: multiblock membership, controller state, teardown and menu.
 */
abstract class FrameMachineBlockEntity<S : ValueIOSerializable>(
    type: net.minecraft.world.level.block.entity.BlockEntityType<*>,
    pos: BlockPos,
    state: BlockState,
    private val multiblock: () -> FrameMultiblock,
) : FemtoBlockEntity(type, pos, state) {
    @JvmField
    val info = FragMultiBlockInfo()

    abstract val mbState: FragMultiblockState<S>

    val isController: Boolean get() = info.info.isFormed && info.info.isController

    fun state(): S? = mbState.get()

    protected abstract fun drops(state: S): List<ItemStack>

    protected fun addFrameMachineFragments(title: String, menu: (Int, net.minecraft.world.entity.player.Inventory) -> net.minecraft.world.inventory.AbstractContainerMenu) {
        fragList.addFragment(info)
        fragList.addInternalFragment(mbState)
        fragList.addFragment(FragMenu(Component.translatable(title), { id, inv, _ -> menu(id, inv) }, info))
        fragList.addInternalFragment(FragMultiblockTeardown(info, { multiblock().takenLocations(it) }) { level, controller ->
            val ctrl = if (controller == blockPos) this else level.getBlockEntity(controller) as? FrameMachineBlockEntity<*>
            @Suppress("UNCHECKED_CAST")
            (ctrl as? FrameMachineBlockEntity<S>)?.state()?.let { drops(it) } ?: listOf()
        })
    }
}

/**
 * A block of the germination chamber (2x3x2, built from frames). Port of v3's `TileGerminationChamber`: grows seeds
 * into crops with water and power. Every block exposes the chamber's input, outputs and tank on its outer faces (side
 * configuration per block; faces between chamber blocks expose nothing) and moves items and fluid automatically; the
 * controller is a wireless power consumer.
 *
 * Difference from v3: breaking the chamber drops its contents (v3 removed the blocks and lost them).
 */
class GerminationChamberBlockEntity(pos: BlockPos, state: BlockState) :
    FrameMachineBlockEntity<GerminationState>(IndustryContent.GERMINATION_CHAMBER_BE.get(), pos, state, { FrameMultiblocks.GERMINATION_CHAMBER }) {
    override val mbState: FragMultiblockState<GerminationState> = FragMultiblockState(info, { GerminationState { markDirty() } }, { (it as? GerminationChamberBlockEntity)?.mbState })

    @JvmField
    val storage: IItemStorage = DynamicIItemStorage { state()?.storage ?: IItemStorage.Empty }

    @JvmField
    val tank: IFluidStorage = DynamicIFluidStorage { state()?.tank ?: IFluidStorage.Empty }

    @JvmField
    val battery: IBattery = DynamicIBattery { state()?.battery ?: IBattery.Empty }

    @JvmField
    val leaf = FragWirelessPowerLeafNode({ battery }, PowerStorageNodeType.CONSUMER, rate = { GerminationState.LEAF_TRANSFER_RATE }, active = { isController })

    init {
        addFrameMachineFragments("block.femtocraft.germination_chamber") { id, inv -> GerminationChamberMenu(id, inv, this) }
        val input = DynamicIItemStorage { state()?.input ?: IItemStorage.Empty }
        val output = DynamicIItemStorage { state()?.output ?: IItemStorage.Empty }
        fragList.addFragment(FragSidedConfiguration<SidedItemStorageConfiguration>(
            "ItemConfig",
            MultiblockSidedItemStorageConfiguration({ level?.let(ILevel::of) }, { blockPos }, info.info, NONE, { INPUT },
                mapOf(NONE to IItemStorage.Empty, INPUT to input, OUTPUT to output), { Direction.NORTH }),
            Modules.ITEM_STORAGE_CONFIGURABLE,
        ))
        fragList.addFragment(FragSidedConfiguration<SidedFluidStorageConfiguration>(
            "FluidConfig",
            MultiblockSidedFluidStorageConfiguration({ level?.let(ILevel::of) }, { blockPos }, info.info, NONE, { TANK },
                mapOf(NONE to IFluidStorage.Empty, TANK to tank), { Direction.NORTH }),
            Modules.FLUID_STORAGE_CONFIGURABLE,
        ))
        fragList.addItemStorage(FragItemStorage(storage, persist = false))
        fragList.addFluidStorage(FragFluidStorage(tank, persist = false))
        fragList.addTickableFragment(FragItemAutoIO())
        fragList.addTickableFragment(FragFluidAutoIO())
        fragList.addFragment(FragPowerStorage({ battery }, persist = false))
        fragList.addWirelessLeaf(leaf)
        fragList.addFragment(FragDerivedColor { controllerLeafColor() })
        fragList.addTickableFragment(object : FragMultiblockTickable(info.info) {
            override fun name(): String = "Germination"
            override fun serverControllerTick(level: ILevel, pos: BlockPos) {
                state()?.tick(Random)
            }
        })
    }

    private fun controllerLeafColor() = (info.controller() as? GerminationChamberBlockEntity)?.let { parentColor(it, it.leaf) } ?: FragDerivedColor.NONE

    override fun serverTick() {
        leaf.refreshRegistration()
    }

    override fun drops(state: GerminationState): List<ItemStack> =
        (0 until state.storage.size()).map { state.storage.get(it).toMinecraft().copy() } + state.pending.map { it.copy() } + listOfNotNull(state.seed.copy().takeIf { !it.isEmpty })

    companion object {
        const val INPUT = "Input"
        const val OUTPUT = "Output"
        const val NONE = "None"
        const val TANK = "Tank"
    }
}

class GerminationChamberBlock(p: BlockBehaviour.Properties) : FemtoEntityBlock<GerminationChamberBlockEntity>(p, { IndustryContent.GERMINATION_CHAMBER_BE.get() })

// --- Crystal focusing chamber ----------------------------------------------------------------------------------------

/**
 * Shared state of a crystal focusing chamber. Port of v3's `FocusingChamberState`: four small/medium crystals and one
 * large crystal.
 */
class FocusingState(onChanged: Runnable) : ValueIOSerializable {
    @JvmField
    val small = object : ItemStorageArray(4, onChanged) {
        override fun canInsert(index: Int, stack: IItemStack): Boolean = PowerCrystals.data(stack.toMinecraft())?.let { it.type != PowerCrystals.TYPE_LARGE } ?: false
        override fun maxStackSize(index: Int): Int = 1
    }

    @JvmField
    val large = object : ItemStorageArray(1, onChanged) {
        override fun canInsert(index: Int, stack: IItemStack): Boolean = PowerCrystals.data(stack.toMinecraft())?.type == PowerCrystals.TYPE_LARGE
        override fun maxStackSize(index: Int): Int = 1
    }

    /**
     * Port of v3's `ModuleFocusingChamber.serverControllerUpdate`: small crystals charge at six times their passive
     * rate; the large crystal trickle charges and draws up to [TRANSFER_PER_TICK] from each small one.
     *
     * @return True if anything changed.
     */
    fun tick(): Boolean {
        var changed = false
        val smalls = (0 until small.size()).map { small.get(it).toMinecraft() }.filter { PowerCrystals.isCrystal(it) }
        for (s in smalls) {
            val data = PowerCrystals.data(s)!!
            if (PowerCrystals.battery(s)!!.fill(data.passiveGen.toDouble() * SMALL_MULTIPLIER) > 0) changed = true
        }
        val big = large.get(0).toMinecraft()
        if (PowerCrystals.isCrystal(big)) {
            changed = PowerCrystals.onTick(big) || changed
            val moved = DistributionAlgorithm(
                smalls.map { DistributableBattery(PowerCrystals.battery(it)!!) { TRANSFER_PER_TICK } },
                listOf(),
                listOf(DistributableBattery(PowerCrystals.battery(big)!!) { small.size() * TRANSFER_PER_TICK }),
            ).distribute()
            if (moved.total > 0) changed = true
        }
        return changed
    }

    override fun serialize(output: ValueOutput) {
        small.serialize(output.child("SmallCrystals"))
        large.serialize(output.child("LargeCrystal"))
    }

    override fun deserialize(input: ValueInput) {
        input.child("SmallCrystals").ifPresent(small::deserialize)
        input.child("LargeCrystal").ifPresent(large::deserialize)
    }

    companion object {
        const val TRANSFER_PER_TICK = 5.0
        const val SMALL_MULTIPLIER = 6
    }
}

/**
 * A block of the crystal focusing chamber (2x2x2, built from frames). v3 wrote the logic (`ModuleFocusingChamber`) but
 * never attached it to the tile, and gave it no GUI; here it runs and has a menu (DECISIONS D13).
 */
class CrystalFocusingChamberBlockEntity(pos: BlockPos, state: BlockState) :
    FrameMachineBlockEntity<FocusingState>(IndustryContent.CRYSTAL_FOCUSING_CHAMBER_BE.get(), pos, state, { FrameMultiblocks.CRYSTAL_FOCUSING_CHAMBER }) {
    override val mbState: FragMultiblockState<FocusingState> = FragMultiblockState(info, { FocusingState { markDirty() } }, { (it as? CrystalFocusingChamberBlockEntity)?.mbState })

    @JvmField
    val small: IItemStorage = DynamicIItemStorage { state()?.small ?: IItemStorage.Empty }

    @JvmField
    val large: IItemStorage = DynamicIItemStorage { state()?.large ?: IItemStorage.Empty }

    init {
        addFrameMachineFragments("block.femtocraft.crystal_focusing_chamber") { id, inv -> FocusingChamberMenu(id, inv, this) }
        fragList.addTickableFragment(object : FragMultiblockTickable(info.info) {
            override fun name(): String = "Focusing"
            override fun serverControllerTick(level: ILevel, pos: BlockPos) {
                if (state()?.tick() == true) markDirty()
            }
        })
    }

    override fun drops(state: FocusingState): List<ItemStack> =
        (0 until 4).map { state.small.get(it).toMinecraft().copy() } + state.large.get(0).toMinecraft().copy()
}

class CrystalFocusingChamberBlock(p: BlockBehaviour.Properties) : FemtoEntityBlock<CrystalFocusingChamberBlockEntity>(p, { IndustryContent.CRYSTAL_FOCUSING_CHAMBER_BE.get() })

