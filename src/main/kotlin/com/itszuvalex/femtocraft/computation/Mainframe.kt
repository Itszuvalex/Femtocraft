package com.itszuvalex.femtocraft.computation

import com.itszuvalex.femtocraft.core.FemtoBlockEntity
import com.itszuvalex.femtocraft.core.FemtoHorizontalEntityBlock
import com.itszuvalex.femtocraft.core.FragData
import com.itszuvalex.femtocraft.core.FragDerivedColor
import com.itszuvalex.femtocraft.power.FragPowerStorage
import com.itszuvalex.femtocraft.power.FragWiredPowerLeafNode
import com.itszuvalex.femtocraft.power.FragWirelessPowerLeafNode
import com.itszuvalex.femtocraft.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.power.addWirelessLeaf
import com.itszuvalex.femtocraft.power.parentColor
import com.itszuvalex.itszulib.api.adapters.IItemStack
import com.itszuvalex.itszulib.api.storage.ItemStorageArray
import com.itszuvalex.itszulib.api.storage.PowerBattery
import com.itszuvalex.itszulib.core.Distributable
import com.itszuvalex.itszulib.core.DistributionRole
import com.itszuvalex.itszulib.core.frag.FragDropInventory
import com.itszuvalex.itszulib.core.frag.FragItemStorage
import com.itszuvalex.itszulib.core.frag.FragMenu
import com.itszuvalex.itszulib.core.frag.addItemStorage
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import java.util.function.Consumer
import kotlin.math.min

/**
 * A processor a mainframe runs: FLOPS per tick at full speed, the power each FLOP costs and the heat it gives off.
 */
enum class ProcessorTier(val flopsPerTick: Double, val powerPerFlop: Double, val heatPerFlop: Double) {
    /** The 1.7.10 alpha's Micro Logic Core: the first processor. */
    MICRO_LOGIC_CORE(20.0, 0.5, 0.01),

    /** The alpha's Orpheus Processor: three times the FLOPS, cheaper and cooler per FLOP. */
    ORPHEUS(60.0, 0.3, 0.008),
}

class ProcessorItem(val tier: ProcessorTier, properties: Properties) : Item(properties) {
    override fun appendHoverText(stack: ItemStack, context: TooltipContext, display: TooltipDisplay, tooltip: Consumer<Component>, flag: TooltipFlag) {
        tooltip.accept(Component.translatable("tooltip.femtocraft.processor.flops", fmt(tier.flopsPerTick)).withStyle(ChatFormatting.GRAY))
        tooltip.accept(Component.translatable("tooltip.femtocraft.processor.power", fmt(tier.powerPerFlop)).withStyle(ChatFormatting.GRAY))
    }

    companion object {
        fun tierOf(stack: ItemStack): ProcessorTier? = (stack.item as? ProcessorItem)?.tier

        private fun fmt(value: Double) = "%.2f".format(java.util.Locale.ROOT, value).trimEnd('0').trimEnd('.')
    }
}

/**
 * A mainframe's temperature (°C). Computing heats it ([heat]); every tick it loses a share of its excess over the
 * ambient temperature ([cool]), a larger share with cold blocks around it. Above [THROTTLE_START] it slows down
 * linearly, to a stop at [MAX] ([speed]), so an uncooled mainframe settles where heat in matches heat out.
 */
class MainframeHeat(var temperature: Double = DEFAULT_AMBIENT) {
    /** 1 at full speed, down to 0 at [MAX]. */
    fun speed(): Double = ((MAX - temperature) / (MAX - THROTTLE_START)).coerceIn(0.0, 1.0)

    fun heat(degrees: Double) {
        temperature += degrees
    }

    /**
     * Moves the temperature [rate] of the way to [ambient] ([rate] is [BASE_COOLING] plus the [coolant] around).
     */
    fun cool(ambient: Double, rate: Double) {
        temperature -= (temperature - ambient) * rate.coerceIn(0.0, 1.0)
    }

    companion object {
        const val DEFAULT_AMBIENT = 20.0
        const val THROTTLE_START = 60.0
        const val MAX = 100.0
        const val BASE_COOLING = 0.01

        /**
         * The ambient temperature of a biome with base temperature [biomeTemperature] (plains 0.8 is 20 °C, a snowy
         * biome near 0 °C, a desert 50 °C).
         */
        @JvmStatic
        fun ambient(biomeTemperature: Float): Double = DEFAULT_AMBIENT + (biomeTemperature - 0.8) * 25.0

        /** How much [state] next to a mainframe adds to its cooling rate. */
        @JvmStatic
        fun coolant(state: BlockState): Double = when {
            state.`is`(Blocks.BLUE_ICE) -> 0.08
            state.`is`(Blocks.PACKED_ICE) -> 0.04
            state.`is`(Blocks.ICE) || state.`is`(Blocks.POWDER_SNOW) -> 0.02
            state.`is`(Blocks.SNOW_BLOCK) -> 0.015
            state.fluidState.`is`(net.minecraft.tags.FluidTags.WATER) -> 0.01
            else -> 0.0
        }
    }
}

/**
 * A mainframe's processors as a computer: what they can compute this tick, given their speed and the power stored.
 * [compute] spends the power, heats the mainframe and counts the FLOPS (at most the tick's capacity in total, however
 * many jobs ask). Pure, for unit tests; [MainframeBlockEntity] feeds it.
 */
class MainframeComputer(
    private val processors: () -> List<ProcessorTier>,
    private val heat: MainframeHeat,
    private val power: () -> Double,
    private val spendPower: (Double) -> Unit,
) : Distributable {
    /** FLOPS computed in the current tick. */
    var computed = 0.0
        private set

    /** Starts a new tick. */
    fun newTick() {
        computed = 0.0
    }

    /** FLOPS the processors manage this tick at the current temperature. */
    fun capacity(): Double = processors().sumOf { it.flopsPerTick } * heat.speed()

    /** Average power per FLOP over the processors, weighted by their FLOPS (0 without processors). */
    fun powerPerFlop(): Double {
        val list = processors()
        val flops = list.sumOf { it.flopsPerTick }
        return if (flops <= 0.0) 0.0 else list.sumOf { it.flopsPerTick * it.powerPerFlop } / flops
    }

    fun heatPerFlop(): Double {
        val list = processors()
        val flops = list.sumOf { it.flopsPerTick }
        return if (flops <= 0.0) 0.0 else list.sumOf { it.flopsPerTick * it.heatPerFlop } / flops
    }

    /** What is still available this tick. */
    fun available(): Double {
        val ppf = powerPerFlop()
        if (ppf <= 0.0) return 0.0
        return min(capacity() - computed, power() / ppf).coerceAtLeast(0.0)
    }

    override val max: Double get() = available()
    override val amount: Double get() = available()
    override val transferMax: Double get() = available()

    /** FLOPS per power, lower while throttled: the most efficient computers give first. */
    override val priority: Double get() = powerPerFlop().let { if (it <= 0.0) 0.0 else heat.speed() / it }

    override fun add(amount: Double): Double = 0.0

    override fun remove(amount: Double): Double {
        val flops = min(amount, available()).coerceAtLeast(0.0)
        if (flops <= 0.0) return 0.0
        spendPower(flops * powerPerFlop())
        heat.heat(flops * heatPerFlop())
        computed += flops
        return flops
    }
}

/**
 * The mainframe: four processor slots, a battery charged by wired or wireless power, and a computation leaf. It
 * computes for whatever jobs its computation network has, spending power only on FLOPS used, and heats up as it
 * does; keep it cool (ice, snow or water beside it, or a cold biome) to keep it at full speed.
 */
class MainframeBlockEntity(pos: BlockPos, state: BlockState) : FemtoBlockEntity(ComputationContent.MAINFRAME_BE.get(), pos, state) {
    @JvmField
    val processors: ItemStorageArray = object : ItemStorageArray(SLOTS, { markDirty() }) {
        override fun canInsert(index: Int, stack: IItemStack): Boolean = stack.isEmpty() || ProcessorItem.tierOf(stack.toMinecraft()) != null

        override fun maxStackSize(index: Int): Int = 1
    }

    @JvmField
    val battery = PowerBattery(BATTERY_SIZE) { markDirty() }

    @JvmField
    val heat = MainframeHeat()

    @JvmField
    val computer = MainframeComputer({ installed() }, heat, { battery.storage() }, { battery.drain(it) })

    /** FLOPS computed last tick, for the screen. */
    var lastFlops = 0.0
        private set

    @JvmField
    val leaf = FragWirelessPowerLeafNode({ battery }, PowerStorageNodeType.CONSUMER, rate = { LEAF_TRANSFER_RATE })

    init {
        fragList.addItemStorage(FragItemStorage(processors))
        fragList.addInternalFragment(FragDropInventory(processors))
        fragList.addFragment(FragPowerStorage({ battery }))
        fragList.addWirelessLeaf(leaf)
        fragList.addFragment(FragDerivedColor { parentColor(this, leaf) })
        fragList.addFragment(FragWiredPowerLeafNode({ battery }, PowerStorageNodeType.CONSUMER) { LEAF_TRANSFER_RATE })
        fragList.addFragment(FragComputationLeaf { participant() })
        fragList.addFragment(FragMenu(Component.translatable("block.femtocraft.mainframe"), { id, inv, _ -> MainframeMenu(id, inv, this) }))
        fragList.addInternalFragment(FragData("Mainframe", FragData.LEVEL, { _, o -> o.putDouble(TEMPERATURE_KEY, heat.temperature) }, { _, i ->
            heat.temperature = i.getDoubleOr(TEMPERATURE_KEY, MainframeHeat.DEFAULT_AMBIENT)
        }))
    }

    fun installed(): List<ProcessorTier> = (0 until processors.size()).mapNotNull { ProcessorItem.tierOf(processors.get(it).toMinecraft()) }

    /** The computer, while it has processors. */
    private fun participant(): ComputationParticipant? {
        if (installed().isEmpty()) return null
        return ComputationParticipant(DistributionRole.PRODUCER, computer)
    }

    fun ambient(): Double = level?.let { MainframeHeat.ambient(it.getBiome(blockPos).value().baseTemperature) } ?: MainframeHeat.DEFAULT_AMBIENT

    fun coolingRate(): Double {
        val level = level ?: return MainframeHeat.BASE_COOLING
        return MainframeHeat.BASE_COOLING + Direction.entries.sumOf { face ->
            val at = blockPos.relative(face)
            if (level.isLoaded(at)) MainframeHeat.coolant(level.getBlockState(at)) else 0.0
        }
    }

    override fun serverTick() {
        // Networks distribute at the end of the server tick, after block entities tick: what was computed since the
        // last tick belongs to it.
        lastFlops = computer.computed
        computer.newTick()
        val before = heat.temperature
        heat.cool(ambient(), coolingRate())
        if (lastFlops > 0.0 || kotlin.math.abs(before - heat.temperature) > 0.01) markDirty()
    }

    companion object {
        const val SLOTS = 4
        const val BATTERY_SIZE = 10000.0
        const val LEAF_TRANSFER_RATE = 100.0
        const val TEMPERATURE_KEY = "Temperature"
    }
}

class MainframeBlock(properties: BlockBehaviour.Properties) :
    FemtoHorizontalEntityBlock<MainframeBlockEntity>(properties, { ComputationContent.MAINFRAME_BE.get() })
