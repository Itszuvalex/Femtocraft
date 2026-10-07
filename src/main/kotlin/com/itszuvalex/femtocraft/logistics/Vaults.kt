package com.itszuvalex.femtocraft.logistics

import com.itszuvalex.femtocraft.core.FemtoEntityBlock
import com.itszuvalex.femtocraft.core.FemtoMenu
import com.itszuvalex.femtocraft.industry.FrameMachineBlockEntity
import com.itszuvalex.femtocraft.industry.FrameMultiblocks
import com.itszuvalex.femtocraft.industry.PackedMultiblocks
import com.itszuvalex.femtocraft.industry.PackedState
import net.minecraft.network.chat.Component
import java.util.Locale
import com.itszuvalex.femtocraft.nanite.FragNaniteAutoIO
import com.itszuvalex.femtocraft.nanite.FragNaniteTank
import com.itszuvalex.femtocraft.nanite.INaniteTank
import com.itszuvalex.femtocraft.nanite.NaniteMachineMenu
import com.itszuvalex.femtocraft.nanite.NaniteModules
import com.itszuvalex.femtocraft.nanite.NaniteStack
import com.itszuvalex.femtocraft.nanite.NaniteTank
import com.itszuvalex.femtocraft.nanite.PlayerNanites
import com.itszuvalex.femtocraft.nanite.SidedNaniteStorageConfiguration
import com.itszuvalex.itszulib.api.Modules
import com.itszuvalex.itszulib.api.adapters.IFluidStack
import com.itszuvalex.itszulib.api.adapters.ILevel
import com.itszuvalex.itszulib.api.multiblock.IMultiblockMember
import com.itszuvalex.itszulib.api.multiblock.IMultiblockState
import com.itszuvalex.itszulib.api.multiblock.MultiblockFaces
import com.itszuvalex.itszulib.api.multiblock.MultiblockSidedFluidStorageConfiguration
import com.itszuvalex.itszulib.api.multiblock.MultiblockSidedItemStorageConfiguration
import com.itszuvalex.itszulib.api.storage.DynamicIFluidStorage
import com.itszuvalex.itszulib.api.storage.DynamicIItemStorage
import com.itszuvalex.itszulib.api.storage.FluidStorageArray
import com.itszuvalex.itszulib.api.storage.FluidStorageIndex
import com.itszuvalex.itszulib.api.storage.IFluidStorage
import com.itszuvalex.itszulib.api.storage.IItemStorage
import com.itszuvalex.itszulib.api.storage.IndexedFluidStorage
import com.itszuvalex.itszulib.api.storage.IndexedItemStorage
import com.itszuvalex.itszulib.api.storage.ItemStorageArray
import com.itszuvalex.itszulib.api.storage.ItemStorageIndex
import com.itszuvalex.itszulib.core.EnumAutomaticIO
import com.itszuvalex.itszulib.core.SidedFluidStorageConfiguration
import com.itszuvalex.itszulib.core.SidedItemStorageConfiguration
import com.itszuvalex.itszulib.core.frag.FragFluidAutoIO
import com.itszuvalex.itszulib.core.frag.FragFluidStorage
import com.itszuvalex.itszulib.core.frag.FragItemAutoIO
import com.itszuvalex.itszulib.core.frag.FragItemStorage
import com.itszuvalex.itszulib.core.frag.FragSidedConfiguration
import com.itszuvalex.itszulib.core.frag.addFluidStorage
import com.itszuvalex.itszulib.core.frag.addItemStorage
import com.itszuvalex.itszulib.core.frag.addTickableFragment
import com.itszuvalex.itszulib.menu.MenuSync
import com.itszuvalex.itszulib.menu.MenuSyncs
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

// Frame-built storage multiblocks (3x3x3): every block of a vault exposes the shared storage on its outer faces (side
// configuration per block, faces between vault blocks expose nothing) and moves contents automatically. The shared
// storage lives on the home block, saved with its chunk, and breaking any block drops the items (fluids and nanites
// are lost, as a broken tank's are).

private const val NONE = "None"

/**
 * Logistics modules: [ITEM_INDEX] is an indexed inventory's [ItemStorageIndex] on the faces that expose it, so logistics
 * chips can take exactly what they are filtered for without scanning slots.
 */
object LogisticsModules {
    @JvmField
    val ITEM_INDEX: com.itszuvalex.itszulib.api.adapters.IModule<ItemStorageIndex> =
        com.itszuvalex.itszulib.api.adapters.Module.registerModule(Identifier.fromNamespaceAndPath(com.itszuvalex.femtocraft.Femtocraft.ID, "item_index"), null)

    fun init() {}
}

/**
 * Exposes [index] through [LogisticsModules.ITEM_INDEX] on the faces whose item configuration shows [storage] (so a
 * face set to expose nothing gives no index either). Saves nothing.
 */
class FragItemIndex(private val storage: IItemStorage, private val index: () -> ItemStorageIndex?) :
    com.itszuvalex.itszulib.core.frag.BlockEntityFragment<ItemStorageIndex>() {
    override fun name(): String = "ItemIndex"
    override fun module(): com.itszuvalex.itszulib.api.adapters.IModule<ItemStorageIndex> = LogisticsModules.ITEM_INDEX
    override fun faceToModuleMapper(be: com.itszuvalex.itszulib.api.adapters.IBlockEntity): (Direction?) -> ItemStorageIndex? = { side ->
        val config = host?.blockEntity()?.getModule(Modules.ITEM_STORAGE_CONFIGURABLE, null)
        if (side == null || config == null || config.getStorageForGlobalFacing(side) === storage) index() else null
    }
    override fun handlesScope(scope: com.itszuvalex.itszulib.api.utility.NBTSerializationScope): Boolean = false
    override fun serializeTo(scope: com.itszuvalex.itszulib.api.utility.NBTSerializationScope, output: ValueOutput) {}
    override fun deserialize(input: ValueInput, scope: com.itszuvalex.itszulib.api.utility.NBTSerializationScope) {}
}

/**
 * The item vault's contents: [SLOTS] slots in an ItszuLib [IndexedItemStorage], so finding, counting and taking an
 * item reads only the slots holding it, and an [index] over it for the terminal (and for anything that wants several
 * vaults searched as one).
 */
class ItemVaultState(onChanged: Runnable) : PackedState {
    @JvmField
    val storage = IndexedItemStorage(ItemStorageArray(SLOTS, onChanged))

    @JvmField
    val index = ItemStorageIndex().apply { add(storage) }

    fun drops() = (0 until storage.size()).map { storage.get(it).toMinecraft().copy() }.filter { !it.isEmpty }

    override fun onBreak(level: ILevel, anchor: BlockPos, brokenAt: BlockPos) = PackedMultiblocks.drop(level, anchor, FrameMultiblocks.ITEM_VAULT, this)

    override fun isPackedEmpty(): Boolean = (0 until storage.size()).all { storage.get(it).isEmpty() }

    override fun describe(): List<Component> {
        val used = (0 until storage.size()).count { !storage.get(it).isEmpty() }
        val total = (0 until storage.size()).sumOf { storage.get(it).stackSize().toLong() }
        return listOf(Component.translatable("tooltip.itszulib.contents.items", "%,d".format(Locale.ROOT, used), "%,d".format(Locale.ROOT, storage.size()), "%,d".format(Locale.ROOT, total)))
    }

    override fun serialize(output: ValueOutput) = storage.serialize(output.child("Items"))

    override fun deserialize(input: ValueInput) {
        input.child("Items").ifPresent(storage::deserialize)
    }

    companion object {
        /** Nine slots per block. */
        const val SLOTS = 27 * 9
    }
}

/**
 * The fluid reservoir's contents: [TANKS] cells of [CAPACITY] mB ([ReservoirTanks]: neighbouring cells link into one
 * tank, and a tank can be locked to a fluid) behind an [IndexedFluidStorage] (a fluid goes to the tanks holding it,
 * then to empty tanks locked to it, then to any empty one), with an [index] over it.
 */
class FluidReservoirState(onChanged: Runnable) : PackedState {
    @JvmField
    val cells = ReservoirTanks(TANKS, CAPACITY, onChanged)

    @JvmField
    val tanks = ReservoirIndexedStorage(cells)

    @JvmField
    val index = FluidStorageIndex().apply { add(tanks) }

    override fun onBreak(level: ILevel, anchor: BlockPos, brokenAt: BlockPos) = PackedMultiblocks.drop(level, anchor, FrameMultiblocks.FLUID_RESERVOIR, this)

    override fun isPackedEmpty(): Boolean = (0 until cells.size()).all { cells.get(it).isEmpty() }

    override fun describe(): List<Component> = (0 until cells.size()).map { i ->
        val stack = cells.get(i).toMinecraft()
        Component.translatable(
            "tooltip.itszulib.contents.fluid",
            if (stack.isEmpty) Component.translatable("gui.itszulib.tank.empty") else stack.hoverName,
            "%,d".format(Locale.ROOT, stack.amount), "%,d".format(Locale.ROOT, cells.capacity(i)),
        )
    }

    override fun serialize(output: ValueOutput) = tanks.serialize(output.child("Tanks"))

    override fun deserialize(input: ValueInput) {
        input.child("Tanks").ifPresent(tanks::deserialize)
    }

    /** Links or unlinks the cells either side of [boundary] (the index follows the tanks changing). */
    fun toggleLink(boundary: Int): Boolean {
        val done = if (cells.isLinked(boundary)) cells.unlink(boundary) else cells.link(boundary)
        if (done) tanks.rebuild()
        return done
    }

    /** Locks or unlocks the tank holding [cell] (see [ReservoirTanks.toggleLock]). */
    fun toggleLock(cell: Int, fallback: Identifier?): Boolean {
        if (cell !in 0 until TANKS) return false
        return cells.toggleLock(cells.tankOfCell(cell), fallback)
    }

    companion object {
        const val TANKS = 4
        const val CAPACITY = 64_000

        /** A change of at least this much syncs at once; smaller ones wait for [SYNC_TICKS]. */
        const val SYNC_STEP = CAPACITY / 64
        const val SYNC_TICKS = 20

        /**
         * Whether clients need the tanks again: a tank's fluid changed, an amount moved by [SYNC_STEP] or more, or
         * anything changed and [SYNC_TICKS] have passed since the last sync.
         */
        @JvmStatic
        fun needsSync(last: List<IFluidStack>, now: List<IFluidStack>, ticksSince: Int): Boolean {
            if (last.size != now.size) return true
            var changed = false
            for (i in now.indices) {
                val a = last[i]
                val b = now[i]
                if (a.isEmpty() != b.isEmpty() || (!a.isEmpty() && !a.isFluidEqual(b))) return true
                val delta = kotlin.math.abs(a.amount() - b.amount())
                if (delta >= SYNC_STEP) return true
                if (delta > 0) changed = true
            }
            return changed && ticksSince >= SYNC_TICKS
        }
    }
}

/**
 * The nanite vault's contents: one [NaniteTank] of [VOLUME] holding any number of strains.
 */
class NaniteVaultState(onChanged: Runnable) : PackedState {
    @JvmField
    val tank = NaniteTank(VOLUME, onChanged)

    override fun onBreak(level: ILevel, anchor: BlockPos, brokenAt: BlockPos) = PackedMultiblocks.drop(level, anchor, FrameMultiblocks.NANITE_VAULT, this)

    override fun isPackedEmpty(): Boolean = tank.amount <= 0

    override fun describe(): List<Component> = listOf(Component.translatable("tooltip.femtocraft.contents.nanites", tank.amount, tank.capacity))

    override fun serialize(output: ValueOutput) = tank.serialize(output.child("Nanites"))

    override fun deserialize(input: ValueInput) {
        input.child("Nanites").ifPresent(tank::deserialize)
    }

    companion object {
        const val VOLUME = 10_000
    }
}

/**
 * [SidedNaniteStorageConfiguration] for a multiblock member: faces towards another block of the same structure
 * expose nothing and move nothing (the nanite counterpart of ItszuLib's `MultiblockSidedItemStorageConfiguration`).
 */
class MultiblockSidedNaniteStorageConfiguration(
    level: () -> ILevel?,
    pos: () -> BlockPos,
    member: IMultiblockMember,
    private val emptyStorage: String,
    defaults: (Direction) -> String,
    storages: Map<String, INaniteTank>,
    front: () -> Direction,
) : SidedNaniteStorageConfiguration(defaults, storages, front) {
    private val faces = MultiblockFaces(level, pos, member, front)

    override fun getStorageNameForAbsoluteFacing(direction: Direction): String =
        if (faces.internal(direction)) emptyStorage else super.getStorageNameForAbsoluteFacing(direction)

    override fun getStorageNameForRelativeFacing(direction: Direction): String =
        if (faces.internalRelative(direction)) emptyStorage else super.getStorageNameForRelativeFacing(direction)

    override fun getIOForAbsoluteFacing(direction: Direction): EnumAutomaticIO =
        if (faces.internal(direction)) EnumAutomaticIO.NONE else super.getIOForAbsoluteFacing(direction)

    override fun getIOForRelativeFacing(direction: Direction): EnumAutomaticIO =
        if (faces.internalRelative(direction)) EnumAutomaticIO.NONE else super.getIOForRelativeFacing(direction)

    override fun cycleRelativeFacingStorageForward(direction: Direction) {
        if (!faces.internalRelative(direction)) super.cycleRelativeFacingStorageForward(direction)
    }

    override fun cycleRelativeFacingStorageBackward(direction: Direction) {
        if (!faces.internalRelative(direction)) super.cycleRelativeFacingStorageBackward(direction)
    }

    override fun cycleRelativeFacingIOForward(direction: Direction) {
        if (!faces.internalRelative(direction)) super.cycleRelativeFacingIOForward(direction)
    }

    override fun cycleRelativeFacingIOBackward(direction: Direction) {
        if (!faces.internalRelative(direction)) super.cycleRelativeFacingIOBackward(direction)
    }
}

/**
 * A block of the item vault (3x3x3, built from frames): 243 indexed slots, searched and paged through a storage
 * terminal.
 */
class ItemVaultBlockEntity(pos: BlockPos, state: BlockState) :
    FrameMachineBlockEntity<ItemVaultState>(LogisticsContent.ITEM_VAULT_BE.get(), pos, state, { FrameMultiblocks.ITEM_VAULT }) {
    @JvmField
    val storage: IItemStorage = DynamicIItemStorage { state()?.storage ?: IItemStorage.Empty }

    init {
        addFrameMachineFragments("block.femtocraft.item_vault") { id, inv -> ItemVaultMenu(id, inv, this) }
        fragList.addFragment(FragSidedConfiguration<SidedItemStorageConfiguration>(
            "ItemConfig",
            MultiblockSidedItemStorageConfiguration({ level?.let(ILevel::of) }, { blockPos }, part, NONE, { VAULT },
                mapOf(NONE to IItemStorage.Empty, VAULT to storage), { Direction.NORTH }),
            Modules.ITEM_STORAGE_CONFIGURABLE,
        ))
        fragList.addItemStorage(FragItemStorage(storage, persist = false))
        fragList.addTickableFragment(FragItemAutoIO())
        fragList.addFragment(FragItemIndex(storage) { index() })
    }

    /** The vault's index (null while not formed, or while the home block's chunk loads). */
    fun index(): ItemStorageIndex? = state()?.index

    companion object {
        const val VAULT = "Vault"
    }
}

/**
 * A block of the fluid reservoir (3x3x3, built from frames): four large tanks. Its walls have windows; the home block
 * syncs the tanks to clients ([clientTanks]) so the renderer can draw them inside
 * ([com.itszuvalex.femtocraft.client.FemtoRenderers.ReservoirRenderer]).
 */
class FluidReservoirBlockEntity(pos: BlockPos, state: BlockState) :
    FrameMachineBlockEntity<FluidReservoirState>(LogisticsContent.FLUID_RESERVOIR_BE.get(), pos, state, { FrameMultiblocks.FLUID_RESERVOIR }) {
    @JvmField
    val tanks: IFluidStorage = DynamicIFluidStorage { state()?.tanks ?: IFluidStorage.Empty }

    init {
        addFrameMachineFragments("block.femtocraft.fluid_reservoir") { id, inv -> FluidReservoirMenu(id, inv, this) }
        fragList.addFragment(FragSidedConfiguration<SidedFluidStorageConfiguration>(
            "FluidConfig",
            MultiblockSidedFluidStorageConfiguration({ level?.let(ILevel::of) }, { blockPos }, part, NONE, { TANKS },
                mapOf(NONE to IFluidStorage.Empty, TANKS to tanks), { Direction.NORTH }),
            Modules.FLUID_STORAGE_CONFIGURABLE,
        ))
        fragList.addFluidStorage(FragFluidStorage(tanks, persist = false))
        fragList.addTickableFragment(FragFluidAutoIO())
        fragList.addInternalFragment(com.itszuvalex.femtocraft.core.FragData("TankView", setOf(com.itszuvalex.itszulib.api.utility.NBTSerializationScope.DESCRIPTION),
            { _, o -> if (isHome) tanks.serialize(o) }, { _, i -> clientTanks.deserialize(i) }))
    }

    /** Client side, on the home block: the tanks as last synced, for the renderer. */
    @JvmField
    val clientTanks = ReservoirTanks(FluidReservoirState.TANKS, FluidReservoirState.CAPACITY)

    private var synced: List<IFluidStack> = emptyList()
    private var ticksSinceSync = 0

    /** On the home block: sends the tanks to clients when [FluidReservoirState.needsSync] says they changed enough. */
    override fun serverTick() {
        if (!isHome) return
        val now = (0 until tanks.size()).map { tanks.get(it).copy() }
        ticksSinceSync++
        if (!FluidReservoirState.needsSync(synced, now, ticksSinceSync)) return
        synced = now
        ticksSinceSync = 0
        markDirtyAndSync()
    }

    companion object {
        const val TANKS = "Tanks"
    }
}

/**
 * A block of the nanite vault (3x3x3, built from frames): one tank for any number of strains.
 */
class NaniteVaultBlockEntity(pos: BlockPos, state: BlockState) :
    FrameMachineBlockEntity<NaniteVaultState>(LogisticsContent.NANITE_VAULT_BE.get(), pos, state, { FrameMultiblocks.NANITE_VAULT }) {
    /** The vault's tank, through this block (empty while the structure is not loaded). */
    @JvmField
    val tank: INaniteTank = object : INaniteTank {
        private fun t(): INaniteTank = state()?.tank ?: INaniteTank.EMPTY
        override val capacity: Int get() = t().capacity
        override val amount: Int get() = t().amount
        override fun contents(): List<NaniteStack> = t().contents()
        override fun canFill(stack: NaniteStack) = t().canFill(stack)
        override fun canDrain(stack: NaniteStack) = t().canDrain(stack)
        override fun fill(stack: NaniteStack, doFill: Boolean) = t().fill(stack, doFill)
        override fun drain(stack: NaniteStack, doDrain: Boolean) = t().drain(stack, doDrain)
        override fun serialize(output: ValueOutput) {}
        override fun deserialize(input: ValueInput) {}
    }

    init {
        addFrameMachineFragments("block.femtocraft.nanite_vault") { id, inv -> NaniteVaultMenu(id, inv, this) }
        fragList.addFragment(FragSidedConfiguration<SidedNaniteStorageConfiguration>(
            "NaniteConfig",
            MultiblockSidedNaniteStorageConfiguration({ level?.let(ILevel::of) }, { blockPos }, part, NONE, { TANK },
                mapOf(NONE to INaniteTank.EMPTY, TANK to tank), { Direction.NORTH }),
            NaniteModules.NANITE_STORAGE_CONFIGURABLE,
        ))
        fragList.addFragment(FragNaniteTank(tank, persist = false))
        fragList.addTickableFragment(FragNaniteAutoIO())
    }

    companion object {
        const val TANK = "Tank"
    }
}

class ItemVaultBlock(p: BlockBehaviour.Properties) : FemtoEntityBlock<ItemVaultBlockEntity>(p, { LogisticsContent.ITEM_VAULT_BE.get() })
class FluidReservoirBlock(p: BlockBehaviour.Properties) : FemtoEntityBlock<FluidReservoirBlockEntity>(p, { LogisticsContent.FLUID_RESERVOIR_BE.get() })
class NaniteVaultBlock(p: BlockBehaviour.Properties) : FemtoEntityBlock<NaniteVaultBlockEntity>(p, { LogisticsContent.NANITE_VAULT_BE.get() })

/**
 * The item vault's menu: a storage terminal over the vault's index (search, sort, pages, click to take and put) and
 * the player's inventory, whose shift-clicks go into the vault.
 */
class ItemVaultMenu(containerId: Int, inventory: Inventory, be: ItemVaultBlockEntity?) :
    FemtoMenu<ItemVaultBlockEntity>(LogisticsContent.ITEM_VAULT_MENU.get(), containerId, inventory, be) {
    @JvmField
    val vault = enableStorageTerminal { be?.index() }

    init {
        addPlayerInventorySlots(inventory, 8, INVENTORY_Y)
    }

    companion object {
        /** Terminal rows: five, so the screen (232 high) fits a 720p window at GUI scale 3. */
        const val ROWS = 5
        const val INVENTORY_Y = 17 + 14 + ROWS * 18 + 14 + 15
        const val HEIGHT = INVENTORY_Y + 58 + 18 + 6
    }
}

/**
 * The fluid reservoir's menu: its cells, locks and links (client copies in [view]), and actions to lock a tank
 * ([ACTION_LOCK], data: one of its cells; an empty tank locks to the fluid in the carried container) and to link or
 * unlink neighbouring cells ([ACTION_LINK], data: the boundary).
 */
class FluidReservoirMenu(containerId: Int, inventory: Inventory, be: FluidReservoirBlockEntity?) :
    FemtoMenu<FluidReservoirBlockEntity>(LogisticsContent.FLUID_RESERVOIR_MENU.get(), containerId, inventory, be) {
    @JvmField
    val view = ReservoirTanks(FluidReservoirState.TANKS, FluidReservoirState.CAPACITY)

    init {
        addPlayerInventorySlots(inventory, 8, INVENTORY_Y)
        fun cells() = be?.state()?.cells
        for (i in 0 until FluidReservoirState.TANKS) {
            addSync(MenuSync({ cells()?.cell(i) ?: IFluidStack.Empty }, { view.mirrorCell(i, it) }, MenuSyncs.FLUID,
                { a, b -> a.amount() == b.amount() && a.isFluidEqual(b) }, { it.copy() }))
            addSync(MenuSyncs.string({ cells()?.let { c -> c.lockOf(c.tankOfCell(i))?.toString() } ?: "" }, { view.mirrorLock(i, it.takeIf { s -> s.isNotEmpty() }?.let(Identifier::tryParse)) }))
        }
        addSync(MenuSyncs.int({ cells()?.linkMask() ?: 0 }, { view.mirrorLinks(it) }))
    }

    override fun handleAction(player: Player, action: Int, data: Int): Boolean {
        val state = blockEntity?.state() ?: return false
        return when (action) {
            ACTION_LOCK -> {
                val held = net.neoforged.neoforge.fluids.FluidUtil.getFluidContained(carried).orElse(null)
                val fallback = held?.takeUnless { it.isEmpty }?.let { net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(it.fluid) }
                state.toggleLock(data, fallback)
            }
            ACTION_LINK -> state.toggleLink(data)
            else -> false
        }
    }

    companion object {
        const val ACTION_LOCK = 0
        const val ACTION_LINK = 1
        const val INVENTORY_Y = 113
        const val HEIGHT = INVENTORY_Y + 58 + 18 + 6
    }
}

/**
 * The nanite vault's menu: its strains, the player's nanites, and buttons to fill the vault from the player or drain it
 * into them.
 */
class NaniteVaultMenu(containerId: Int, inventory: Inventory, be: NaniteVaultBlockEntity?) :
    FemtoMenu<NaniteVaultBlockEntity>(LogisticsContent.NANITE_VAULT_MENU.get(), containerId, inventory, be) {
    var tank: List<NaniteStack> = listOf()
    var playerTank: List<NaniteStack> = listOf()

    init {
        addPlayerInventorySlots(inventory)
        if (be != null) addSync(MenuSync({ be.tank.contents() }, { tank = it }, NaniteMachineMenu.LIST))
        addSync(MenuSync({ PlayerNanites.tank(inventory.player).contents() }, { playerTank = it }, NaniteMachineMenu.LIST))
    }

    override fun handleAction(player: Player, action: Int, data: Int): Boolean {
        val tank = blockEntity?.tank ?: return false
        when (action) {
            NaniteMachineMenu.ACTION_FILL -> PlayerNanites.fill(player, tank)
            NaniteMachineMenu.ACTION_DRAIN -> PlayerNanites.drain(player, tank)
            else -> return false
        }
        return true
    }
}
