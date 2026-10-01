package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.core.FemtoBlockEntity
import com.itszuvalex.femtocraft.core.FemtoHorizontalEntityBlock
import com.itszuvalex.femtocraft.core.FragData
import com.itszuvalex.femtocraft.core.FragDerivedColor
import com.itszuvalex.femtocraft.power.FragPowerStorage
import com.itszuvalex.femtocraft.power.FragWiredPowerLeafNode
import com.itszuvalex.femtocraft.power.FragWirelessPowerLeafNode
import com.itszuvalex.femtocraft.power.PowerCrystals
import com.itszuvalex.femtocraft.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.power.addWirelessLeaf
import com.itszuvalex.femtocraft.power.crystalBattery
import com.itszuvalex.femtocraft.power.parentColor
import com.itszuvalex.itszulib.api.Modules
import com.itszuvalex.itszulib.api.adapters.IBattery
import com.itszuvalex.itszulib.api.adapters.IFluidStack
import com.itszuvalex.itszulib.api.adapters.IItemStack
import com.itszuvalex.itszulib.api.storage.DynamicIBattery
import com.itszuvalex.itszulib.api.storage.FluidStorageArray
import com.itszuvalex.itszulib.api.storage.IFluidStorage
import com.itszuvalex.itszulib.api.storage.IItemStorage
import com.itszuvalex.itszulib.api.storage.ItemStorageArray
import com.itszuvalex.itszulib.api.storage.ItemStorageSlice
import com.itszuvalex.itszulib.api.storage.PowerBattery
import com.itszuvalex.itszulib.core.HorizontalFacing
import com.itszuvalex.itszulib.core.SidedFluidStorageConfiguration
import com.itszuvalex.itszulib.core.SidedItemStorageConfiguration
import com.itszuvalex.itszulib.core.frag.FragDropInventory
import com.itszuvalex.itszulib.core.frag.FragFluidAutoIO
import com.itszuvalex.itszulib.core.frag.FragFluidStorage
import com.itszuvalex.itszulib.core.frag.FragItemAutoIO
import com.itszuvalex.itszulib.core.frag.FragItemStorage
import com.itszuvalex.itszulib.core.frag.FragMenu
import com.itszuvalex.itszulib.core.frag.FragSidedConfiguration
import com.itszuvalex.itszulib.core.frag.addFluidStorage
import com.itszuvalex.itszulib.core.frag.addItemStorage
import com.itszuvalex.itszulib.core.frag.addTickableFragment
import com.itszuvalex.itszulib.util.Color
import com.itszuvalex.itszulib.util.Task
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.neoforged.neoforge.fluids.FluidStack

/**
 * v3's default item faces for single-block machines (relative to a north-facing block): top and back take input, the
 * rest output.
 */
fun machineItemFaces(face: Direction): String = when (face) {
    Direction.UP, Direction.SOUTH -> ProcessingMachineBlockEntity.INPUT
    else -> ProcessingMachineBlockEntity.OUTPUT
}

/**
 * A single-block machine that takes one item at a time from its input slot, spends power on it (an ItszuLib [Task],
 * v3's `BatteryPoweredTask`), then pushes the result out. Port of v3's machine modules (`NanoFurnaceModule`,
 * `DemolisherModule`, `CrystalFurnaceModule`, `CrystalCrusherModule`, `CrystalLiquifierModule`): the result is worked
 * out when the task completes and kept until the output has room for all of it.
 *
 * Saves `Task` (progress, goal, ticks), `Processing` (the input being worked on), `Done` and the pending result.
 */
abstract class ProcessingMachineBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState, slots: Int) :
    FemtoBlockEntity(type, pos, state) {
    @JvmField
    val inventory: ItemStorageArray = object : ItemStorageArray(slots, { markDirty() }) {
        override fun canInsert(index: Int, stack: IItemStack): Boolean = canInsertSlot(index, stack.toMinecraft())
    }

    @JvmField
    val input: IItemStorage = ItemStorageSlice(inventory, intArrayOf(0))

    @JvmField
    val task = Task(POWER_REQ.toDouble(), TICKS_REQ)

    var processing: ItemStack = ItemStack.EMPTY
        private set

    private var done = false

    abstract val battery: IBattery

    protected abstract fun canInsertSlot(index: Int, stack: ItemStack): Boolean

    protected abstract fun hasRecipe(stack: ItemStack): Boolean

    /**
     * Works out the result of [stack] and holds it until [pushResult] gets it all out.
     *
     * @return False if there is none (the recipe went away); the input is then dropped from the task.
     */
    protected abstract fun computeResult(stack: ItemStack): Boolean

    /**
     * @return True once the pending result is entirely out.
     */
    protected abstract fun pushResult(): Boolean

    protected abstract fun clearResult()

    protected abstract fun saveResult(output: ValueOutput)

    protected abstract fun loadResult(input: ValueInput)

    fun progressFraction(): Double = task.fraction(0.0)

    init {
        fragList.addInternalFragment(FragData("Machine", FragData.LEVEL, { _, o ->
            task.serialize(o.child(TASK_KEY))
            o.store(PROCESSING_KEY, ItemStack.OPTIONAL_CODEC, processing)
            o.putBoolean(DONE_KEY, done)
            saveResult(o)
        }, { _, i ->
            i.child(TASK_KEY).ifPresent(task::deserialize)
            processing = i.read(PROCESSING_KEY, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY)
            done = i.getBooleanOr(DONE_KEY, false)
            loadResult(i)
        }))
    }

    override fun serverTick() {
        if (processing.isEmpty) {
            val item = input.get(0).toMinecraft()
            if (!item.isEmpty && hasRecipe(item)) {
                processing = input.split(0, 1).toMinecraft()
                task.reset()
                done = false
                markDirty()
            }
            return
        }
        if (task.contributeFrom(battery, 0.0, 0.0) > 0) markDirty()
        if (!task.completed(0.0)) return
        if (!done) {
            if (!computeResult(processing)) {
                reset()
                return
            }
            done = true
        }
        if (pushResult()) reset()
        markDirty()
    }

    private fun reset() {
        task.reset()
        processing = ItemStack.EMPTY
        done = false
        clearResult()
    }

    companion object {
        const val INPUT = "Input"
        const val OUTPUT = "Output"
        const val NONE = "None"
        const val TICKS_REQ = 20 * 8
        const val POWER_PER_TICK = 10
        const val POWER_REQ = TICKS_REQ * POWER_PER_TICK
        const val TASK_KEY = "Task"
        const val PROCESSING_KEY = "Processing"
        const val DONE_KEY = "Done"
        const val PENDING_KEY = "Pending"
        const val LEAF_TRANSFER_RATE = 50.0
    }
}

/**
 * A machine with an item output in slot 1.
 */
abstract class ItemProcessingMachineBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState, slots: Int) :
    ProcessingMachineBlockEntity(type, pos, state, slots) {
    @JvmField
    val output: IItemStorage = ItemStorageSlice(inventory, intArrayOf(1))

    private var pending: ItemStack = ItemStack.EMPTY

    protected abstract fun resultFor(stack: ItemStack): ItemStack

    override fun computeResult(stack: ItemStack): Boolean {
        pending = resultFor(stack).copy()
        return !pending.isEmpty
    }

    override fun pushResult(): Boolean {
        pending = inventory.insert(1, IItemStack.of(pending)).toMinecraft()
        return pending.isEmpty
    }

    override fun clearResult() {
        pending = ItemStack.EMPTY
    }

    override fun saveResult(output: ValueOutput) = output.store(PENDING_KEY, ItemStack.OPTIONAL_CODEC, pending)

    override fun loadResult(input: ValueInput) {
        pending = input.read(PENDING_KEY, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY)
    }

    protected fun addItemMachineFragments(title: String) {
        val config = FragSidedConfiguration(
            "ItemConfig",
            SidedItemStorageConfiguration(::machineItemFaces, mapOf(NONE to IItemStorage.Empty, INPUT to input, OUTPUT to output)) { HorizontalFacing.front(blockState) },
            Modules.ITEM_STORAGE_CONFIGURABLE,
        )
        fragList.addFragment(config)
        fragList.addItemStorage(FragItemStorage(inventory))
        fragList.addInternalFragment(FragDropInventory(inventory))
        fragList.addTickableFragment(FragItemAutoIO())
        fragList.addFragment(FragMenu(Component.translatable(title), { id, inv, _ -> MachineMenu(id, inv, this) }))
    }
}

/**
 * Machines with their own battery, charged from a wireless parent or a wired conduit (nano furnace, demolisher).
 */
abstract class PoweredItemMachineBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) :
    ItemProcessingMachineBlockEntity(type, pos, state, 2) {
    override val battery: IBattery = PowerBattery(BATTERY_SIZE.toDouble()) { markDirty() }

    @JvmField
    val leaf = FragWirelessPowerLeafNode({ battery }, PowerStorageNodeType.CONSUMER, rate = { LEAF_TRANSFER_RATE })

    protected fun addPowerFragments() {
        fragList.addFragment(FragPowerStorage({ battery }))
        fragList.addWirelessLeaf(leaf)
        fragList.addFragment(FragDerivedColor { parentColor(this, leaf) })
        fragList.addFragment(FragWiredPowerLeafNode({ battery }, PowerStorageNodeType.CONSUMER) { LEAF_TRANSFER_RATE })
    }

    companion object {
        const val BATTERY_SIZE = 5000
    }
}

/**
 * Smelts with vanilla furnace recipes. Port of v3's `TileNanoFurnace`.
 */
class NanoFurnaceBlockEntity(pos: BlockPos, state: BlockState) : PoweredItemMachineBlockEntity(IndustryContent.NANO_FURNACE_BE.get(), pos, state) {
    init {
        addItemMachineFragments("block.femtocraft.nano_furnace")
        addPowerFragments()
    }

    override fun canInsertSlot(index: Int, stack: ItemStack): Boolean = index == 0 && hasRecipe(stack)
    override fun hasRecipe(stack: ItemStack): Boolean = !Smelting.result(level, stack).isEmpty
    override fun resultFor(stack: ItemStack): ItemStack = Smelting.result(level, stack)
}

/**
 * Grinds ores and other items into dust ([DustRecipes]). Port of v3's `TileDemolisher`.
 */
class DemolisherBlockEntity(pos: BlockPos, state: BlockState) : PoweredItemMachineBlockEntity(IndustryContent.DEMOLISHER_BE.get(), pos, state) {
    init {
        addItemMachineFragments("block.femtocraft.demolisher")
        addPowerFragments()
    }

    override fun canInsertSlot(index: Int, stack: ItemStack): Boolean = index == 0 && hasRecipe(stack)
    override fun hasRecipe(stack: ItemStack): Boolean = DustRecipes.hasResult(stack)
    override fun resultFor(stack: ItemStack): ItemStack = DustRecipes.result(stack)
}

/**
 * Machines powered by the crystal in their last slot (battery = the crystal's charge, trickle charged every tick),
 * also reachable as wired consumers. Port of v3's `TileCrystalFurnace`/`TileCrystalCrusher`.
 */
abstract class CrystalItemMachineBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) :
    ItemProcessingMachineBlockEntity(type, pos, state, 3) {
    override val battery: IBattery = DynamicIBattery { crystalBattery(inventory, CRYSTAL_SLOT) { markDirty() } }

    override fun canInsertSlot(index: Int, stack: ItemStack): Boolean = when (index) {
        0 -> hasRecipe(stack)
        CRYSTAL_SLOT -> stack.isEmpty || PowerCrystals.isCrystal(stack)
        else -> false
    }

    protected fun addCrystalFragments() {
        fragList.addFragment(FragPowerStorage({ battery }, persist = false))
        fragList.addFragment(FragDerivedColor { PowerCrystals.data(inventory.get(CRYSTAL_SLOT).toMinecraft())?.let { Color(it.color) } ?: FragDerivedColor.NONE })
        fragList.addFragment(FragWiredPowerLeafNode({ battery }, PowerStorageNodeType.CONSUMER) { LEAF_TRANSFER_RATE })
    }

    override fun serverTick() {
        if (PowerCrystals.onTick(inventory.get(CRYSTAL_SLOT).toMinecraft())) markDirty()
        super.serverTick()
    }

    companion object {
        const val CRYSTAL_SLOT = 2
    }
}

class CrystalFurnaceBlockEntity(pos: BlockPos, state: BlockState) : CrystalItemMachineBlockEntity(IndustryContent.CRYSTAL_FURNACE_BE.get(), pos, state) {
    init {
        addItemMachineFragments("block.femtocraft.crystal_furnace")
        addCrystalFragments()
    }

    override fun hasRecipe(stack: ItemStack): Boolean = !Smelting.result(level, stack).isEmpty
    override fun resultFor(stack: ItemStack): ItemStack = Smelting.result(level, stack)
}

class CrystalCrusherBlockEntity(pos: BlockPos, state: BlockState) : CrystalItemMachineBlockEntity(IndustryContent.CRYSTAL_CRUSHER_BE.get(), pos, state) {
    init {
        addItemMachineFragments("block.femtocraft.crystal_crusher")
        addCrystalFragments()
    }

    override fun hasRecipe(stack: ItemStack): Boolean = DustRecipes.hasResult(stack)
    override fun resultFor(stack: ItemStack): ItemStack = DustRecipes.result(stack)
}

/**
 * Turns items into fluid ([LiquifierRecipes]) in an 8000 mB tank; the crystal is in slot 1. Port of v3's
 * `TileCrystalLiquifier`: items enter from the top and back, fluid leaves from the other faces.
 */
class CrystalLiquifierBlockEntity(pos: BlockPos, state: BlockState) : ProcessingMachineBlockEntity(IndustryContent.CRYSTAL_LIQUIFIER_BE.get(), pos, state, 2) {
    override val battery: IBattery = DynamicIBattery { crystalBattery(inventory, CRYSTAL_SLOT) { markDirty() } }

    @JvmField
    val tank = FluidStorageArray(1, TANK_SIZE) { markDirty() }

    private var pending: FluidStack = FluidStack.EMPTY

    init {
        fragList.addFragment(FragSidedConfiguration(
            "ItemConfig",
            SidedItemStorageConfiguration({ if (it == Direction.UP || it == Direction.SOUTH) INPUT else NONE }, mapOf(NONE to IItemStorage.Empty, INPUT to input)) { HorizontalFacing.front(blockState) },
            Modules.ITEM_STORAGE_CONFIGURABLE,
        ))
        fragList.addFragment(FragSidedConfiguration(
            "FluidConfig",
            SidedFluidStorageConfiguration({ if (it == Direction.UP || it == Direction.SOUTH) NONE else OUTPUT }, mapOf(NONE to IFluidStorage.Empty, OUTPUT to tank)) { HorizontalFacing.front(blockState) },
            Modules.FLUID_STORAGE_CONFIGURABLE,
        ))
        fragList.addItemStorage(FragItemStorage(inventory))
        fragList.addFluidStorage(FragFluidStorage(tank))
        fragList.addInternalFragment(FragDropInventory(inventory))
        fragList.addTickableFragment(FragItemAutoIO())
        fragList.addTickableFragment(FragFluidAutoIO())
        fragList.addFragment(FragPowerStorage({ battery }, persist = false))
        fragList.addFragment(FragDerivedColor { PowerCrystals.data(inventory.get(CRYSTAL_SLOT).toMinecraft())?.let { Color(it.color) } ?: FragDerivedColor.NONE })
        fragList.addFragment(FragWiredPowerLeafNode({ battery }, PowerStorageNodeType.CONSUMER) { LEAF_TRANSFER_RATE })
        fragList.addFragment(FragMenu(Component.translatable("block.femtocraft.crystal_liquifier"), { id, inv, _ -> MachineMenu(id, inv, this) }))
    }

    override fun canInsertSlot(index: Int, stack: ItemStack): Boolean = when (index) {
        0 -> hasRecipe(stack)
        CRYSTAL_SLOT -> stack.isEmpty || PowerCrystals.isCrystal(stack)
        else -> false
    }

    override fun hasRecipe(stack: ItemStack): Boolean = LiquifierRecipes.find(stack) != null

    override fun computeResult(stack: ItemStack): Boolean {
        pending = LiquifierRecipes.find(stack)?.output?.invoke() ?: FluidStack.EMPTY
        return !pending.isEmpty
    }

    override fun pushResult(): Boolean {
        val filled = tank.fill(IFluidStack.of(pending), true)
        pending = pending.copyWithAmount(pending.amount - filled)
        return pending.isEmpty
    }

    override fun clearResult() {
        pending = FluidStack.EMPTY
    }

    override fun saveResult(output: ValueOutput) = output.store(PENDING_KEY, FluidStack.OPTIONAL_CODEC, pending)

    override fun loadResult(input: ValueInput) {
        pending = input.read(PENDING_KEY, FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY)
    }

    override fun serverTick() {
        if (PowerCrystals.onTick(inventory.get(CRYSTAL_SLOT).toMinecraft())) markDirty()
        super.serverTick()
    }

    companion object {
        const val CRYSTAL_SLOT = 1
        const val TANK_SIZE = 8000
    }
}

class NanoFurnaceBlock(p: BlockBehaviour.Properties) : FemtoHorizontalEntityBlock<NanoFurnaceBlockEntity>(p, { IndustryContent.NANO_FURNACE_BE.get() })
class DemolisherBlock(p: BlockBehaviour.Properties) : FemtoHorizontalEntityBlock<DemolisherBlockEntity>(p, { IndustryContent.DEMOLISHER_BE.get() })
class CrystalFurnaceBlock(p: BlockBehaviour.Properties) : FemtoHorizontalEntityBlock<CrystalFurnaceBlockEntity>(p, { IndustryContent.CRYSTAL_FURNACE_BE.get() })
class CrystalCrusherBlock(p: BlockBehaviour.Properties) : FemtoHorizontalEntityBlock<CrystalCrusherBlockEntity>(p, { IndustryContent.CRYSTAL_CRUSHER_BE.get() })
class CrystalLiquifierBlock(p: BlockBehaviour.Properties) : FemtoHorizontalEntityBlock<CrystalLiquifierBlockEntity>(p, { IndustryContent.CRYSTAL_LIQUIFIER_BE.get() })
