package com.itszuvalex.femtocraft.logistics

import com.itszuvalex.femtocraft.FemtoRegistries
import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.core.FemtoBlockEntity
import com.itszuvalex.femtocraft.core.FemtoEntityBlock
import com.itszuvalex.femtocraft.nanite.NaniteModules
import com.itszuvalex.femtocraft.power.WirelessPowerNetwork
import com.itszuvalex.itszulib.api.Modules
import com.itszuvalex.itszulib.api.adapters.IBlockEntity
import com.itszuvalex.itszulib.api.adapters.IItemStack
import com.itszuvalex.itszulib.api.adapters.ILevel
import com.itszuvalex.itszulib.api.adapters.IModule
import com.itszuvalex.itszulib.api.adapters.Module
import com.itszuvalex.itszulib.api.storage.ItemStorageArray
import com.itszuvalex.itszulib.api.utility.NBTSerializationScope
import com.itszuvalex.itszulib.core.TileNetwork
import com.itszuvalex.itszulib.core.frag.FragDropInventory
import com.itszuvalex.itszulib.core.frag.FragMenu
import com.itszuvalex.itszulib.core.frag.FragNetworkedWire
import com.itszuvalex.itszulib.util.FaceBitSet
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape
import net.neoforged.fml.LogicalSide
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.transfer.ResourceHandlerUtil
import net.neoforged.neoforge.transfer.item.ItemResource
import java.util.Locale
import java.util.function.Consumer
import kotlin.math.min

/**
 * Direction of a logistics connection. Port of v3's `ConnectionDirection`.
 */
enum class ConnectionDirection { INPUT, OUTPUT, DISABLED }

/**
 * State of an item chip's connection, kept on the chip as the `femtocraft:item_connection` component. Port of v3's
 * `ItemConnection` NBT keys (`flops`, `buffer`, `channel`, `paused`, `condir`, `intdir`).
 */
data class ItemConnectionData(
    val flops: Double,
    val buffer: ItemStack,
    val channel: String,
    val paused: Boolean,
    val direction: ConnectionDirection,
    val interfaceDirection: Direction,
) {
    override fun equals(other: Any?): Boolean = other is ItemConnectionData && flops == other.flops && ItemStack.matches(buffer, other.buffer) &&
        channel == other.channel && paused == other.paused && direction == other.direction && interfaceDirection == other.interfaceDirection

    override fun hashCode(): Int = java.util.Objects.hash(flops, ItemStack.hashItemAndComponents(buffer), buffer.count, channel, paused, direction, interfaceDirection)

    companion object {
        @JvmField
        val CODEC: Codec<ItemConnectionData> = RecordCodecBuilder.create { i ->
            i.group(
                Codec.DOUBLE.fieldOf("flops").forGetter(ItemConnectionData::flops),
                ItemStack.OPTIONAL_CODEC.optionalFieldOf("buffer", ItemStack.EMPTY).forGetter(ItemConnectionData::buffer),
                Codec.STRING.optionalFieldOf("channel", "default").forGetter(ItemConnectionData::channel),
                Codec.BOOL.optionalFieldOf("paused", false).forGetter(ItemConnectionData::paused),
                Codec.STRING.xmap({ n -> ConnectionDirection.entries.firstOrNull { it.name == n } ?: ConnectionDirection.DISABLED }, ConnectionDirection::name)
                    .optionalFieldOf("condir", ConnectionDirection.DISABLED).forGetter(ItemConnectionData::direction),
                Direction.CODEC.optionalFieldOf("intdir", Direction.NORTH).forGetter(ItemConnectionData::interfaceDirection),
            ).apply(i, ::ItemConnectionData)
        }
    }
}

/**
 * One item chip in a conduit face: buffers up to [stackLimit] items, and every time its flop counter runs down it
 * pulls (INPUT) [itemsPerOp] items from the inventory on that face into the buffer, or pushes (OUTPUT) them from the
 * buffer into the inventory. Flops recharge passively at a 200th of [flopsRequired] per tick. Port of v3's
 * `ItemConnection` (5000 flops, 1 item per operation, 16-item buffer).
 */
class ItemConnection(
    private val chip: ItemStack,
    private val level: Level,
    private val conduit: BlockPos,
    private val face: Direction,
    private val onChanged: Runnable,
) {
    val data: ItemConnectionData get() = chip.get(ItemChips.CONNECTION.get()) ?: ItemChips.defaults(face)

    private fun update(change: (ItemConnectionData) -> ItemConnectionData) {
        chip.set(ItemChips.CONNECTION.get(), change(data))
        onChanged.run()
    }

    val buffer: ItemStack get() = data.buffer

    val direction: ConnectionDirection get() = if (data.paused) ConnectionDirection.DISABLED else data.direction

    val channel: String get() = data.channel

    val active: Boolean get() = !data.paused && if (direction == ConnectionDirection.INPUT) canAcceptMoreInput() else !buffer.isEmpty

    val passiveFlopGen: Double get() = FLOPS_REQUIRED / (20 * 10)

    private fun handler() = level.getCapability(Capabilities.Item.BLOCK, conduit.relative(face), data.interfaceDirection)

    private fun canAcceptMoreInput(): Boolean = buffer.isEmpty || (buffer.count < STACK_LIMIT && buffer.count < buffer.maxStackSize)

    /**
     * Spends [flops] on the countdown; runs an operation when it reaches 0.
     *
     * @return Unused flops.
     */
    fun contributeFlops(flops: Double): Double {
        val used = min(data.flops, flops)
        update { it.copy(flops = it.flops - used) }
        if (data.flops <= 0) {
            when (direction) {
                ConnectionDirection.INPUT -> inputItem()
                ConnectionDirection.OUTPUT -> outputItem()
                ConnectionDirection.DISABLED -> {}
            }
        }
        return flops - used
    }

    /**
     * Whether the inventory on this face would take [stack].
     */
    fun canInsert(stack: ItemStack): Boolean {
        if (data.paused || stack.isEmpty) return false
        val handler = handler() ?: return false
        val resource = ItemResource.of(stack)
        return (0 until handler.size()).any { handler.isValid(it, resource) }
    }

    /**
     * Adds [stack] to the buffer.
     *
     * @return What did not fit.
     */
    fun insert(stack: ItemStack): ItemStack {
        if (stack.isEmpty) return stack
        val current = buffer
        if (!current.isEmpty && !ItemStack.isSameItemSameComponents(current, stack)) return stack
        val limit = min(STACK_LIMIT, stack.maxStackSize)
        val room = limit - current.count
        if (room <= 0) return stack
        val moved = min(room, stack.count)
        update { it.copy(buffer = stack.copyWithCount(current.count + moved)) }
        return stack.copyWithCount(stack.count - moved)
    }

    fun setBuffer(stack: ItemStack) = update { it.copy(buffer = stack.copy()) }

    private fun inputItem() {
        if (canAcceptMoreInput()) {
            val handler = handler()
            if (handler != null) {
                val current = buffer
                val want = min(ITEMS_PER_OP, STACK_LIMIT - current.count)
                val got = ResourceHandlerUtil.extractFirst(handler, { r -> current.isEmpty || r.matches(current) }, want, null)
                if (got != null && got.amount() > 0) update { it.copy(buffer = got.resource().toStack(current.count + got.amount())) }
            }
        }
        update { it.copy(flops = FLOPS_REQUIRED) }
    }

    private fun outputItem() {
        val current = buffer
        if (!current.isEmpty) {
            val handler = handler()
            if (handler != null) {
                val inserted = ResourceHandlerUtil.insertStacking(handler, ItemResource.of(current), min(ITEMS_PER_OP, current.count), null)
                if (inserted > 0) update { it.copy(buffer = current.copyWithCount(current.count - inserted)) }
            }
        }
        update { it.copy(flops = FLOPS_REQUIRED) }
    }

    companion object {
        const val FLOPS_REQUIRED = 5000.0
        const val ITEMS_PER_OP = 1
        const val STACK_LIMIT = 16
    }
}

/**
 * The basic item chip and its connection component. Port of v3's `ItemLogisticsItemChip`: the damage bar shows the flop
 * countdown and the tooltip the connection state.
 */
object ItemChips {
    @JvmField
    val CONNECTION: DeferredHolder<DataComponentType<*>, DataComponentType<ItemConnectionData>> =
        FemtoRegistries.DATA_COMPONENTS.registerComponentType("item_connection") { b ->
            b.persistent(ItemConnectionData.CODEC).networkSynchronized(net.minecraft.network.codec.ByteBufCodecs.fromCodecWithRegistries(ItemConnectionData.CODEC))
        }

    /**
     * A new chip's connection when first used in [face]: v3 made even-indexed faces (down, north, west) inputs and the
     * others outputs, facing back into the inventory.
     */
    fun defaults(face: Direction?): ItemConnectionData = ItemConnectionData(
        ItemConnection.FLOPS_REQUIRED, ItemStack.EMPTY, "default", false,
        when {
            face == null -> ConnectionDirection.DISABLED
            face.get3DDataValue() % 2 == 0 -> ConnectionDirection.INPUT
            else -> ConnectionDirection.OUTPUT
        },
        face?.opposite ?: Direction.NORTH,
    )

    fun isChip(stack: ItemStack): Boolean = stack.item is ItemChipItem

    /**
     * A copy of [chip] (in conduit face [face]) with its connection direction ([mode]) or interface face cycled one
     * step. Port of v3's `MessageConduitInputOutputChange` and `MessageConduitFacingChange`.
     */
    fun cycled(chip: ItemStack, face: Direction, mode: Boolean, forward: Boolean): ItemStack {
        val d = chip.get(CONNECTION.get()) ?: defaults(face)
        val step = if (forward) 1 else -1
        val next = if (mode) {
            val values = ConnectionDirection.entries
            d.copy(direction = values[Math.floorMod(d.direction.ordinal + step, values.size)])
        } else {
            d.copy(interfaceDirection = Direction.from3DDataValue(Math.floorMod(d.interfaceDirection.get3DDataValue() + step, 6)))
        }
        return chip.copy().also { it.set(CONNECTION.get(), next) }
    }
}

class ItemChipItem(properties: Properties) : Item(properties) {
    override fun isBarVisible(stack: ItemStack): Boolean = stack.has(ItemChips.CONNECTION.get())

    override fun getBarWidth(stack: ItemStack): Int {
        val d = stack.get(ItemChips.CONNECTION.get()) ?: return 0
        return (13.0 * (1 - d.flops / ItemConnection.FLOPS_REQUIRED)).toInt().coerceIn(0, 13)
    }

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, display: TooltipDisplay, builder: Consumer<Component>, flag: TooltipFlag) {
        val d = stack.get(ItemChips.CONNECTION.get()) ?: ItemChips.defaults(null)
        fun line(key: String, vararg args: Any) = builder.accept(Component.translatable("tooltip.femtocraft.chip.$key", *args).withStyle(ChatFormatting.GRAY))
        line("item", if (d.buffer.isEmpty) Component.translatable("tooltip.femtocraft.none") else d.buffer.hoverName)
        line("flops", "%,.1f".format(Locale.ROOT, d.flops), "%,.1f".format(Locale.ROOT, ItemConnection.FLOPS_REQUIRED))
        line("channel", d.channel)
        line("mode", d.direction.name)
        line("interface", d.interfaceDirection.serializedName)
    }
}

/**
 * Network of logistics conduits. Each tick every chip earns its passive flops, then input chips' buffers are offered
 * to output chips (non-empty buffers first, then by item id) that can pass them on. Port of v3's `LogisticsNetwork`
 * (item connections only; v3 never implemented fluid or nanite connections).
 */
class LogisticsNetwork(id: Int) : TileNetwork<LogisticsConduit, LogisticsNetwork>(id, LogicalSide.SERVER) {
    override fun networkModule(): IModule<LogisticsConduit> = LogisticsConduit.MODULE

    override fun create(): LogisticsNetwork = LogisticsNetwork(WirelessPowerNetwork.nextId())

    fun connections(): List<ItemConnection> = getNodes().flatMap { it.connections() }.toList()

    override fun onTickEnd() {
        val connections = connections()
        connections.filter { it.active }.forEach { it.contributeFlops(it.passiveFlopGen) }
        val outputs = connections.filter { it.direction == ConnectionDirection.OUTPUT }
            .sortedWith(compareBy<ItemConnection> { it.buffer.isEmpty }.thenBy { BuiltInRegistries.ITEM.getKey(it.buffer.item).toString() })
        for (input in connections.filter { it.direction == ConnectionDirection.INPUT && !it.buffer.isEmpty }) {
            for (output in outputs) {
                if (input.buffer.isEmpty) break
                if (output.channel != input.channel || !output.canInsert(input.buffer)) continue
                input.setBuffer(output.insert(input.buffer))
            }
        }
    }
}

/**
 * A logistics conduit: joins neighbouring conduits into a [LogisticsNetwork] and holds up to four chips per face
 * (stack size 1). Faces towards an inventory, tank, nanite tank or another conduit count as connected (synced, key
 * `Inv`, for the model). Port of v3's `ModuleConduit`/`TileConduit`.
 */
class LogisticsConduit : FragNetworkedWire<LogisticsConduit, LogisticsNetwork>({ LogisticsNetwork(WirelessPowerNetwork.nextId()) }) {
    @JvmField
    val inventoryFaces = FaceBitSet()

    private var lvl: Level? = null

    @JvmField
    val chips: Array<ItemStorageArray> = Array(6) {
        object : ItemStorageArray(CHIPS_PER_FACE, { markDirty() }) {
            override fun canInsert(index: Int, stack: IItemStack): Boolean = stack.isEmpty() || ItemChips.isChip(stack.toMinecraft())
            override fun maxStackSize(index: Int): Int = 1
        }
    }

    override fun module(): IModule<LogisticsConduit> = MODULE

    fun connections(): List<ItemConnection> {
        val level = lvl ?: return listOf()
        val pos = host?.blockEntity()?.getBlockPos() ?: return listOf()
        return Direction.entries.flatMap { face ->
            val storage = chips[face.get3DDataValue()]
            (0 until storage.size()).map { storage.get(it).toMinecraft() }.filter { ItemChips.isChip(it) }.map { ItemConnection(it, level, pos, face) { markDirty() } }
        }
    }

    private fun refreshInventories() {
        val level = lvl ?: return
        if (level.isClientSide) return
        val pos = host?.blockEntity()?.getBlockPos() ?: return
        var changed = false
        for (face in Direction.entries) {
            val at = pos.relative(face)
            if (!level.isLoaded(at)) continue
            val be = level.getBlockEntity(at)
            val inventory = be != null && (level.getCapability(Capabilities.Item.BLOCK, at, face.opposite) != null ||
                level.getCapability(Capabilities.Fluid.BLOCK, at, face.opposite) != null ||
                (be as? IBlockEntity)?.getModule(NaniteModules.NANITE_TANK, face.opposite) != null)
            val connect = inventory && !isBlocked(face)
            if (inventoryFaces[face] != connect) {
                inventoryFaces[face] = connect
                changed = true
            }
        }
        if (changed) markDirtyAndSync()
    }

    override fun onLoad(level: ILevel, pos: BlockPos) {
        lvl = level.toMinecraft()
        super.onLoad(level, pos)
        refreshInventories()
    }

    override fun onNeighborChanged(level: ILevel, pos: BlockPos) {
        lvl = level.toMinecraft()
        super.onNeighborChanged(level, pos)
        refreshInventories()
    }

    override fun serializeTo(scope: NBTSerializationScope, output: ValueOutput) {
        super.serializeTo(scope, output)
        output.putInt(INV_KEY, inventoryFaces.bits)
        if (scope == NBTSerializationScope.LEVEL) {
            val storage = output.child(STORAGE_KEY)
            Direction.entries.forEach { chips[it.get3DDataValue()].serialize(storage.child(it.serializedName)) }
        }
    }

    override fun deserialize(input: ValueInput, scope: NBTSerializationScope) {
        super.deserialize(input, scope)
        inventoryFaces.load(input.getIntOr(INV_KEY, 0))
        if (scope == NBTSerializationScope.LEVEL) {
            input.child(STORAGE_KEY).ifPresent { storage -> Direction.entries.forEach { d -> storage.child(d.serializedName).ifPresent(chips[d.get3DDataValue()]::deserialize) } }
        }
    }

    companion object {
        const val CHIPS_PER_FACE = 4
        const val INV_KEY = "Inv"
        const val STORAGE_KEY = "storage"

        @JvmField
        val MODULE: IModule<LogisticsConduit> = Module.registerModule(Identifier.fromNamespaceAndPath(Femtocraft.ID, "logistics_node"), null)
    }
}

class ConduitBlockEntity(pos: BlockPos, state: BlockState) : FemtoBlockEntity(LogisticsContent.CONDUIT_BE.get(), pos, state) {
    @JvmField
    val conduit = LogisticsConduit()

    @JvmField
    val allChips = com.itszuvalex.itszulib.api.storage.ItemStorageAggregate(conduit.chips.map { it as com.itszuvalex.itszulib.api.storage.IItemStorage }.toTypedArray())

    init {
        fragList.addFragment(conduit)
        fragList.addInternalFragment(FragDropInventory(allChips))
        fragList.addFragment(FragMenu(Component.translatable("block.femtocraft.conduit"), { id, inv, _ -> ConduitMenu(id, inv, this) }))
    }
}

class ConduitBlock(properties: BlockBehaviour.Properties) : FemtoEntityBlock<ConduitBlockEntity>(properties, { LogisticsContent.CONDUIT_BE.get() }) {
    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = SHAPE

    companion object {
        private val SHAPE: VoxelShape = box(6.0, 6.0, 6.0, 10.0, 10.0, 10.0)
    }
}

