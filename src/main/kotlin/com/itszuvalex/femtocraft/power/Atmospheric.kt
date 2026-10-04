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
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.ScheduledTickAccess
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

/**
 * What can be stacked on an atmospheric charging base (the 1.7.10 alpha's `IAtmosphericChargingAddon`): coils and the
 * capacitor that caps the pole.
 */
abstract class AtmosphericAddonBlock(properties: BlockBehaviour.Properties, private val shape: VoxelShape) : Block(properties) {
    /** Whether another addon may stand on this one. */
    abstract val supportsAddons: Boolean

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = shape

    /** On the base or an addon that supports more, with air on all four sides (as in the alpha). */
    override fun canSurvive(state: BlockState, level: LevelReader, pos: BlockPos): Boolean {
        val below = level.getBlockState(pos.below()).block
        val supported = below is AtmosphericChargingBaseBlock || (below is AtmosphericAddonBlock && below.supportsAddons)
        return supported && HORIZONTAL.all { level.getBlockState(pos.relative(it)).isAir }
    }

    override fun updateShape(
        state: BlockState,
        level: LevelReader,
        ticks: ScheduledTickAccess,
        pos: BlockPos,
        directionToNeighbour: Direction,
        neighbourPos: BlockPos,
        neighbourState: BlockState,
        random: RandomSource,
    ): BlockState {
        if (!state.canSurvive(level, pos)) ticks.scheduleTick(pos, this, 1)
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random)
    }

    /** Breaks (dropping itself) once it can no longer stand, as the alpha's did. */
    override fun tick(state: BlockState, level: ServerLevel, pos: BlockPos, random: RandomSource) {
        if (!state.canSurvive(level, pos)) level.destroyBlock(pos, true)
    }

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        val box = shape.bounds()
        level.addParticle(com.itszuvalex.femtocraft.core.FemtoParticles.power(ConduitTier.CRYSTAL.particleColor),
            pos.x + box.minX + random.nextDouble() * box.xsize, pos.y + box.minY + random.nextDouble() * box.ysize, pos.z + box.minZ + random.nextDouble() * box.zsize,
            0.0, 0.0, 0.0)
    }

    companion object {
        val HORIZONTAL = listOf(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
    }
}

/**
 * A coil: 0.1 DE a tick (the alpha's value). A pole takes at most [AtmosphericChargingBaseBlockEntity.MAX_COILS] coils;
 * only a capacitor goes above the last.
 */
class AtmosphericChargingCoilBlock(properties: BlockBehaviour.Properties) : AtmosphericAddonBlock(properties, box(4.0, 0.0, 4.0, 12.0, 16.0, 12.0)) {
    override val supportsAddons: Boolean get() = true

    override fun canSurvive(state: BlockState, level: LevelReader, pos: BlockPos): Boolean =
        super.canSurvive(state, level, pos) && coilsBelow(level, pos) < AtmosphericChargingBaseBlockEntity.MAX_COILS

    companion object {
        const val POWER_PER_TICK = 0.1

        /** The coils stacked directly under [pos]. */
        fun coilsBelow(level: LevelReader, pos: BlockPos): Int {
            var n = 0
            while (n <= AtmosphericChargingBaseBlockEntity.MAX_COILS && level.getBlockState(pos.below(n + 1)).block is AtmosphericChargingCoilBlock) n++
            return n
        }
    }
}

/**
 * The capacitor that caps the pole: adds a share of the coils' power below it (the alpha's 20%, 40% in rain, 80% in a
 * thunderstorm; rain and storm only count where the capacitor is out in the weather), and in a thunderstorm draws
 * lightning onto itself for a burst ([AtmosphericChargingBaseBlockEntity.STRIKE_POWER]). Nothing stands on it.
 */
class AtmosphericChargingCapacitorBlock(properties: BlockBehaviour.Properties) : AtmosphericAddonBlock(properties, box(2.0, 0.0, 2.0, 14.0, 14.0, 14.0)) {
    override val supportsAddons: Boolean get() = false

    companion object {
        const val MULTIPLIER = 0.2
        const val MULTIPLIER_RAIN = 0.4
        const val MULTIPLIER_STORM = 0.8
    }
}

/**
 * The atmospheric charging base (the 1.7.10 alpha's, micro tier): sums the power of the addons stacked on it (up to
 * [MAX_COILS] coils, and a capacitor on top) into its battery, a producer on the wireless and wired power networks. Two bases may not stand side by
 * side. During a thunderstorm a capped pole out in the weather is struck by lightning now and then (about once every
 * [STRIKE_CHANCE] ticks): the bolt lands on the capacitor's top, harms nothing and sets no fire, and gives
 * [STRIKE_POWER].
 */
class AtmosphericChargingBaseBlockEntity(pos: BlockPos, state: BlockState) : FemtoBlockEntity(PowerContent.ATMOSPHERIC_BASE_BE.get(), pos, state) {
    @JvmField
    val battery = PowerBattery(POWER_STORAGE) { markDirty() }

    @JvmField
    val leaf = FragWirelessPowerLeafNode({ battery }, PowerStorageNodeType.PRODUCER, rate = { LEAF_TRANSFER_RATE })

    private var fraction = 0.0

    /** Updated every tick, for the screen. */
    var coils = 0
        private set
    var capped = false
        private set
    var powerPerTick = 0.0
        private set
    var strikes = 0
        private set

    init {
        fragList.addFragment(FragPowerStorage({ battery }))
        fragList.addWirelessLeaf(leaf)
        fragList.addFragment(FragDerivedColor { parentColor(this, leaf) })
        fragList.addFragment(FragWiredPowerLeafNode({ battery }, PowerStorageNodeType.PRODUCER) { LEAF_TRANSFER_RATE })
        fragList.addFragment(FragMenu(Component.translatable("block.femtocraft.atmospheric_charging_base"), { id, inv, _ -> AtmosphericChargingBaseMenu(id, inv, this) }))
        fragList.addInternalFragment(FragData("Atmospheric", FragData.LEVEL, { _, o ->
            o.putDouble("Fraction", fraction)
            o.putInt("Strikes", strikes)
        }, { _, i ->
            fraction = i.getDoubleOr("Fraction", 0.0)
            strikes = i.getIntOr("Strikes", 0)
        }))
    }

    /** The capacitor capping the pole, if there is one. */
    fun capacitorPos(): BlockPos? = if (capped) blockPos.above(coils + 1) else null

    override fun serverTick() {
        val level = level as? ServerLevel ?: return
        coils = 0
        capped = false
        for (up in 1..MAX_COILS + 1) {
            when (level.getBlockState(blockPos.above(up)).block) {
                is AtmosphericChargingCoilBlock -> if (coils < MAX_COILS) coils++ else break
                is AtmosphericChargingCapacitorBlock -> {
                    capped = true
                    break
                }
                else -> break
            }
        }
        val coilPower = coils * AtmosphericChargingCoilBlock.POWER_PER_TICK
        val top = capacitorPos()
        val exposed = top != null && level.isRainingAt(top.above())
        powerPerTick = coilPower + if (top == null) 0.0 else coilPower * when {
            exposed && level.isThundering -> AtmosphericChargingCapacitorBlock.MULTIPLIER_STORM
            exposed -> AtmosphericChargingCapacitorBlock.MULTIPLIER_RAIN
            else -> AtmosphericChargingCapacitorBlock.MULTIPLIER
        }
        fraction += powerPerTick
        if (fraction >= 1.0) {
            val whole = kotlin.math.floor(fraction)
            battery.fill(whole)
            fraction -= whole
        }
        if (top != null && exposed && level.isThundering && level.random.nextInt(STRIKE_CHANCE) == 0) strike(level, top)
    }

    /**
     * Strikes the capacitor at [top] with a harmless bolt (visual only: no fire, no damage) and stores its power.
     */
    fun strike(level: ServerLevel, top: BlockPos) {
        val bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED) ?: return
        bolt.setVisualOnly(true)
        bolt.snapTo(Vec3(top.x + 0.5, top.y + CAPACITOR_HEIGHT, top.z + 0.5))
        level.addFreshEntity(bolt)
        battery.fill(STRIKE_POWER)
        strikes++
        markDirty()
    }

    companion object {
        /**
         * The alpha's base held 250 DE; it holds enough here for a lightning strike.
         */
        const val POWER_STORAGE = 2500.0
        const val LEAF_TRANSFER_RATE = 20.0

        /** The alpha's `maxAddonsSupported`, counting coils: the capacitor may still cap the pole above the last. */
        const val MAX_COILS = 10
        const val STRIKE_POWER = 1000.0

        /** One in this many ticks, during a thunderstorm: about once a minute. */
        const val STRIKE_CHANCE = 1200

        /** The capacitor's top, in blocks above its base. */
        const val CAPACITOR_HEIGHT = 14.0 / 16.0
    }
}

class AtmosphericChargingBaseBlock(properties: BlockBehaviour.Properties) :
    FemtoEntityBlock<AtmosphericChargingBaseBlockEntity>(properties, { PowerContent.ATMOSPHERIC_BASE_BE.get() }) {
    /** Not beside another base (as in the alpha). */
    override fun canSurvive(state: BlockState, level: LevelReader, pos: BlockPos): Boolean =
        AtmosphericAddonBlock.HORIZONTAL.none { level.getBlockState(pos.relative(it)).block is AtmosphericChargingBaseBlock }
}

/**
 * The atmospheric charging base's menu: battery, coils, whether the pole is capped, power per tick and strikes taken.
 */
class AtmosphericChargingBaseMenu(containerId: Int, inventory: Inventory, be: AtmosphericChargingBaseBlockEntity?) :
    FemtoMenu<AtmosphericChargingBaseBlockEntity>(PowerContent.ATMOSPHERIC_BASE_MENU.get(), containerId, inventory, be) {
    @JvmField
    var battery = EnergyView()

    var coils = 0
    var capped = false
    var powerPerTick = 0.0
    var strikes = 0

    init {
        addPlayerInventorySlots(inventory)
        battery = syncEnergy { be?.battery }
        addSync(MenuSyncs.int({ be?.coils ?: 0 }, { coils = it }))
        addSync(MenuSyncs.boolean({ be?.capped ?: false }, { capped = it }))
        addSync(MenuSyncs.double({ be?.powerPerTick ?: 0.0 }, { powerPerTick = it }))
        addSync(MenuSyncs.int({ be?.strikes ?: 0 }, { strikes = it }))
    }
}
