package com.itszuvalex.femtocraft.logistics

import com.itszuvalex.femtocraft.core.FemtoEntityBlock
import com.itszuvalex.femtocraft.core.FemtoMenu
import com.itszuvalex.femtocraft.industry.FrameMachineBlockEntity
import com.itszuvalex.femtocraft.industry.FrameMultiblocks
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
 * The item vault's contents: [SLOTS] slots in an ItszuLib [IndexedItemStorage], so finding, counting and taking an
 * item reads only the slots holding it, and an [index] over it for the terminal (and for anything that wants several
 * vaults searched as one).
 */
class ItemVaultState(onChanged: Runnable) : IMultiblockState {
    @JvmField
    val storage = IndexedItemStorage(ItemStorageArray(SLOTS, onChanged))

    @JvmField
    val index = ItemStorageIndex().apply { add(storage) }

    fun drops() = (0 until storage.size()).map { storage.get(it).toMinecraft().copy() }.filter { !it.isEmpty }

    override fun onBreak(level: ILevel, anchor: BlockPos, brokenAt: BlockPos) =
        drops().forEach { Block.popResource(level.toMinecraft(), anchor, it) }

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
 * The fluid reservoir's contents: [TANKS] tanks of [CAPACITY] mB in an [IndexedFluidStorage] (a fluid goes to the tanks
 * holding it first, then to an empty one), with an [index] over it.
 */
class FluidReservoirState(onChanged: Runnable) : IMultiblockState {
    @JvmField
    val tanks = IndexedFluidStorage(FluidStorageArray(TANKS, CAPACITY, onChanged))

    @JvmField
    val index = FluidStorageIndex().apply { add(tanks) }

    override fun onBreak(level: ILevel, anchor: BlockPos, brokenAt: BlockPos) {}

    override fun serialize(output: ValueOutput) = tanks.serialize(output.child("Tanks"))

    override fun deserialize(input: ValueInput) {
        input.child("Tanks").ifPresent(tanks::deserialize)
    }

    companion object {
        const val TANKS = 4
        const val CAPACITY = 64_000
    }
}

/**
 * The nanite vault's contents: one [NaniteTank] of [VOLUME] holding any number of strains.
 */
class NaniteVaultState(onChanged: Runnable) : IMultiblockState {
    @JvmField
    val tank = NaniteTank(VOLUME, onChanged)

    override fun onBreak(level: ILevel, anchor: BlockPos, brokenAt: BlockPos) {}

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
    }

    /** The vault's index (null while not formed, or while the home block's chunk loads). */
    fun index(): ItemStorageIndex? = state()?.index

    companion object {
        const val VAULT = "Vault"
    }
}

/**
 * A block of the fluid reservoir (3x3x3, built from frames): four large tanks.
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
 * The fluid reservoir's menu: its four tanks (client copies in [tanks]).
 */
class FluidReservoirMenu(containerId: Int, inventory: Inventory, be: FluidReservoirBlockEntity?) :
    FemtoMenu<FluidReservoirBlockEntity>(LogisticsContent.FLUID_RESERVOIR_MENU.get(), containerId, inventory, be) {
    @JvmField
    val tanks = FluidStorageArray(FluidReservoirState.TANKS, FluidReservoirState.CAPACITY)

    init {
        addPlayerInventorySlots(inventory)
        for (i in 0 until FluidReservoirState.TANKS) {
            addSync(MenuSync({ be?.tanks?.get(i) ?: IFluidStack.Empty }, { tanks.setQuietly(i, it) }, MenuSyncs.FLUID,
                { a, b -> a.amount() == b.amount() && a.isFluidEqual(b) }, { it.copy() }))
        }
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
