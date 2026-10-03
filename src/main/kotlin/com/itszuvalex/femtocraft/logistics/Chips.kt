package com.itszuvalex.femtocraft.logistics

import com.itszuvalex.femtocraft.FemtoRegistries
import com.itszuvalex.femtocraft.nanite.INaniteTank
import com.itszuvalex.femtocraft.nanite.NaniteModules
import com.itszuvalex.femtocraft.nanite.NaniteStack
import com.itszuvalex.itszulib.api.adapters.IBlockEntity
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.level.Level
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.transfer.ResourceHandlerUtil
import net.neoforged.neoforge.transfer.fluid.FluidResource
import net.neoforged.neoforge.transfer.item.ItemResource
import java.util.Locale
import java.util.Objects
import java.util.function.Consumer
import kotlin.math.min

/**
 * Direction of a logistics connection. Port of v3's `ConnectionDirection`.
 */
enum class ConnectionDirection { INPUT, OUTPUT, DISABLED }

/**
 * The settings every chip shares, whatever it moves. Port of v3's `ItemConnection` NBT keys (`flops`, `channel`,
 * `paused`, `condir`, `intdir`).
 */
data class ConnectionSettings(
    val flops: Double,
    val channel: String,
    val paused: Boolean,
    val direction: ConnectionDirection,
    val interfaceDirection: Direction,
) {
    /**
     * These settings with the connection direction ([mode]) or interface face cycled one step. Port of v3's
     * `MessageConduitInputOutputChange` and `MessageConduitFacingChange`.
     */
    fun cycled(mode: Boolean, forward: Boolean): ConnectionSettings {
        val step = if (forward) 1 else -1
        return if (mode) {
            val values = ConnectionDirection.entries
            copy(direction = values[Math.floorMod(direction.ordinal + step, values.size)])
        } else {
            copy(interfaceDirection = Direction.from3DDataValue(Math.floorMod(interfaceDirection.get3DDataValue() + step, 6)))
        }
    }

    companion object {
        @JvmField
        val MAP_CODEC: MapCodec<ConnectionSettings> = RecordCodecBuilder.mapCodec { i ->
            i.group(
                Codec.DOUBLE.fieldOf("flops").forGetter(ConnectionSettings::flops),
                Codec.STRING.optionalFieldOf("channel", "default").forGetter(ConnectionSettings::channel),
                Codec.BOOL.optionalFieldOf("paused", false).forGetter(ConnectionSettings::paused),
                Codec.STRING.xmap({ n -> ConnectionDirection.entries.firstOrNull { it.name == n } ?: ConnectionDirection.DISABLED }, ConnectionDirection::name)
                    .optionalFieldOf("condir", ConnectionDirection.DISABLED).forGetter(ConnectionSettings::direction),
                Direction.CODEC.optionalFieldOf("intdir", Direction.NORTH).forGetter(ConnectionSettings::interfaceDirection),
            ).apply(i, ::ConnectionSettings)
        }

        /**
         * A new chip's settings when first used in [face]: v3 made even-indexed faces (down, north, west) inputs and
         * the others outputs, facing back into the inventory.
         */
        fun defaults(face: Direction?, flops: Double): ConnectionSettings = ConnectionSettings(
            flops, "default", false,
            when {
                face == null -> ConnectionDirection.DISABLED
                face.get3DDataValue() % 2 == 0 -> ConnectionDirection.INPUT
                else -> ConnectionDirection.OUTPUT
            },
            face?.opposite ?: Direction.NORTH,
        )
    }
}

/**
 * A chip's state, kept on the chip as a data component: its [settings], the [buffer] of whatever it moves, and its
 * [filter]: up to [ChipKind.FILTER_SLOTS] allowed things (empty entries are unused; no entries at all allows
 * everything). Equality goes through [kind], since item and fluid stacks do not compare by value.
 */
class ChipData<B : Any>(val settings: ConnectionSettings, val buffer: B, val kind: ChipKind<B>, filter: List<B> = emptyList()) {
    /** Always [ChipKind.FILTER_SLOTS] entries. */
    val filter: List<B> = List(ChipKind.FILTER_SLOTS) { filter.getOrNull(it) ?: kind.empty }

    fun with(settings: ConnectionSettings = this.settings, buffer: B = this.buffer, filter: List<B> = this.filter) = ChipData(settings, buffer, kind, filter)

    /** Whether the filter lets [b] through. */
    fun allows(b: B): Boolean = kind.passes(filter, b)

    @Suppress("UNCHECKED_CAST")
    override fun equals(other: Any?): Boolean =
        other is ChipData<*> && other.kind === kind && settings == other.settings && kind.matches(buffer, other.buffer as B) &&
            filter.indices.all { kind.matches(filter[it], (other.filter as List<B>)[it]) }

    override fun hashCode(): Int = Objects.hash(settings, kind.hash(buffer), filter.map(kind::hash))

    override fun toString(): String = "ChipData(${kind.name}, $settings, ${kind.amount(buffer)})"
}

/**
 * What a kind of chip moves and how: its buffer type [B], how much it moves per operation ([perOp]) and holds
 * ([bufferLimit]), and how it reaches the block on its face. The connection logic itself is shared ([Connection]).
 */
abstract class ChipKind<B : Any>(
    val name: String,
    private val bufferCodec: Codec<B>,
    val empty: B,
    val perOp: Int,
    val bufferLimit: Int,
) {
    val flopsRequired: Double = 5000.0

    companion object {
        /** Filter entries per chip. */
        const val FILTER_SLOTS = 9
    }

    val codec: Codec<ChipData<B>> by lazy {
        RecordCodecBuilder.create { i ->
            i.group(
                ConnectionSettings.MAP_CODEC.forGetter(ChipData<B>::settings),
                bufferCodec.optionalFieldOf("buffer", empty).forGetter(ChipData<B>::buffer),
                bufferCodec.listOf().optionalFieldOf("filter", emptyList()).forGetter { d -> d.filter.takeIf { f -> f.any { !isEmpty(it) } } ?: emptyList() },
            ).apply(i) { s, b, f -> ChipData(s, b, this, f) }
        }
    }

    private val holder: DeferredHolder<DataComponentType<*>, DataComponentType<ChipData<B>>> =
        FemtoRegistries.DATA_COMPONENTS.registerComponentType("${name}_connection") { b ->
            b.persistent(codec).networkSynchronized(ByteBufCodecs.fromCodecWithRegistries(codec))
        }

    val component: DataComponentType<ChipData<B>> get() = holder.get()

    fun defaults(face: Direction?): ChipData<B> = ChipData(ConnectionSettings.defaults(face, flopsRequired), empty, this)

    fun data(chip: ItemStack, face: Direction?): ChipData<B> = chip.get(component) ?: defaults(face)

    /** Same contents and amount. */
    abstract fun matches(a: B, b: B): Boolean

    abstract fun hash(b: B): Int

    /** Same contents, ignoring the amount: can [a] and [b] share a buffer. */
    abstract fun sameType(a: B, b: B): Boolean

    abstract fun amount(b: B): Int

    abstract fun withAmount(b: B, amount: Int): B

    fun isEmpty(b: B): Boolean = amount(b) <= 0

    /** Whether [filter] lets [b] through: no entries allow everything, otherwise [b] must be the same as one. */
    fun passes(filter: List<B>, b: B): Boolean = filter.all { isEmpty(it) } || filter.any { !isEmpty(it) && sameType(it, b) }

    /** Whether filter entries can be set from a held item ([template]). */
    open val filterable: Boolean get() = false

    /**
     * The filter entry a click with [held] sets: [empty] for an empty hand (clears the entry), what [held] is or holds
     * (one of it), or null if [held] means nothing to this kind.
     */
    open fun template(held: ItemStack): B? = if (held.isEmpty) empty else null

    /** How much the buffer may hold of [b]. */
    open fun limit(b: B): Int = bufferLimit

    abstract fun describe(b: B): Component

    /** What [b] is, without an amount (filter entries). */
    open fun typeName(b: B): Component = describe(b)

    /** Orders output buffers in the network. */
    abstract fun sortKey(b: B): String

    /** Whether the block at [pos], accessed from [side], would take some of [offer]. */
    abstract fun canAccept(level: Level, pos: BlockPos, side: Direction, offer: B): Boolean

    /**
     * Pulls up to [max] matching [buffer] (anything [filter] allows, if empty) from the block at [pos].
     *
     * @return The new buffer.
     */
    abstract fun pull(level: Level, pos: BlockPos, side: Direction, buffer: B, max: Int, filter: List<B>): B

    /** Pushes up to [max] of [buffer] into the block at [pos]. @return How much went in. */
    abstract fun push(level: Level, pos: BlockPos, side: Direction, buffer: B, max: Int): Int
}

/**
 * Item chips: 1 item per operation, a 16-item buffer (v3's `ItemConnection`), through `Capabilities.Item.BLOCK`.
 */
object ItemChipKind : ChipKind<ItemStack>("item", ItemStack.OPTIONAL_CODEC, ItemStack.EMPTY, 1, 16) {
    override fun matches(a: ItemStack, b: ItemStack) = ItemStack.matches(a, b)
    override fun hash(b: ItemStack) = Objects.hash(ItemStack.hashItemAndComponents(b), b.count)
    override fun sameType(a: ItemStack, b: ItemStack) = ItemStack.isSameItemSameComponents(a, b)
    override fun amount(b: ItemStack) = b.count
    override fun withAmount(b: ItemStack, amount: Int): ItemStack = if (amount <= 0) ItemStack.EMPTY else b.copyWithCount(amount)
    override fun limit(b: ItemStack) = if (b.isEmpty) bufferLimit else min(bufferLimit, b.maxStackSize)
    override fun describe(b: ItemStack): Component = b.hoverName
    override fun sortKey(b: ItemStack) = BuiltInRegistries.ITEM.getKey(b.item).toString()

    private fun handler(level: Level, pos: BlockPos, side: Direction) = level.getCapability(Capabilities.Item.BLOCK, pos, side)

    override fun canAccept(level: Level, pos: BlockPos, side: Direction, offer: ItemStack): Boolean {
        val handler = handler(level, pos, side) ?: return false
        val resource = ItemResource.of(offer)
        return (0 until handler.size()).any { handler.isValid(it, resource) }
    }

    override val filterable: Boolean get() = true

    override fun template(held: ItemStack): ItemStack? = if (held.isEmpty) empty else held.copyWithCount(1)

    /**
     * From an indexed inventory ([LogisticsModules.ITEM_INDEX] on that face, e.g. an item vault) straight through its
     * index: only the slots holding what is wanted are read. Otherwise the first matching item through the block's item
     * handler.
     */
    override fun pull(level: Level, pos: BlockPos, side: Direction, buffer: ItemStack, max: Int, filter: List<ItemStack>): ItemStack {
        index(level, pos, side)?.let { return pullIndexed(it, buffer, max, filter) }
        val handler = handler(level, pos, side) ?: return buffer
        val got = ResourceHandlerUtil.extractFirst(handler, { r -> if (buffer.isEmpty) passes(filter, r.toStack(1)) else r.matches(buffer) }, max, null)
        return if (got == null || got.amount() <= 0) buffer else got.resource().toStack(buffer.count + got.amount())
    }

    private fun index(level: Level, pos: BlockPos, side: Direction): com.itszuvalex.itszulib.api.storage.ItemStorageIndex? {
        if (!level.isLoaded(pos)) return null
        return level.getBlockEntity(pos)?.let(IBlockEntity::of)?.getModule(LogisticsModules.ITEM_INDEX, side)
    }

    /** Takes from [index]: more of the buffer's kind, or the first item held that [filter] allows. */
    fun pullIndexed(index: com.itszuvalex.itszulib.api.storage.ItemStorageIndex, buffer: ItemStack, max: Int, filter: List<ItemStack>): ItemStack {
        val wanted = when {
            !buffer.isEmpty -> listOf(buffer)
            filter.any { !it.isEmpty } -> filter.filter { !it.isEmpty }
            else -> null
        }
        val ids = wanted?.map { BuiltInRegistries.ITEM.getKey(it.item) }?.distinct() ?: index.items().toList()
        for (id in ids) {
            val taken = index.extract(id, max) { stack ->
                val mc = stack.toMinecraft()
                if (!buffer.isEmpty) ItemStack.isSameItemSameComponents(mc, buffer) else passes(filter, mc)
            }
            if (!taken.isEmpty()) return taken.toMinecraft().let { it.copyWithCount(buffer.count + it.count) }
        }
        return buffer
    }

    override fun push(level: Level, pos: BlockPos, side: Direction, buffer: ItemStack, max: Int): Int {
        val handler = handler(level, pos, side) ?: return 0
        return ResourceHandlerUtil.insertStacking(handler, ItemResource.of(buffer), min(max, buffer.count), null)
    }
}

/**
 * Fluid chips: 250 mB per operation and a 1000 mB buffer, through `Capabilities.Fluid.BLOCK`. v3 had the item but no
 * connection; the amounts are new.
 */
object FluidChipKind : ChipKind<FluidStack>("fluid", FluidStack.OPTIONAL_CODEC, FluidStack.EMPTY, 250, 1000) {
    override fun matches(a: FluidStack, b: FluidStack) = FluidStack.matches(a, b)
    override fun hash(b: FluidStack) = Objects.hash(FluidStack.hashFluidAndComponents(b), b.amount)
    override fun sameType(a: FluidStack, b: FluidStack) = FluidStack.isSameFluidSameComponents(a, b)
    override fun amount(b: FluidStack) = b.amount
    override fun withAmount(b: FluidStack, amount: Int): FluidStack = if (amount <= 0) FluidStack.EMPTY else b.copyWithAmount(amount)
    override fun describe(b: FluidStack): Component = Component.literal("${b.amount} mB ").append(b.hoverName)
    override fun typeName(b: FluidStack): Component = b.hoverName
    override fun sortKey(b: FluidStack) = BuiltInRegistries.FLUID.getKey(b.fluid).toString()

    private fun handler(level: Level, pos: BlockPos, side: Direction) = level.getCapability(Capabilities.Fluid.BLOCK, pos, side)

    override fun canAccept(level: Level, pos: BlockPos, side: Direction, offer: FluidStack): Boolean {
        val handler = handler(level, pos, side) ?: return false
        val resource = FluidResource.of(offer)
        return (0 until handler.size()).any { handler.isValid(it, resource) }
    }

    override val filterable: Boolean get() = true

    /** The fluid in a held container (a bucket, a tank item). */
    override fun template(held: ItemStack): FluidStack? = if (held.isEmpty) empty else
        net.neoforged.neoforge.fluids.FluidUtil.getFluidContained(held).orElse(null)?.takeUnless { it.isEmpty }?.copyWithAmount(1)

    override fun pull(level: Level, pos: BlockPos, side: Direction, buffer: FluidStack, max: Int, filter: List<FluidStack>): FluidStack {
        val handler = handler(level, pos, side) ?: return buffer
        val got = ResourceHandlerUtil.extractFirst(handler, { r -> if (buffer.isEmpty) passes(filter, r.toStack(1)) else r.matches(buffer) }, max, null)
        return if (got == null || got.amount() <= 0) buffer else got.resource().toStack(buffer.amount + got.amount())
    }

    override fun push(level: Level, pos: BlockPos, side: Direction, buffer: FluidStack, max: Int): Int {
        val handler = handler(level, pos, side) ?: return 0
        return ResourceHandlerUtil.insertStacking(handler, FluidResource.of(buffer), min(max, buffer.amount), null)
    }
}

/**
 * Nanite chips: 5 nanites per operation and a 25-nanite buffer of one strain and version, through the nanite tank
 * module ([NaniteModules.NANITE_TANK]). v3 had the item but no connection; the amounts are new.
 */
object NaniteChipKind : ChipKind<NaniteStack>("nanite", NaniteStack.CODEC, NaniteStack.EMPTY, 5, 25) {
    override fun matches(a: NaniteStack, b: NaniteStack) = (a.isEmpty && b.isEmpty) || a == b
    override fun hash(b: NaniteStack) = if (b.isEmpty) 0 else b.hashCode()
    override fun sameType(a: NaniteStack, b: NaniteStack) = a.isSameNanite(b)
    override fun amount(b: NaniteStack) = b.amount
    override fun withAmount(b: NaniteStack, amount: Int) = b.withAmount(amount)
    override fun describe(b: NaniteStack): Component = Component.literal("${b.amount} ${b.archetype}/${b.strain} v${b.version.major}.${b.version.minor}")
    override fun sortKey(b: NaniteStack) = "${b.archetype}/${b.strain}/${b.version.major}.${b.version.minor}"

    private fun tank(level: Level, pos: BlockPos, side: Direction): INaniteTank? {
        if (!level.isLoaded(pos)) return null
        return level.getBlockEntity(pos)?.let(IBlockEntity::of)?.getModule(NaniteModules.NANITE_TANK, side)
    }

    override fun canAccept(level: Level, pos: BlockPos, side: Direction, offer: NaniteStack): Boolean {
        val tank = tank(level, pos, side) ?: return false
        return tank.fill(offer.withAmount(1), false).isEmpty
    }

    override fun pull(level: Level, pos: BlockPos, side: Direction, buffer: NaniteStack, max: Int, filter: List<NaniteStack>): NaniteStack {
        val tank = tank(level, pos, side) ?: return buffer
        val source = if (buffer.isEmpty) tank.contents().firstOrNull { passes(filter, it) } ?: return buffer else buffer
        val got = tank.drain(source.withAmount(max), true)
        return if (got.isEmpty) buffer else got.withAmount(buffer.amount + got.amount)
    }

    override fun push(level: Level, pos: BlockPos, side: Direction, buffer: NaniteStack, max: Int): Int {
        val tank = tank(level, pos, side) ?: return 0
        val offer = buffer.withAmount(min(max, buffer.amount))
        return offer.amount - tank.fill(offer, true).amount
    }
}

/**
 * Where a [Connection] keeps its flop countdown. A conduit keeps it in memory and writes it back to the chip only when
 * the chip is read from outside (taken out, dropped, saved, shown), so ticking does not rewrite the chip.
 */
interface FlopCounter {
    var flops: Double

    /** Called after an operation reset the countdown. */
    fun onOperation() {}
}

/**
 * One chip in a conduit face: every time its flop counter runs down it pulls (INPUT) [ChipKind.perOp] from the block on
 * that face into its buffer, or pushes (OUTPUT) that much from the buffer into the block. Flops recharge passively at
 * a 200th of [ChipKind.flopsRequired] per tick. Port of v3's `ItemConnection` (5000 flops, 1 item per operation,
 * 16-item buffer), generalised to the fluid and nanite chips v3 never implemented.
 */
class Connection<B : Any>(
    val kind: ChipKind<B>,
    private val chip: ItemStack,
    private val level: Level,
    private val conduit: BlockPos,
    private val face: Direction,
    private val counter: FlopCounter,
    private val onChanged: Runnable,
) {
    val data: ChipData<B> get() = kind.data(chip, face)

    private fun update(change: (ChipData<B>) -> ChipData<B>) {
        Chips.write(kind, chip, change(data))
        onChanged.run()
    }

    val settings: ConnectionSettings get() = data.settings

    val buffer: B get() = data.buffer

    val direction: ConnectionDirection get() = if (settings.paused) ConnectionDirection.DISABLED else settings.direction

    val channel: String get() = settings.channel

    val active: Boolean get() = !settings.paused && if (direction == ConnectionDirection.INPUT) canAcceptMoreInput() else !kind.isEmpty(buffer)

    val passiveFlopGen: Double get() = kind.flopsRequired / (20 * 10)

    /** Flops left before the next operation. */
    val flopsRemaining: Double get() = counter.flops

    private val target: BlockPos get() = conduit.relative(face)

    private fun canAcceptMoreInput(): Boolean = kind.amount(buffer) < kind.limit(buffer)

    /**
     * Spends [flops] on the countdown; runs an operation when it reaches 0.
     *
     * @return Unused flops.
     */
    fun contributeFlops(flops: Double): Double {
        val used = min(counter.flops, flops)
        counter.flops -= used
        if (counter.flops <= 0) {
            when (direction) {
                ConnectionDirection.INPUT -> input()
                ConnectionDirection.OUTPUT -> output()
                ConnectionDirection.DISABLED -> {}
            }
            counter.flops = kind.flopsRequired
            counter.onOperation()
        }
        return flops - used
    }

    /**
     * Whether the block on this face would take [offer].
     */
    fun canInsert(offer: B): Boolean =
        !settings.paused && !kind.isEmpty(offer) && data.allows(offer) && kind.canAccept(level, target, settings.interfaceDirection, offer)

    /**
     * Adds [offer] to the buffer.
     *
     * @return What did not fit.
     */
    fun insert(offer: B): B {
        if (kind.isEmpty(offer)) return offer
        val current = buffer
        if (!kind.isEmpty(current) && !kind.sameType(current, offer)) return offer
        val room = kind.limit(offer) - kind.amount(current)
        if (room <= 0) return offer
        val moved = min(room, kind.amount(offer))
        update { it.with(buffer = kind.withAmount(offer, kind.amount(current) + moved)) }
        return kind.withAmount(offer, kind.amount(offer) - moved)
    }

    fun setBuffer(buffer: B) = update { it.with(buffer = buffer) }

    private fun input() {
        if (!canAcceptMoreInput()) return
        val current = buffer
        val want = min(kind.perOp, kind.limit(current) - kind.amount(current))
        val next = kind.pull(level, target, settings.interfaceDirection, current, want, data.filter)
        if (!kind.matches(next, current)) setBuffer(next)
    }

    private fun output() {
        val current = buffer
        if (kind.isEmpty(current)) return
        val pushed = kind.push(level, target, settings.interfaceDirection, current, kind.perOp)
        if (pushed > 0) setBuffer(kind.withAmount(current, kind.amount(current) - pushed))
    }
}

/**
 * The chip kinds and helpers for chip stacks.
 */
object Chips {
    @JvmField val KINDS: List<ChipKind<*>> = listOf(ItemChipKind, FluidChipKind, NaniteChipKind)

    fun isChip(stack: ItemStack): Boolean = stack.item is ChipItem

    fun kindOf(stack: ItemStack): ChipKind<*>? = (stack.item as? ChipItem)?.kind

    /** The settings of chip [stack] (its defaults if it has never been used), or null for a non-chip. */
    fun settingsOf(stack: ItemStack, face: Direction? = null): ConnectionSettings? = kindOf(stack)?.data(stack, face)?.settings

    /**
     * Stores [data] on [chip]. A blank chip stacks to 64; one that holds data (settings, a buffer) stacks to 1, so two
     * used chips never merge and lose one buffer.
     */
    fun <B : Any> write(kind: ChipKind<B>, chip: ItemStack, data: ChipData<B>) {
        chip.set(kind.component, data)
        chip.set(DataComponents.MAX_STACK_SIZE, 1)
    }

    /**
     * A connection for [chip] in conduit face [face].
     */
    fun connection(chip: ItemStack, level: Level, conduit: BlockPos, face: Direction, counter: FlopCounter, onChanged: Runnable): Connection<*>? =
        kindOf(chip)?.let { Connection(it, chip, level, conduit, face, counter, onChanged) }

    /**
     * Writes [flops] into chip [chip] (in conduit face [face]) if it differs from what the chip holds.
     */
    fun writeFlops(chip: ItemStack, face: Direction, flops: Double) {
        val kind = kindOf(chip) ?: return
        writeFlops(kind, chip, face, flops)
    }

    private fun <B : Any> writeFlops(kind: ChipKind<B>, chip: ItemStack, face: Direction, flops: Double) {
        val d = kind.data(chip, face)
        if (d.settings.flops != flops) write(kind, chip, d.with(settings = d.settings.copy(flops = flops)))
    }

    /**
     * A copy of [chip] (in conduit face [face]) with its connection direction ([mode]) or interface face cycled one
     * step.
     */
    fun cycled(chip: ItemStack, face: Direction, mode: Boolean, forward: Boolean): ItemStack {
        val kind = kindOf(chip) ?: return chip
        return chip.copy().also { cycle(kind, it, face, mode, forward) }
    }

    /**
     * A copy of [chip] (in conduit face [face]) with filter entry [slot] set from [held] ([ChipKind.template]), or null if
     * [held] cannot be a filter entry for this chip.
     */
    fun withFilter(chip: ItemStack, face: Direction, slot: Int, held: ItemStack): ItemStack? {
        val kind = kindOf(chip) ?: return null
        return setFilter(kind, chip.copy(), face, slot, held)
    }

    private fun <B : Any> setFilter(kind: ChipKind<B>, chip: ItemStack, face: Direction, slot: Int, held: ItemStack): ItemStack? {
        if (slot !in 0 until ChipKind.FILTER_SLOTS || !kind.filterable) return null
        val entry = kind.template(held) ?: return null
        val d = kind.data(chip, face)
        write(kind, chip, d.with(filter = d.filter.toMutableList().also { it[slot] = entry }))
        return chip
    }

    private fun <B : Any> cycle(kind: ChipKind<B>, chip: ItemStack, face: Direction, mode: Boolean, forward: Boolean) {
        val d = kind.data(chip, face)
        write(kind, chip, d.with(settings = d.settings.cycled(mode, forward)))
    }
}

/**
 * A logistics chip of one [kind]. Port of v3's `ItemLogisticsItemChip`: the damage bar shows the flop countdown and the
 * tooltip the connection state.
 */
class ChipItem(val kind: ChipKind<*>, properties: Properties) : Item(properties) {
    override fun isBarVisible(stack: ItemStack): Boolean = stack.has(kind.component)

    override fun getBarWidth(stack: ItemStack): Int {
        val d = stack.get(kind.component) ?: return 0
        return (13.0 * (1 - d.settings.flops / kind.flopsRequired)).toInt().coerceIn(0, 13)
    }

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, display: TooltipDisplay, builder: Consumer<Component>, flag: TooltipFlag) =
        tooltip(kind, stack, builder)

    private fun <B : Any> tooltip(kind: ChipKind<B>, stack: ItemStack, builder: Consumer<Component>) {
        val d = kind.data(stack, null)
        val s = d.settings
        fun line(key: String, vararg args: Any) = builder.accept(Component.translatable("tooltip.femtocraft.chip.$key", *args).withStyle(ChatFormatting.GRAY))
        line("item", if (kind.isEmpty(d.buffer)) Component.translatable("tooltip.femtocraft.none") else kind.describe(d.buffer))
        line("flops", "%,.1f".format(Locale.ROOT, s.flops), "%,.1f".format(Locale.ROOT, kind.flopsRequired))
        line("channel", s.channel)
        line("mode", s.direction.name)
        line("interface", s.interfaceDirection.serializedName)
        val allowed = d.filter.filter { !kind.isEmpty(it) }
        if (allowed.isNotEmpty()) {
            line("filter", allowed.size)
            for (entry in allowed) builder.accept(Component.literal("  ").append(kind.typeName(entry)).withStyle(ChatFormatting.DARK_GRAY))
        }
    }
}
