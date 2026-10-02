package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.core.FemtoBlockEntity
import com.itszuvalex.femtocraft.core.FemtoEntityBlock
import com.itszuvalex.femtocraft.core.FemtoHorizontalEntityBlock
import com.itszuvalex.femtocraft.core.FragData
import com.itszuvalex.femtocraft.core.FragDerivedColor
import com.itszuvalex.itszulib.api.Modules
import com.itszuvalex.itszulib.api.adapters.IBattery
import com.itszuvalex.itszulib.api.adapters.IItemStack
import com.itszuvalex.itszulib.api.storage.IItemStorage
import com.itszuvalex.itszulib.api.storage.ItemStorageArray
import com.itszuvalex.itszulib.api.storage.PowerBattery
import com.itszuvalex.itszulib.api.utility.NBTSerializationScope
import com.itszuvalex.itszulib.core.frag.FragDropInventory
import com.itszuvalex.itszulib.core.frag.FragItemStorage
import com.itszuvalex.itszulib.core.frag.FragMenu
import com.itszuvalex.itszulib.core.frag.addItemStorage
import com.itszuvalex.itszulib.util.Color
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import com.itszuvalex.femtocraft.core.ConduitArms
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.ScheduledTickAccess
import net.minecraft.util.RandomSource
import net.minecraft.core.Direction as MountDirection
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import kotlin.random.Random

/**
 * Item storage that only accepts power crystals (v3's `canInsert` on every crystal slot).
 */
open class CrystalStorage(size: Int, onChanged: Runnable) : ItemStorageArray(size, onChanged) {
    override fun canInsert(index: Int, stack: IItemStack): Boolean = stack.isEmpty() || PowerCrystals.isCrystal(stack.toMinecraft())
}

/**
 * The battery of the crystal in [storage] slot [index], or [IBattery.Empty].
 */
fun crystalBattery(storage: IItemStorage, index: Int, onChanged: Runnable): IBattery =
    PowerCrystals.battery(storage.get(index).toMinecraft(), onChanged) ?: IBattery.Empty

/**
 * Moves up to the crystal's transfer rate from the crystal in [stack] into [battery], never more than fits. v3 drained
 * the crystal's full rate and clamped the battery, destroying whatever did not fit.
 *
 * @return Power moved.
 */
fun drainCrystalInto(stack: ItemStack, battery: IBattery): Double {
    val data = PowerCrystals.data(stack) ?: return 0.0
    val crystal = PowerCrystals.battery(stack) ?: return 0.0
    val amount = minOf(data.storage, data.transferRate, battery.room())
    if (amount <= 0) return 0.0
    return crystal.drain(battery.fill(amount))
}

/**
 * The color of a wireless leaf's parent node, or [FragDerivedColor.NONE] (v3's `ModuleColorableFromPowerLeafNode`).
 */
fun parentColor(be: BlockEntity, leaf: IWirelessPowerLeafNode): Color {
    val level = be.level ?: return FragDerivedColor.NONE
    val parent = leaf.parent ?: return FragDerivedColor.NONE
    if (!level.isLoaded(parent.pos)) return FragDerivedColor.NONE
    return (level.getBlockEntity(parent.pos) as? com.itszuvalex.itszulib.api.adapters.IBlockEntity)
        ?.getModule(Modules.COLORABLE, null)?.getColor() ?: FragDerivedColor.NONE
}

/**
 * The wireless network a power block belongs to: its own node's, or its leaf parent's. For the network screen.
 */
fun wirelessNetworkOf(be: BlockEntity): WirelessPowerNetwork? {
    val ibe = be as? com.itszuvalex.itszulib.api.adapters.IBlockEntity ?: return null
    ibe.getModule(PowerModules.WIRELESS_NODE, null)?.let { return it.getNetwork() }
    val leaf = ibe.getModule(PowerModules.WIRELESS_LEAF, null) ?: return null
    val parent = leaf.parent ?: return null
    return WirelessPowerManager.nodeAt(leaf.storageLoc, parent)?.getNetwork()
}

// --- Crystal mount ---------------------------------------------------------------------------------------------------

/**
 * Holds one power crystal and is a wireless power node with the crystal as its storage. Port of v3's
 * `BlockCrystalMount`/`TileCrystalMount`: radius [RANGE], transfer rate the crystal's, trickle charges the crystal.
 */
/**
 * The crystal mount. Its model draws the bottom half when a solid face is below it (or nothing solid is above), and
 * the top half when a solid face is above it, as v3's renderer did; [TOP] and [BOTTOM] follow the neighbours.
 */
class CrystalMountBlock(properties: BlockBehaviour.Properties) :
    FemtoEntityBlock<CrystalMountBlockEntity>(properties, { PowerContent.CRYSTAL_MOUNT_BE.get() }) {
    init {
        registerDefaultState(stateDefinition.any().setValue(TOP, false).setValue(BOTTOM, true))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(TOP, BOTTOM)
    }

    override fun getStateForPlacement(context: BlockPlaceContext): BlockState = withMounts(defaultBlockState(), context.level, context.clickedPos)

    override fun updateShape(
        state: BlockState, level: LevelReader, ticks: ScheduledTickAccess, pos: BlockPos, direction: MountDirection,
        neighborPos: BlockPos, neighborState: BlockState, random: RandomSource,
    ): BlockState = if (direction.axis == MountDirection.Axis.Y) withMounts(state, level, pos) else state

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = SHAPE

    /**
     * A power particle near the crystal, in its color (v3 `BlockCrystalMount.randomDisplayTick`).
     */
    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        val color = (level.getBlockEntity(pos) as? CrystalMountBlockEntity)?.crystal()?.color ?: return
        level.addParticle(com.itszuvalex.femtocraft.core.FemtoParticles.power(color),
            pos.x + .5 + random.nextDouble() * .2 - .1, pos.y + .5 + random.nextDouble() * .2 - .1, pos.z + .5 + random.nextDouble() * .2 - .1, 0.0, 0.0, 0.0)
    }

    companion object {
        @JvmField
        val TOP: BooleanProperty = BooleanProperty.create("top")

        @JvmField
        val BOTTOM: BooleanProperty = BooleanProperty.create("bottom")

        fun withMounts(state: BlockState, level: BlockGetter, pos: BlockPos): BlockState = state
            .setValue(TOP, level.getBlockState(pos.above()).isFaceSturdy(level, pos.above(), MountDirection.DOWN))
            .setValue(BOTTOM, level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), MountDirection.UP))

        private val SHAPE: VoxelShape = Shapes.or(box(2.0, 0.0, 2.0, 14.0, 6.4, 14.0), box(6.4, 4.8, 6.4, 9.6, 11.2, 9.6))
    }
}

class CrystalMountBlockEntity(pos: BlockPos, state: BlockState) : FemtoBlockEntity(PowerContent.CRYSTAL_MOUNT_BE.get(), pos, state) {
    @JvmField
    val storage = object : CrystalStorage(1, { markDirtyAndSync() }) {
        override fun maxStackSize(index: Int): Int = 1
    }

    @JvmField
    val battery: IBattery = com.itszuvalex.itszulib.api.storage.DynamicIBattery { crystalBattery(storage, 0) { markDirty() } }

    fun crystal(): PowerCrystalData? = PowerCrystals.data(storage.get(0).toMinecraft())

    fun transferRate(): Double = crystal()?.transferRate ?: 0.0

    @JvmField
    val node = FragWirelessPowerNode({ RANGE }, ::transferRate)

    init {
        fragList.addItemStorage(FragItemStorage(storage))
        // Clients see the crystal (color, future crystal renderer), as v3 synced it in the description packet.
        fragList.addInternalFragment(FragData("CrystalSync", setOf(NBTSerializationScope.DESCRIPTION), { _, o -> storage.serialize(o) }, { _, i -> storage.deserialize(i) }))
        fragList.addInternalFragment(FragDropInventory(storage))
        fragList.addFragment(FragPowerStorage({ battery }, persist = false))
        fragList.addFragment(FragWirelessPowerStorageNode({ battery }, PowerStorageNodeType.STORAGE, ::transferRate))
        fragList.addFragment(node)
        fragList.addFragment(FragDerivedColor { crystal()?.let { Color(it.color) } ?: FragDerivedColor.NONE })
        fragList.addFragment(FragMenu(Component.translatable("block.femtocraft.crystal_mount"), { id, inv, _ -> CrystalMountMenu(id, inv, this) }))
    }

    override fun serverTick() {
        if (PowerCrystals.onTick(storage.get(0).toMinecraft())) markDirty()
    }

    companion object {
        const val RANGE = 8f
    }
}

// --- Crystal arrays and heat exchanger -------------------------------------------------------------------------------

/**
 * Common shape of the three crystal machines: a crystal inventory, a battery, a wireless leaf and a color taken from
 * the leaf's parent.
 */
abstract class CrystalMachineBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) :
    FemtoBlockEntity(type, pos, state) {
    abstract val battery: IBattery

    abstract val storage: IItemStorage

    protected abstract val leaf: FragWirelessPowerLeafNode

    /**
     * Power generated per tick, for the screen.
     */
    abstract fun powerPerTick(): Double

    protected fun addCommonFragments(type: PowerStorageNodeType, title: String) {
        fragList.addItemStorage(FragItemStorage(storage))
        fragList.addInternalFragment(FragDropInventory(storage))
        fragList.addFragment(FragPowerStorage({ battery }))
        fragList.addWirelessLeaf(leaf)
        fragList.addFragment(FragDerivedColor { parentColor(this, leaf) })
        fragList.addFragment(FragMenu(Component.translatable(title), { id, inv, _ -> CrystalMachineMenu(id, inv, this) }))
    }

    companion object {
        const val LEAF_TRANSFER_RATE = 50.0
    }
}

/**
 * Drains its crystals into its battery and adds twice their passive generation; a producer on both the wireless and
 * the wired network. Port of v3's `TileCrystalChargingArray`.
 */
class CrystalChargingArrayBlockEntity(pos: BlockPos, state: BlockState) :
    CrystalMachineBlockEntity(PowerContent.CRYSTAL_CHARGING_ARRAY_BE.get(), pos, state) {
    override val storage: IItemStorage = CrystalStorage(6) { markDirty() }
    override val battery: IBattery = PowerBattery(POWER_STORAGE.toDouble()) { markDirty() }
    override val leaf = FragWirelessPowerLeafNode({ battery }, PowerStorageNodeType.PRODUCER, rate = { LEAF_TRANSFER_RATE })

    init {
        addCommonFragments(PowerStorageNodeType.PRODUCER, "block.femtocraft.crystal_charging_array")
        fragList.addFragment(FragWiredPowerLeafNode({ battery }, PowerStorageNodeType.PRODUCER) { LEAF_TRANSFER_RATE })
    }

    override fun powerPerTick(): Double = (0 until storage.size()).sumOf { (PowerCrystals.data(storage.get(it).toMinecraft())?.passiveGen ?: 0f) * PASSIVE_GEN_MULTIPLIER.toDouble() }

    override fun serverTick() {
        for (i in 0 until storage.size()) {
            val stack = storage.get(i).toMinecraft()
            val data = PowerCrystals.data(stack) ?: continue
            if (drainCrystalInto(stack, battery) > 0) markDirty()
            battery.fill(data.passiveGen.toDouble() * PASSIVE_GEN_MULTIPLIER)
        }
    }

    companion object {
        const val PASSIVE_GEN_MULTIPLIER = 2
        const val POWER_STORAGE = 10000
    }
}

/**
 * Battery sized by its crystals (twice their total capacity), filled by draining them; wireless storage. Port of v3's
 * `TileCrystalStorageArray`. Removing crystals can leave the battery above its new capacity, as in v3; it then only
 * gives power until it is back under.
 */
class CrystalStorageArrayBlockEntity(pos: BlockPos, state: BlockState) :
    CrystalMachineBlockEntity(PowerContent.CRYSTAL_STORAGE_ARRAY_BE.get(), pos, state) {
    override val storage: IItemStorage = CrystalStorage(6) { markDirty() }

    override val battery: IBattery = object : IBattery {
        private var stored = 0.0
        override fun storage(): Double = stored
        override fun setStorage(storage: Double) {
            stored = storage.coerceAtLeast(0.0)
            markDirty()
        }

        override fun setStorageQuietly(storage: Double) {
            stored = storage.coerceAtLeast(0.0)
        }

        override fun maxStorage(): Double =
            (0 until this@CrystalStorageArrayBlockEntity.storage.size()).sumOf { PowerCrystals.data(this@CrystalStorageArrayBlockEntity.storage.get(it).toMinecraft())?.maxStorage ?: 0.0 } * STORAGE_MULTIPLIER

        override fun room(): Double = (maxStorage() - stored).coerceAtLeast(0.0)
        override fun serialize(output: ValueOutput) = output.putDouble("P", stored)
        override fun deserialize(input: ValueInput) {
            stored = input.getDoubleOr("P", 0.0)
        }
    }

    override val leaf = FragWirelessPowerLeafNode({ battery }, PowerStorageNodeType.STORAGE, rate = { LEAF_TRANSFER_RATE })

    init {
        addCommonFragments(PowerStorageNodeType.STORAGE, "block.femtocraft.crystal_storage_array")
    }

    override fun powerPerTick(): Double = 0.0

    override fun serverTick() {
        for (i in 0 until storage.size()) {
            if (drainCrystalInto(storage.get(i).toMinecraft(), battery) > 0) markDirty()
        }
    }

    companion object {
        const val STORAGE_MULTIPLIER = 2.0
    }
}

/**
 * Burns fuel; while burning, drains its crystal into its battery and adds ten times the crystal's passive
 * generation. Port of v3's `TileCrystalHeatExchanger` (slot 0 fuel, slot 1 crystal; burn time is half the vanilla
 * value). Saves `Burn`/`BurnMax`.
 */
class CrystalHeatExchangerBlockEntity(pos: BlockPos, state: BlockState) :
    CrystalMachineBlockEntity(PowerContent.CRYSTAL_HEAT_EXCHANGER_BE.get(), pos, state) {
    override val storage: IItemStorage = object : ItemStorageArray(2, { markDirty() }) {
        override fun canInsert(index: Int, stack: IItemStack): Boolean = stack.isEmpty() || when (index) {
            CRYSTAL_INDEX -> PowerCrystals.isCrystal(stack.toMinecraft())
            FUEL_INDEX -> burnDuration(stack.toMinecraft()) > 0
            else -> false
        }
    }
    override val battery: IBattery = PowerBattery(POWER_STORAGE.toDouble()) { markDirty() }
    override val leaf = FragWirelessPowerLeafNode({ battery }, PowerStorageNodeType.PRODUCER, rate = { LEAF_TRANSFER_RATE })

    var burnTime = 0
    var burnMax = 0

    init {
        addCommonFragments(PowerStorageNodeType.PRODUCER, "block.femtocraft.crystal_heat_exchanger")
        fragList.addInternalFragment(FragData("Burn", FragData.LEVEL, { _, o ->
            o.putInt(BURN_TIME_KEY, burnTime)
            o.putInt(BURN_MAX_KEY, burnMax)
        }, { _, i ->
            burnTime = i.getIntOr(BURN_TIME_KEY, 0)
            burnMax = i.getIntOr(BURN_MAX_KEY, 0)
        }))
    }

    private fun burnDuration(stack: ItemStack): Int = level?.fuelValues()?.burnDuration(stack) ?: 0

    override fun powerPerTick(): Double = (PowerCrystals.data(storage.get(CRYSTAL_INDEX).toMinecraft())?.passiveGen ?: 0f) * CHARGING_MULTIPLIER

    override fun serverTick() {
        val crystal = storage.get(CRYSTAL_INDEX).toMinecraft()
        if (burnTime > 0) {
            if (PowerCrystals.isCrystal(crystal)) {
                drainCrystalInto(crystal, battery)
                battery.fill(powerPerTick())
            }
            burnTime--
            markDirty()
        }
        if (burnTime <= 0 && battery.room() > 0 && PowerCrystals.isCrystal(crystal)) {
            val fuel = storage.get(FUEL_INDEX).toMinecraft()
            val duration = burnDuration(fuel)
            if (duration > 0) {
                burnTime = (duration * BURN_TIME_MULTIPLIER).toInt()
                burnMax = burnTime
                val remainder = fuel.item.getCraftingRemainder(fuel)?.create() ?: ItemStack.EMPTY
                storage.split(FUEL_INDEX, 1)
                // Keep a bucket from a lava bucket (v3 lost it).
                if (storage.get(FUEL_INDEX).isEmpty() && !remainder.isEmpty) storage.setSlot(FUEL_INDEX, IItemStack.of(remainder.copy()))
            }
        }
    }

    companion object {
        const val POWER_STORAGE = 10000
        const val CHARGING_MULTIPLIER = 10.0
        const val BURN_TIME_MULTIPLIER = 0.5
        const val FUEL_INDEX = 0
        const val CRYSTAL_INDEX = 1
        const val BURN_TIME_KEY = "Burn"
        const val BURN_MAX_KEY = "BurnMax"
    }
}

class CrystalChargingArrayBlock(properties: BlockBehaviour.Properties) :
    FemtoHorizontalEntityBlock<CrystalChargingArrayBlockEntity>(properties, { PowerContent.CRYSTAL_CHARGING_ARRAY_BE.get() })

class CrystalStorageArrayBlock(properties: BlockBehaviour.Properties) :
    FemtoHorizontalEntityBlock<CrystalStorageArrayBlockEntity>(properties, { PowerContent.CRYSTAL_STORAGE_ARRAY_BE.get() })

class CrystalHeatExchangerBlock(properties: BlockBehaviour.Properties) :
    FemtoHorizontalEntityBlock<CrystalHeatExchangerBlockEntity>(properties, { PowerContent.CRYSTAL_HEAT_EXCHANGER_BE.get() })

// --- Conduit and glow stick ------------------------------------------------------------------------------------------

/**
 * Crystal-tier power conduit. Port of v3's `BlockPowerConduitCrystal`/`TilePowerConduitCrystal`. The model is the
 * conduit core only; arms towards connections are rendering follow-up work.
 */
class PowerConduitBlock(properties: BlockBehaviour.Properties) :
    FemtoEntityBlock<PowerConduitBlockEntity>(properties, { PowerContent.POWER_CONDUIT_BE.get() }) {
    init {
        registerDefaultState(ConduitArms.withoutArms(stateDefinition.any()))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) = ConduitArms.addProperties(builder)

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = ConduitArms.shape(state)

    /**
     * A power particle somewhere inside the conduit and its arms, in its tier's colour, as the 1.7.10 alpha's cables
     * gave off (`BlockMicroCable.randomDisplayTick`).
     */
    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: net.minecraft.util.RandomSource) {
        val tier = (level.getBlockEntity(pos) as? PowerConduitBlockEntity)?.conduit?.tier ?: return
        val box = ConduitArms.shape(state).bounds()
        level.addParticle(com.itszuvalex.femtocraft.core.FemtoParticles.power(tier.particleColor),
            pos.x + box.minX + random.nextDouble() * box.xsize, pos.y + box.minY + random.nextDouble() * box.ysize, pos.z + box.minZ + random.nextDouble() * box.zsize,
            0.0, 0.0, 0.0)
    }
}

class PowerConduitBlockEntity(pos: BlockPos, state: BlockState) : FemtoBlockEntity(PowerContent.POWER_CONDUIT_BE.get(), pos, state) {
    @JvmField
    val conduit = WiredPowerConduit(ConduitTier.CRYSTAL)

    init {
        fragList.addFragment(conduit)
    }

    /**
     * Arms towards connected conduits and attached leaves (the model's, see [ConduitArms]).
     */
    override fun serverTick() = ConduitArms.sync(this) { conduit.isConnected(it) || conduit.leafFaces[it] }
}

/**
 * A light source with a random pale color. Port of v3's `BlockGlowStick`/`TileGlowStick` (color saved and synced as
 * `color`); no collision.
 */
class GlowStickBlock(properties: BlockBehaviour.Properties) :
    FemtoEntityBlock<GlowStickBlockEntity>(properties, { PowerContent.GLOW_STICK_BE.get() }) {
    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = SHAPE

    override fun getCollisionShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = Shapes.empty()

    override fun <E : BlockEntity> getTicker(level: Level, state: BlockState, type: BlockEntityType<E>): BlockEntityTicker<E>? = null

    companion object {
        private val SHAPE: VoxelShape = Block.box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0)
    }
}

class GlowStickBlockEntity(pos: BlockPos, state: BlockState) : FemtoBlockEntity(PowerContent.GLOW_STICK_BE.get(), pos, state) {
    var color: Int = Color(255.toByte(), (Random.nextInt(125) + 130).toByte(), (Random.nextInt(125) + 130).toByte(), (Random.nextInt(125) + 130).toByte()).toInt()
        private set

    init {
        fragList.addInternalFragment(FragData("Glow", FragData.LEVEL_AND_DESCRIPTION, { _, o -> o.putInt("color", color) }, { _, i -> color = i.getIntOr("color", color) }))
    }
}
