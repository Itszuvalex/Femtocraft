package com.itszuvalex.femtocraft.cyber

import com.itszuvalex.femtocraft.FemtoBlockEntities
import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.FemtoComponents
import com.itszuvalex.femtocraft.FemtoItems
import com.itszuvalex.femtocraft.FemtoMenus
import com.itszuvalex.femtocraft.core.FemtoBlockEntity
import com.itszuvalex.femtocraft.core.FemtoMenu
import com.itszuvalex.femtocraft.core.FluidTanks
import com.itszuvalex.femtocraft.core.FragData
import com.itszuvalex.femtocraft.core.FragMultiBlock
import com.itszuvalex.femtocraft.core.getLoc
import com.itszuvalex.femtocraft.core.putLoc
import com.itszuvalex.femtocraft.industry.IMultiblockPart
import com.itszuvalex.femtocraft.industry.MultiblockGuard
import com.itszuvalex.femtocraft.logistics.IndexedItemStorage
import com.itszuvalex.itszulib.api.adapters.IItemStack
import com.itszuvalex.itszulib.api.adapters.ILevel
import com.itszuvalex.itszulib.api.utility.Loc4
import com.itszuvalex.itszulib.core.frag.InternalBlockEntityFragment
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.neoforged.neoforge.capabilities.Capabilities
import com.itszuvalex.itszulib.api.wrappers.WrapperResourceHandlerIItemStorage
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.world.Containers
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.MenuProvider
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluids
import java.util.function.Consumer

/**
 * Base for cyber machines: a size x height x size platform (1x1, 2x2 or 3x3) with [getNumSlots] slots of machine
 * space above it. Holds a small inventory (9 input + buffer slots) and cybermass/buffer tanks. Port of 1.7.10
 * `TileCyberBase`.
 *
 * Cybermass is still a placeholder for water, as it was in 1.7.10 (`FemtoFluids.cybermass = FluidRegistry.WATER`).
 */
class CyberBaseBlockEntity(pos: BlockPos, state: BlockState) :
    FemtoBlockEntity(FemtoBlockEntities.CYBER_BASE.get(), pos, state), IMultiblockPart, MenuProvider {
    override val multiblock = FragMultiBlock { blockPos }
    var size = 1
        private set
    val inventory = IndexedItemStorage(MAX_INVENTORY_SIZE) { setChanged() }
    val itemHandler = WrapperResourceHandlerIItemStorage.of(inventory)
    var tanks = tanksFor(1)
        private set
    private val machines = sortedMapOf<Int, Loc4>()
    private var breaking = false

    init {
        fragList.addInternalFragment(multiblock)
        fragList.addInternalFragment(FragData("CyberBase", FragData.LEVEL_AND_DESCRIPTION, { scope, out ->
            val comp = out.child(COMPOUND_KEY)
            comp.putInt(SIZE_KEY, size)
            comp.store(MACHINES_KEY, MachineMapping.LIST_CODEC, machines.map { (slot, loc) -> MachineMapping(slot, loc) })
        }, { _, input ->
            input.child(COMPOUND_KEY).ifPresent { comp ->
                setSize(comp.getIntOr(SIZE_KEY, 1))
                machines.clear()
                comp.read(MACHINES_KEY, MachineMapping.LIST_CODEC).ifPresent { list -> list.forEach { machines[it.startingSlot] = it.controllerLoc } }
            }
        }))
        fragList.addInternalFragment(FragData("Storage", FragData.LEVEL, { _, out ->
            inventory.serialize(out.child("Inventory"))
            tanks.serialize(out.child("Tanks"))
        }, { _, input ->
            input.child("Inventory").ifPresent { inventory.deserialize(it) }
            input.child("Tanks").ifPresent { tanks.deserialize(it) }
        }))
        fragList.addCapability(Capabilities.Item.BLOCK) { controller()?.itemHandler }
        // Filling from the top is not allowed (1.7.10 canFill/canDrain: from != UP).
        fragList.addCapability(Capabilities.Fluid.BLOCK) { side -> if (side == Direction.UP) null else controller()?.tanks }
        fragList.addInternalFragment(object : InternalBlockEntityFragment() {
            override fun name(): String = "BreakBase"
            override fun onRemove(level: ILevel, pos: BlockPos, blockStatePrev: BlockState) = onBaseBroken(level.toMinecraft())
        })
    }

    fun setSize(newSize: Int) {
        if (newSize !in 1..3 || newSize == size && tanks.size() == tanksFor(newSize).size()) return
        size = newSize
        tanks = tanksFor(newSize)
        // The fluid capability hands out the tanks object.
        if (level != null) invalidateCapabilities()
    }

    private fun tanksFor(size: Int) = FluidTanks(
        when (size) {
            1 -> intArrayOf(2000, 2000)
            2 -> intArrayOf(2000, 4000)
            else -> intArrayOf(2000, 4000, 4000)
        },
        { setChanged() },
    )

    /**
     * Inventory slots used: 9 input + (size + 1)^2 buffer.
     */
    val usedInventorySize: Int get() = 9 + (size + 1) * (size + 1)

    fun controller(): CyberBaseBlockEntity? = if (multiblock.isController) this else multiblock.controller(level)

    fun getBaseHeight(): Int = BASE_HEIGHT[size] ?: 1
    fun getNumSlots(): Int = SLOT_HEIGHT[size] ?: 4
    fun yFromSlot(slot: Int): Int = blockPos.y + getBaseHeight() + slot

    private fun topSlotForMachine(start: Int, loc: Loc4): Int {
        val name = (loc.inLevelBlockEntity() as? ICyberMachinePart)?.machineName
        return start + ((CyberMachineRegistry.getMachine(name)?.requiredSlots ?: 1) - 1)
    }

    private fun Loc4.inLevelBlockEntity() = level?.getBlockEntity(pos)

    fun firstFreeSlot(): Int = machines.entries.lastOrNull()?.let { topSlotForMachine(it.key, it.value) + 1 } ?: 0

    fun remainingSlots(): Int = getNumSlots() - firstFreeSlot()

    /**
     * Starts building [name] in the first free slot. Resources and cybermass are not checked or consumed, as in 1.7.10.
     *
     * @return True if construction started.
     */
    fun buildMachine(name: String): Boolean {
        val lvl = level ?: return false
        if (lvl.isClientSide) return false
        if (!multiblock.isController) return controller()?.buildMachine(name) ?: false
        val m = CyberMachineRegistry.getMachine(name) ?: return false
        if (size != m.requiredBaseSize || remainingSlots() < m.requiredSlots) return false
        val freeSlot = firstFreeSlot()
        val controllerPos = BlockPos(blockPos.x, yFromSlot(freeSlot), blockPos.z)
        m.getTakenLocations(controllerPos).forEach { loc ->
            lvl.setBlockAndUpdate(loc, FemtoBlocks.CYBER_MACHINE_IN_PROGRESS.get().defaultBlockState())
            val cin = lvl.getBlockEntity(loc) as? CyberMachineInProgressBlockEntity ?: return@forEach
            cin.machineInProgress = name
            cin.machineIndex = freeSlot
            cin.basePos = loc()
            cin.multiblock.form(controllerPos)
        }
        machines[freeSlot] = Loc4.of(lvl, controllerPos)
        sync()
        return true
    }

    private fun loc(): Loc4 = loc

    /**
     * Breaks the machine occupying [slot] and every machine above it.
     */
    fun breakMachinesUpwardsFromSlot(slot: Int) {
        val lvl = level ?: return
        if (breaking) return
        breaking = true
        try {
            val start = machines.entries.lastOrNull { it.key <= slot }?.takeIf { topSlotForMachine(it.key, it.value) >= slot }?.key ?: slot
            machines.tailMap(start).toList().forEach { (s, loc) ->
                val part = lvl.getBlockEntity(loc.pos) as? ICyberMachinePart
                val machine = CyberMachineRegistry.getMachine(part?.machineName)
                if (machine != null) machine.breakMachine(lvl, loc.pos)
                else MultiblockGuard.run { lvl.removeBlock(loc.pos, false) }
                machines.remove(s)
            }
            sync()
        } finally {
            breaking = false
        }
    }

    private fun onBaseBroken(level: Level) {
        if (level.isClientSide || MultiblockGuard.breaking || !multiblock.isFormed) return
        if (!multiblock.isController) {
            level.removeBlock(multiblock.controllerPos, false)
            return
        }
        breakMachinesUpwardsFromSlot(0)
        MultiblockGuard.run { baseLocations(size, blockPos).filter { it != blockPos }.forEach { level.removeBlock(it, false) } }
        val center = blockPos.offset(size / 2, 0, size / 2)
        Containers.dropItemStack(level, center.x.toDouble(), center.y.toDouble(), center.z.toDouble(), BaseSeedItem.createStack(1, size))
        for (i in 0 until inventory.size()) {
            val stack = inventory.get(i).toMinecraft()
            if (!stack.isEmpty) Containers.dropItemStack(level, center.x.toDouble(), center.y.toDouble(), center.z.toDouble(), stack.copy())
        }
    }

    override fun onUse(player: Player): InteractionResult {
        val controller = controller() ?: return InteractionResult.PASS
        if (player.isShiftKeyDown) return InteractionResult.PASS
        if (isServer) player.openMenu(controller, controller.blockPos)
        return InteractionResult.SUCCESS
    }

    override fun getDisplayName(): Component = Component.translatable("gui.femtocraft.cyber_base", size, size)

    override fun createMenu(containerId: Int, inventory: Inventory, player: Player): AbstractContainerMenu =
        CyberBaseMenu(containerId, inventory, this)

    fun openMachineSelection(player: Player) {
        player.openMenu(SimpleMenuProvider({ id, inv, _ -> MachineSelectionMenu(id, inv, this) }, Component.translatable("gui.femtocraft.machine_selection")), blockPos)
    }

    data class MachineMapping(val startingSlot: Int, val controllerLoc: Loc4) {
        companion object {
            val CODEC: Codec<MachineMapping> = RecordCodecBuilder.create { i ->
                i.group(
                    Codec.INT.fieldOf("startingSlot").forGetter(MachineMapping::startingSlot),
                    Loc4.CODEC.fieldOf("controllerLoc").forGetter(MachineMapping::controllerLoc),
                ).apply(i, ::MachineMapping)
            }
            val LIST_CODEC: Codec<List<MachineMapping>> = CODEC.listOf()
        }
    }

    companion object {
        const val MACHINES_KEY = "Machines"
        const val COMPOUND_KEY = "CyberBase"
        const val SIZE_KEY = "Size"
        const val MAX_INVENTORY_SIZE = 9 + 16
        val BASE_HEIGHT = mapOf(1 to 1, 2 to 1, 3 to 2)
        val SLOT_HEIGHT = mapOf(1 to 4, 2 to 6, 3 to 10)

        fun cube(origin: BlockPos, sx: Int, sy: Int, sz: Int): List<BlockPos> =
            (0 until sx).flatMap { x -> (0 until sy).flatMap { y -> (0 until sz).map { z -> origin.offset(x, y, z) } } }

        fun baseLocations(size: Int, origin: BlockPos): List<BlockPos> = cube(origin, size, BASE_HEIGHT.getValue(size), size)

        fun slotLocations(size: Int, origin: BlockPos): List<BlockPos> =
            cube(origin.above(BASE_HEIGHT.getValue(size)), size, SLOT_HEIGHT.getValue(size), size)

        fun isPlaceable(level: Level, pos: BlockPos) = level.getBlockState(pos).canBeReplaced()

        /**
         * Places a base of [size] with its corner at [origin] and forms it. 1.7.10 `ItemBaseSeed.onItemUse`.
         *
         * @return False if the base footprint, or the first layer of machine space, is not free.
         */
        fun place(level: Level, size: Int, origin: BlockPos): Boolean {
            val locs = baseLocations(size, origin)
            if (!locs.all { isPlaceable(level, it) }) return false
            val firstSlotY = origin.y + BASE_HEIGHT.getValue(size)
            if (!slotLocations(size, origin).filter { it.y == firstSlotY }.all { isPlaceable(level, it) }) return false
            locs.forEach { level.setBlockAndUpdate(it, FemtoBlocks.CYBER_BASE.get().defaultBlockState()) }
            locs.forEach { loc ->
                val be = level.getBlockEntity(loc) as? CyberBaseBlockEntity ?: return@forEach
                be.setSize(size)
                be.multiblock.form(origin)
            }
            return true
        }
    }
}

/**
 * Plants a cyber base; sneak-use cycles its size (1x1, 2x2, 3x3). Port of 1.7.10 `ItemBaseSeed`.
 */
class BaseSeedItem(properties: Properties) : Item(properties) {
    fun getSize(stack: ItemStack): Int = stack.get(FemtoComponents.BASE_SIZE.get()) ?: 1

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResult {
        if (!player.isShiftKeyDown) return InteractionResult.PASS
        val stack = player.getItemInHand(hand)
        stack.set(FemtoComponents.BASE_SIZE.get(), getSize(stack) % 3 + 1)
        return InteractionResult.SUCCESS
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        val player = context.player
        if (player != null && player.isShiftKeyDown) return InteractionResult.PASS
        val level = context.level
        val stack = context.itemInHand
        val size = getSize(stack)
        var origin = if (level.getBlockState(context.clickedPos).canBeReplaced()) context.clickedPos else context.clickedPos.relative(context.clickedFace)
        if (size == 3) origin = origin.offset(-1, 0, -1)
        if (level.isClientSide) return InteractionResult.SUCCESS
        if (!CyberBaseBlockEntity.place(level, size, origin)) return InteractionResult.FAIL
        if (player?.abilities?.instabuild != true) stack.shrink(1)
        return InteractionResult.SUCCESS
    }

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, display: TooltipDisplay, builder: Consumer<Component>, flag: TooltipFlag) {
        builder.accept(Component.translatable("tooltip.femtocraft.base_seed.size." + getSize(stack)))
    }

    companion object {
        fun createStack(count: Int, size: Int): ItemStack =
            ItemStack(FemtoItems.BASE_SEED.get(), count).also { it.set(FemtoComponents.BASE_SIZE.get(), size.coerceIn(1, 3)) }
    }
}

/**
 * Port of 1.7.10 `ContainerCyberBase`: 9 input slots, the buffer grid, tanks shown by the screen. Button 0 opens the
 * machine selection.
 */
class CyberBaseMenu(containerId: Int, inventory: Inventory, val base: CyberBaseBlockEntity?) :
    FemtoMenu(FemtoMenus.CYBER_BASE.get(), containerId, inventory, base) {
    constructor(containerId: Int, inventory: Inventory, buf: RegistryFriendlyByteBuf) :
        this(containerId, inventory, readBlockEntity<CyberBaseBlockEntity>(inventory, buf))

    init {
        base?.let { b ->
            for (i in 0 until 9) addStorageSlot(b.inventory, i, 89 + 18 * (i % 3), 37 + 18 * (i / 3))
            val buffer = b.size + 1
            for (i in 0 until buffer * buffer) addStorageSlot(b.inventory, i + 9, 8 + 18 * (i % buffer), 19 + 18 * (i / buffer))
        }
        addPlayerInventory(8, 95)
    }

    override fun clickMenuButton(player: Player, id: Int): Boolean {
        if (id != BUTTON_BUILD) return false
        base?.openMachineSelection(player)
        return true
    }

    companion object {
        const val BUTTON_BUILD = 0
    }
}

/**
 * Lists machines that fit the base; button id = index into [choices] builds it. Port of 1.7.10
 * `ContainerMachineSelection` + `MessageBuildMachine`, with vanilla menu button clicks instead of a custom packet.
 */
class MachineSelectionMenu(containerId: Int, inventory: Inventory, val base: CyberBaseBlockEntity?) :
    FemtoMenu(FemtoMenus.MACHINE_SELECTION.get(), containerId, inventory, base) {
    constructor(containerId: Int, inventory: Inventory, buf: RegistryFriendlyByteBuf) :
        this(containerId, inventory, readBlockEntity<CyberBaseBlockEntity>(inventory, buf))

    var remainingSlots = 0
        private set

    init {
        base?.let { b -> trackInt({ b.remainingSlots() }, { remainingSlots = it }) }
    }

    val choices: List<CyberMachine>
        get() = base?.let { CyberMachineRegistry.getMachinesThatFitIn(it.size, if (it.isServer) it.remainingSlots() else remainingSlots) }.orEmpty()

    override fun clickMenuButton(player: Player, id: Int): Boolean {
        val machine = choices.getOrNull(id) ?: return false
        return base?.buildMachine(machine.name) ?: false
    }
}
