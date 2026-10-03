package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.core.FemtoBlockEntity
import com.itszuvalex.femtocraft.core.FemtoEntityBlock
import com.itszuvalex.femtocraft.core.FemtoMenu
import com.itszuvalex.femtocraft.core.FragData
import com.itszuvalex.femtocraft.core.FragDerivedColor
import com.itszuvalex.itszulib.api.storage.PowerBattery
import com.itszuvalex.itszulib.core.frag.FragMenu
import com.itszuvalex.itszulib.menu.EnergyView
import com.itszuvalex.itszulib.menu.MenuSyncs
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.tags.FluidTags
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random

/**
 * What a cryo-endothermal coil gets from a block: [power] if [canHandle], then [use] (an active handler changes the
 * block, e.g. freezes it). Port of the 1.7.10 alpha's `ICryogenHandler`.
 */
interface CryogenHandler {
    fun canHandle(level: Level, pos: BlockPos): Boolean

    fun power(level: Level, pos: BlockPos): Double

    fun use(level: Level, pos: BlockPos) {}
}

/**
 * The cryogen handlers coils consult: passive ones every tick for the four blocks beside a coil, active ones now and
 * then for a random block near it. Mods may add their own. Port of the alpha's `CryogenRegistry`, with its two default
 * handlers (`CryogenPassiveHandler`, `CryogenActiveHandler`) and their values.
 */
object CryogenRegistry {
    private val passive = CopyOnWriteArrayList<CryogenHandler>()
    private val active = CopyOnWriteArrayList<CryogenHandler>()

    const val ICE_PER_TICK = 5.0 / 4.0
    const val SNOW_PER_TICK = 2.0 / 4.0
    const val WATER_TO_ICE = 100.0
    const val LAVA_TO_OBSIDIAN = 300.0
    const val AIR_TO_SNOW_LAYER = 10.0

    /** Ice and snow blocks beside a coil, every tick; they are not used up. */
    @JvmField
    val PASSIVE_DEFAULT = object : CryogenHandler {
        override fun canHandle(level: Level, pos: BlockPos): Boolean = power(level, pos) > 0.0

        override fun power(level: Level, pos: BlockPos): Double {
            val state = level.getBlockState(pos)
            return when {
                state.`is`(Blocks.ICE) || state.`is`(Blocks.PACKED_ICE) || state.`is`(Blocks.BLUE_ICE) -> ICE_PER_TICK
                state.`is`(Blocks.SNOW_BLOCK) || state.`is`(Blocks.POWDER_SNOW) -> SNOW_PER_TICK
                else -> 0.0
            }
        }
    }

    /** Water sources freeze to ice, lava sources to obsidian, and air over a solid top gets a snow layer. */
    @JvmField
    val ACTIVE_DEFAULT = object : CryogenHandler {
        private fun snowable(level: Level, pos: BlockPos) =
            level.getBlockState(pos).isAir && Blocks.SNOW.defaultBlockState().canSurvive(level, pos)

        override fun canHandle(level: Level, pos: BlockPos): Boolean = power(level, pos) > 0.0

        override fun power(level: Level, pos: BlockPos): Double {
            val fluid = level.getFluidState(pos)
            return when {
                fluid.isSource && fluid.`is`(FluidTags.WATER) && level.getBlockState(pos).`is`(Blocks.WATER) -> WATER_TO_ICE
                fluid.isSource && fluid.`is`(FluidTags.LAVA) && level.getBlockState(pos).`is`(Blocks.LAVA) -> LAVA_TO_OBSIDIAN
                snowable(level, pos) -> AIR_TO_SNOW_LAYER
                else -> 0.0
            }
        }

        override fun use(level: Level, pos: BlockPos) {
            val fluid = level.getFluidState(pos)
            when {
                fluid.`is`(FluidTags.WATER) -> level.setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState())
                fluid.`is`(FluidTags.LAVA) -> level.setBlockAndUpdate(pos, Blocks.OBSIDIAN.defaultBlockState())
                snowable(level, pos) -> level.setBlockAndUpdate(pos, Blocks.SNOW.defaultBlockState())
            }
        }
    }

    init {
        passive += PASSIVE_DEFAULT
        active += ACTIVE_DEFAULT
    }

    fun registerPassive(handler: CryogenHandler) {
        passive += handler
    }

    fun registerActive(handler: CryogenHandler) {
        active += handler
    }

    fun passivePower(level: Level, pos: BlockPos): Double = power(level, pos, passive)

    fun activePower(level: Level, pos: BlockPos): Double = power(level, pos, active)

    /** The first willing handler's power for [pos], after letting it use the block. */
    private fun power(level: Level, pos: BlockPos, handlers: List<CryogenHandler>): Double {
        if (!level.isLoaded(pos)) return 0.0
        val handler = handlers.firstOrNull { it.canHandle(level, pos) } ?: return 0.0
        val amount = handler.power(level, pos)
        handler.use(level, pos)
        return amount
    }
}

/**
 * The cryo-endothermal charging base (the 1.7.10 alpha's): a 25,000 DE battery fed by the coils stacked beneath it (up
 * to [MAX_COILS]), a producer on the wireless and wired power networks.
 */
class CryoChargingBaseBlockEntity(pos: BlockPos, state: BlockState) : FemtoBlockEntity(PowerContent.CRYO_BASE_BE.get(), pos, state) {
    @JvmField
    val battery = PowerBattery(POWER_STORAGE) { markDirty() }

    @JvmField
    val leaf = FragWirelessPowerLeafNode({ battery }, PowerStorageNodeType.PRODUCER, rate = { LEAF_TRANSFER_RATE })

    /** Power received this tick, and an average over the last seconds for the screen. */
    private var received = 0.0
    var generation = 0.0
        private set

    init {
        fragList.addFragment(FragPowerStorage({ battery }))
        fragList.addWirelessLeaf(leaf)
        fragList.addFragment(FragDerivedColor { parentColor(this, leaf) })
        fragList.addFragment(FragWiredPowerLeafNode({ battery }, PowerStorageNodeType.PRODUCER) { LEAF_TRANSFER_RATE })
        fragList.addFragment(FragMenu(Component.translatable("block.femtocraft.cryo_endothermal_charging_base"), { id, inv, _ -> CryoChargingBaseMenu(id, inv, this) }))
    }

    /** Takes [power] from a coil; @return How much fitted. */
    fun addPower(power: Double): Double = battery.fill(power).also { received += it }

    /** Coils stacked directly beneath, up to [MAX_COILS]. */
    fun coils(): Int {
        val level = level ?: return 0
        var count = 0
        while (count < MAX_COILS) {
            val at = blockPos.below(count + 1)
            if (!level.isLoaded(at) || level.getBlockEntity(at) !is CryoChargingCoilBlockEntity) break
            count++
        }
        return count
    }

    override fun serverTick() {
        generation += (received - generation) * 0.05
        received = 0.0
    }

    companion object {
        const val POWER_STORAGE = 25000.0
        const val LEAF_TRANSFER_RATE = 100.0

        /** The alpha's `maximumDepth`. */
        const val MAX_COILS = 15
    }
}

/**
 * A cryo-endothermal charging coil (the 1.7.10 alpha's): every tick it draws power from ice and snow on its four
 * sides; every 1-10 seconds it also freezes a random block within [ACTIVE_RANGE] (water to ice, lava to obsidian, a snow
 * layer on open ground) for a burst. Its power goes up the coil stack to the base on top; without a base (or more than
 * [CryoChargingBaseBlockEntity.MAX_COILS] coils below it) it does nothing, and freezes nothing.
 */
class CryoChargingCoilBlockEntity(pos: BlockPos, state: BlockState) : FemtoBlockEntity(PowerContent.CRYO_COIL_BE.get(), pos, state) {
    private var stored = 0.0
    private var ticksToActive = 0

    init {
        fragList.addInternalFragment(FragData("Coil", FragData.LEVEL, { _, o ->
            o.putDouble("Stored", stored)
            o.putInt("TicksToActive", ticksToActive)
        }, { _, i ->
            stored = i.getDoubleOr("Stored", 0.0)
            ticksToActive = i.getIntOr("TicksToActive", 0)
        }))
    }

    /** The base on top of this coil's stack, if within reach. */
    fun base(): CryoChargingBaseBlockEntity? {
        val level = level ?: return null
        for (up in 1..CryoChargingBaseBlockEntity.MAX_COILS) {
            val at = blockPos.above(up)
            if (!level.isLoaded(at)) return null
            when (val be = level.getBlockEntity(at)) {
                is CryoChargingBaseBlockEntity -> return be
                is CryoChargingCoilBlockEntity -> continue
                else -> return null
            }
        }
        return null
    }

    override fun serverTick() {
        val level = level ?: return
        val base = base() ?: return
        for (face in HORIZONTAL) stored += CryogenRegistry.passivePower(level, blockPos.relative(face))
        if (ticksToActive <= 0) {
            ticksToActive = Random.nextInt(ACTIVE_MIN_TICKS, ACTIVE_MAX_TICKS)
            val target = blockPos.offset(
                Random.nextInt(-ACTIVE_RANGE, ACTIVE_RANGE + 1), Random.nextInt(-ACTIVE_RANGE, ACTIVE_RANGE + 1), Random.nextInt(-ACTIVE_RANGE, ACTIVE_RANGE + 1))
            stored += CryogenRegistry.activePower(level, target)
        } else {
            ticksToActive--
        }
        if (stored >= 1.0) {
            val whole = kotlin.math.floor(stored)
            base.addPower(whole)
            stored -= whole
            markDirty()
        }
    }

    companion object {
        const val ACTIVE_MIN_TICKS = 20
        const val ACTIVE_MAX_TICKS = 200
        const val ACTIVE_RANGE = 5
        private val HORIZONTAL = listOf(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
    }
}

class CryoChargingBaseBlock(properties: BlockBehaviour.Properties) :
    FemtoEntityBlock<CryoChargingBaseBlockEntity>(properties, { PowerContent.CRYO_BASE_BE.get() })

/** The coil: a 8x16x8 pillar. */
class CryoChargingCoilBlock(properties: BlockBehaviour.Properties) :
    FemtoEntityBlock<CryoChargingCoilBlockEntity>(properties, { PowerContent.CRYO_COIL_BE.get() }) {
    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = SHAPE

    companion object {
        private val SHAPE: VoxelShape = Block.box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0)
    }
}

/**
 * The charging base's menu: its battery, coil count and average generation.
 */
class CryoChargingBaseMenu(containerId: Int, inventory: Inventory, be: CryoChargingBaseBlockEntity?) :
    FemtoMenu<CryoChargingBaseBlockEntity>(PowerContent.CRYO_BASE_MENU.get(), containerId, inventory, be) {
    @JvmField
    var battery = EnergyView()

    var coils = 0
    var generation = 0.0

    init {
        addPlayerInventorySlots(inventory)
        battery = syncEnergy { be?.battery }
        addSync(MenuSyncs.int({ be?.coils() ?: 0 }, { coils = it }))
        addSync(MenuSyncs.double({ be?.generation ?: 0.0 }, { generation = it }))
    }
}
