package com.itszuvalex.femtocraft.logistics

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.core.ConduitArms
import com.itszuvalex.femtocraft.core.FemtoBlockEntity
import com.itszuvalex.femtocraft.core.FemtoEntityBlock
import com.itszuvalex.femtocraft.nanite.NaniteModules
import com.itszuvalex.femtocraft.power.WirelessPowerNetwork
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
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import net.neoforged.fml.LogicalSide
import net.neoforged.neoforge.capabilities.Capabilities

/**
 * Network of logistics conduits. Each tick every chip earns its passive flops, then, for each chip kind, input chips'
 * buffers are offered to output chips of that kind (non-empty buffers first, then by contents) on the same channel
 * that can pass them on. Port of v3's `LogisticsNetwork`; v3 only implemented item connections, fluid and nanite
 * chips follow the same rules here.
 */
class LogisticsNetwork(id: Int) : TileNetwork<LogisticsConduit, LogisticsNetwork>(id, LogicalSide.SERVER) {
    override fun networkModule(): IModule<LogisticsConduit> = LogisticsConduit.MODULE

    override fun create(): LogisticsNetwork = LogisticsNetwork(WirelessPowerNetwork.nextId())

    fun connections(): List<Connection<*>> = getNodes().flatMap { it.connections() }.toList()

    override fun onTickEnd() {
        val connections = connections()
        connections.filter { it.active }.forEach { it.contributeFlops(it.passiveFlopGen) }
        Chips.KINDS.forEach { route(it, connections) }
    }

    private fun <B : Any> route(kind: ChipKind<B>, all: List<Connection<*>>) {
        @Suppress("UNCHECKED_CAST")
        val connections = all.filter { it.kind === kind } as List<Connection<B>>
        val outputs = connections.filter { it.direction == ConnectionDirection.OUTPUT }
            .sortedWith(compareBy<Connection<B>> { kind.isEmpty(it.buffer) }.thenBy { kind.sortKey(it.buffer) })
        for (input in connections.filter { it.direction == ConnectionDirection.INPUT && !kind.isEmpty(it.buffer) }) {
            for (output in outputs) {
                if (kind.isEmpty(input.buffer)) break
                if (output.channel != input.channel || !output.canInsert(input.buffer)) continue
                input.setBuffer(output.insert(input.buffer))
            }
        }
    }
}

/**
 * A logistics conduit: joins neighbouring conduits into a [LogisticsNetwork] and holds up to four chips per face
 * (stack size 1) of any kind. Faces towards an inventory, tank, nanite tank or another conduit count as connected (synced, key
 * `Inv`, for the model). Port of v3's `ModuleConduit`/`TileConduit`.
 */
class LogisticsConduit : FragNetworkedWire<LogisticsConduit, LogisticsNetwork>({ LogisticsNetwork(WirelessPowerNetwork.nextId()) }) {
    @JvmField
    val inventoryFaces = FaceBitSet()

    private var lvl: Level? = null

    @JvmField
    val chips: Array<ChipSlots> = Array(6) { ChipSlots(Direction.from3DDataValue(it)) }

    /**
     * Which kind of chip sits in each slot ([ChipNodes]), for the chips shown in the world: kept current on the server
     * and synced (key `Chips`) whenever it changes.
     */
    var chipLayout: Long = 0L
        private set

    private fun computeLayout(): Long = (0 until ChipNodes.SLOTS).fold(0L) { layout, slot ->
        ChipNodes.withKind(layout, slot, ChipNodes.kindOf(chips[slot / CHIPS_PER_FACE].chip(slot % CHIPS_PER_FACE)))
    }

    /** A chip slot changed: save, and sync if the chips shown in the world changed. */
    private fun chipsChanged() {
        val layout = computeLayout()
        if (layout == chipLayout) markDirty()
        else {
            chipLayout = layout
            markDirtyAndSync()
        }
    }

    /**
     * The chips of one face. Each chip's flop countdown lives here while the chip is in the conduit, so ticking does not
     * rewrite the chip (REVIEW O5): it is read from the chip when the chip is placed or loaded, and written back
     * whenever the slot is read from outside ([get]: taking it out, dropping it, saving, an open menu).
     */
    inner class ChipSlots(private val face: Direction) : ItemStorageArray(CHIPS_PER_FACE, { chipsChanged() }) {
        /** Each slot's countdown; null until read from its chip. */
        private val flops = arrayOfNulls<Double>(CHIPS_PER_FACE)

        override fun canInsert(index: Int, stack: IItemStack): Boolean = stack.isEmpty() || Chips.isChip(stack.toMinecraft())

        override fun maxStackSize(index: Int): Int = 1

        /** The chip in [index], without writing its countdown back. */
        fun chip(index: Int): ItemStack = super.get(index).toMinecraft()

        override fun get(index: Int): IItemStack {
            flops[index]?.let { Chips.writeFlops(chip(index), face, it) }
            return super.get(index)
        }

        override fun setSlot(index: Int, stack: IItemStack) {
            flops[index] = null
            super.setSlot(index, stack)
        }

        override fun setSlotQuietly(index: Int, stack: IItemStack) {
            flops[index] = null
            super.setSlotQuietly(index, stack)
        }

        fun counter(index: Int): FlopCounter = object : FlopCounter {
            override var flops: Double
                get() = this@ChipSlots.flops[index] ?: (Chips.settingsOf(chip(index), face)?.flops ?: 0.0).also { this@ChipSlots.flops[index] = it }
                set(value) {
                    this@ChipSlots.flops[index] = value
                }

            /** Flags the chunk for saving without `setChanged`'s neighbour updates; saving writes the countdown back. */
            override fun onOperation() {
                val pos = host?.blockEntity()?.getBlockPos() ?: return
                lvl?.blockEntityChanged(pos)
            }
        }
    }

    override fun module(): IModule<LogisticsConduit> = MODULE

    /**
     * The conduit's active chips as a computation job (DECISIONS D19): FLOPS from a computation network run their
     * countdowns down on top of the passive rate, at most one operation per chip per tick.
     */
    @JvmField
    val computationJob = object : com.itszuvalex.itszulib.core.Distributable {
        private fun remaining(): Double = connections().filter { it.active }.sumOf { it.flopsRemaining }
        override val max: Double get() = remaining()
        override val amount: Double get() = 0.0
        override val transferMax: Double get() = remaining()
        override fun add(amount: Double): Double {
            var left = amount
            for (connection in connections()) {
                if (left <= 0.0) break
                if (!connection.active) continue
                val give = minOf(left, connection.flopsRemaining)
                left -= give - connection.contributeFlops(give)
            }
            return amount - left
        }
        override fun remove(amount: Double): Double = 0.0
    }

    fun connections(): List<Connection<*>> {
        val level = lvl ?: return listOf()
        val pos = host?.blockEntity()?.getBlockPos() ?: return listOf()
        return Direction.entries.flatMap { face ->
            val storage = chips[face.get3DDataValue()]
            (0 until storage.size()).mapNotNull { Chips.connection(storage.chip(it), level, pos, face, storage.counter(it)) { markDirty() } }
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
        if (scope == NBTSerializationScope.DESCRIPTION) output.putLong(CHIPS_KEY, chipLayout)
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
            chipLayout = computeLayout()
        }
        if (scope == NBTSerializationScope.DESCRIPTION) chipLayout = input.getLongOr(CHIPS_KEY, 0L)
    }

    companion object {
        const val CHIPS_PER_FACE = 4
        const val INV_KEY = "Inv"
        const val STORAGE_KEY = "storage"
        const val CHIPS_KEY = "Chips"

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
        fragList.addFragment(com.itszuvalex.femtocraft.computation.FragComputationLeaf {
            if (conduit.connections().any { it.active }) com.itszuvalex.femtocraft.computation.ComputationParticipant(com.itszuvalex.itszulib.core.DistributionRole.CONSUMER, conduit.computationJob) else null
        })
    }

    /**
     * Arms towards connected conduits and inventories (the model's, see [ConduitArms]).
     */
    override fun serverTick() = ConduitArms.sync(this) { conduit.isConnected(it) || conduit.inventoryFaces[it] }

    /** Whether [state] (this block's) shows an arm on [face]. */
    fun hasArm(state: BlockState, face: Direction): Boolean = ConduitArms.PROPERTIES.getValue(face).let { it in state.properties && state.getValue(it) }

    /** The last shape and what it was made for, as one value (shapes are also read off-thread by chunk rendering). */
    @Volatile
    private var cachedShape: Triple<BlockState, Long, VoxelShape>? = null

    /** [state]'s arms and core plus the cubes of the chips in the conduit ([ChipNodes]). */
    fun shape(state: BlockState): VoxelShape {
        val layout = conduit.chipLayout
        cachedShape?.let { (s, l, shape) -> if (s === state && l == layout) return shape }
        val shape = Shapes.or(ConduitArms.shape(state), ChipNodes.shape(layout) { hasArm(state, it) })
        cachedShape = Triple(state, layout, shape)
        return shape
    }

    /** The chip slot whose cube [hit] (a point in the world) is on, or -1. */
    fun chipSlotAt(hit: net.minecraft.world.phys.Vec3): Int =
        ChipNodes.slotAt(conduit.chipLayout, { hasArm(blockState, it) }, hit.subtract(net.minecraft.world.phys.Vec3.atLowerCornerOf(blockPos)))

    /** Opens the menu of the chip in [slot] ([ChipMenu]) for [player]. */
    fun openChipMenu(player: net.minecraft.server.level.ServerPlayer, slot: Int) {
        val pos = blockPos
        player.openMenu(net.minecraft.world.SimpleMenuProvider({ id, inv, _ -> ChipMenu(id, inv, this, slot) }, conduit.chips[slot / LogisticsConduit.CHIPS_PER_FACE].chip(slot % LogisticsConduit.CHIPS_PER_FACE).hoverName)) { buf ->
            buf.writeBlockPos(pos)
            buf.writeVarInt(slot)
        }
    }
}

class ConduitBlock(properties: BlockBehaviour.Properties) : FemtoEntityBlock<ConduitBlockEntity>(properties, { LogisticsContent.CONDUIT_BE.get() }) {
    init {
        registerDefaultState(ConduitArms.withoutArms(stateDefinition.any()))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) = ConduitArms.addProperties(builder)

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape =
        (level.getBlockEntity(pos) as? ConduitBlockEntity)?.shape(state) ?: ConduitArms.shape(state)

    /** Using one of the chips shown on the conduit opens that chip's menu; anywhere else, the conduit's. */
    override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: net.minecraft.world.entity.player.Player, hitResult: net.minecraft.world.phys.BlockHitResult): net.minecraft.world.InteractionResult {
        val be = level.getBlockEntity(pos) as? ConduitBlockEntity ?: return super.useWithoutItem(state, level, pos, player, hitResult)
        val slot = be.chipSlotAt(hitResult.location)
        if (slot < 0) return super.useWithoutItem(state, level, pos, player, hitResult)
        if (player is net.minecraft.server.level.ServerPlayer) be.openChipMenu(player, slot)
        return net.minecraft.world.InteractionResult.SUCCESS
    }
}

