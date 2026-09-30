package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.FemtoBlockEntities
import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.FemtoComponents
import com.itszuvalex.femtocraft.FemtoItems
import com.itszuvalex.femtocraft.FemtoMenus
import com.itszuvalex.femtocraft.core.FemtoBlockEntity
import com.itszuvalex.femtocraft.core.FemtoMenu
import com.itszuvalex.femtocraft.core.FragData
import com.itszuvalex.femtocraft.core.FragMultiBlock
import com.itszuvalex.femtocraft.logistics.IndexedItemStorage
import com.itszuvalex.itszulib.api.adapters.IItemStack
import com.itszuvalex.itszulib.api.adapters.ILevel
import com.itszuvalex.itszulib.api.utility.NBTSerializationScope
import com.itszuvalex.itszulib.core.frag.InternalBlockEntityFragment
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
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
import java.util.function.Consumer

/**
 * One block of a frame: frames outline a machine, collect its resources in the controller's 9 slots, then build it
 * over [TOTAL_BUILD_TIME] ticks. Port of 1.7.10 `TileFrame` (the per-edge render marks were rendering-only and are
 * not ported).
 */
class FrameBlockEntity(pos: BlockPos, state: BlockState) :
    FemtoBlockEntity(FemtoBlockEntities.FRAME.get(), pos, state), IMultiblockPart {
    override val multiblock = FragMultiBlock { blockPos }
    val inventory = object : IndexedItemStorage(9, Runnable { onInventoryChanged() }) {
        override fun canInsert(index: Int, stack: IItemStack): Boolean = !isBuilding
    }
    var multiBlockName: String? = null
    var progress = 0
    var isBuilding = false
    private var modifyingInventory = false

    init {
        fragList.addInternalFragment(multiblock)
        fragList.addInternalFragment(FragData("Frame", FragData.LEVEL_AND_DESCRIPTION, { _, out ->
            out.putString(MULTIBLOCK_KEY, multiBlockName ?: "")
            out.putInt(PROGRESS_KEY, progress)
            out.putBoolean(BUILDING_KEY, isBuilding)
        }, { _, input ->
            multiBlockName = input.getStringOr(MULTIBLOCK_KEY, "").ifEmpty { null }
            progress = input.getIntOr(PROGRESS_KEY, 0)
            isBuilding = input.getBooleanOr(BUILDING_KEY, false)
        }))
        fragList.addInternalFragment(FragData("Inventory", FragData.LEVEL, { _, out -> inventory.serialize(out) }, { _, input ->
            modifyingInventory = true
            inventory.deserialize(input)
            modifyingInventory = false
        }))
        fragList.addInternalFragment(object : InternalBlockEntityFragment() {
            override fun name(): String = "BreakFrame"
            override fun onRemove(level: ILevel, pos: BlockPos, blockStatePrev: BlockState) = onFrameBroken(level.toMinecraft())
        })
    }

    fun multi(): IFrameMultiblock? = FrameMultiblockRegistry.getMultiblock(multiBlockName)

    fun controller(): FrameBlockEntity? = if (multiblock.isController) this else multiblock.controller(level)

    override fun serverTick() {
        if (!multiblock.isController || !isBuilding) return
        progress++
        if (progress >= TOTAL_BUILD_TIME) {
            multi()?.formAtLocation(level!!, blockPos)
        } else if (progress % 20 == 0) {
            sync()
        } else {
            setChanged()
        }
    }

    private fun onInventoryChanged() {
        setChanged()
        if (!modifyingInventory && isServer) checkForRequiredItems()
    }

    /**
     * When the inventory holds every required resource, consumes them, spits out anything else and starts building.
     */
    fun checkForRequiredItems() {
        val multi = multi() ?: return
        val required = multi.getRequiredResources()
        val hasAll = required.all { req ->
            (0 until inventory.size()).map { inventory.get(it).toMinecraft() }
                .filter { ItemStack.isSameItemSameComponents(it, req) }.sumOf { it.count } >= req.count
        }
        if (!hasAll) return
        modifyingInventory = true
        try {
            required.forEach { req ->
                var needed = req.count
                for (slot in 0 until inventory.size()) {
                    if (needed <= 0) break
                    val stack = inventory.get(slot).toMinecraft()
                    if (!ItemStack.isSameItemSameComponents(stack, req)) continue
                    val amt = minOf(needed, stack.count)
                    needed -= amt
                    val left = stack.copyWithCount(stack.count - amt)
                    inventory.setSlot(slot, IItemStack.of(left))
                }
            }
            for (slot in 0 until inventory.size()) {
                val stack = inventory.get(slot).toMinecraft()
                if (!stack.isEmpty) Containers.dropItemStack(level!!, blockPos.x.toDouble(), blockPos.y + 1.0, blockPos.z.toDouble(), stack.copy())
                inventory.setSlot(slot, IItemStack.Empty)
            }
        } finally {
            modifyingInventory = false
        }
        isBuilding = true
        sync()
    }

    private fun onFrameBroken(level: Level) {
        if (level.isClientSide || MultiblockGuard.breaking || !multiblock.isFormed) return
        if (!multiblock.isController) {
            level.removeBlock(multiblock.controllerPos, false)
            return
        }
        val multi = multi()
        MultiblockGuard.run {
            val locs = multi?.getTakenLocations(blockPos) ?: listOf(blockPos)
            locs.forEach { loc ->
                if (loc != blockPos) level.removeBlock(loc, false)
                val frame = ItemStack(FemtoItems.FRAME.get())
                multiBlockName?.let { frame.set(FemtoComponents.FRAME_SELECTION.get(), it) }
                Containers.dropItemStack(level, loc.x.toDouble(), loc.y.toDouble(), loc.z.toDouble(), frame)
            }
            if (isBuilding) multi?.getRequiredResources()?.forEach {
                Containers.dropItemStack(level, blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble(), it.copy())
            }
            for (slot in 0 until inventory.size()) {
                val stack = inventory.get(slot).toMinecraft()
                if (!stack.isEmpty) Containers.dropItemStack(level, blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble(), stack.copy())
            }
        }
    }

    override fun onUse(player: Player): InteractionResult {
        val controller = controller() ?: return InteractionResult.PASS
        if (!isServer) return InteractionResult.SUCCESS
        val title = Component.literal(controller.multiBlockName ?: "Undefined")
        val provider: MenuProvider = if (controller.isBuilding) {
            SimpleMenuProvider({ id, inv, _ -> FrameConstructingMenu(id, inv, controller) }, title)
        } else {
            SimpleMenuProvider({ id, inv, _ -> FrameMenu(id, inv, controller) }, title)
        }
        player.openMenu(provider, controller.blockPos)
        return InteractionResult.SUCCESS
    }

    companion object {
        const val BUILDING_KEY = "Building"
        const val MULTIBLOCK_KEY = "Multiblock"
        const val PROGRESS_KEY = "BuildProgress"
        const val TOTAL_BUILD_TIME = 10 * 20
    }
}

/**
 * 9 resource slots. Port of 1.7.10 `ContainerFrame`.
 */
class FrameMenu(containerId: Int, inventory: Inventory, val frame: FrameBlockEntity?) :
    FemtoMenu(FemtoMenus.FRAME.get(), containerId, inventory, frame) {
    constructor(containerId: Int, inventory: Inventory, buf: RegistryFriendlyByteBuf) :
        this(containerId, inventory, readBlockEntity<FrameBlockEntity>(inventory, buf))

    init {
        frame?.let { f -> for (i in 0 until 9) addStorageSlot(f.inventory, i, 8 + 18 * i, 62) }
        addPlayerInventory()
    }

    override fun stillValid(player: Player): Boolean = super.stillValid(player) && frame?.isBuilding != true
}

/**
 * Build progress only. Port of 1.7.10 `ContainerFrameConstructing`.
 */
class FrameConstructingMenu(containerId: Int, inventory: Inventory, val frame: FrameBlockEntity?) :
    FemtoMenu(FemtoMenus.FRAME_CONSTRUCTING.get(), containerId, inventory, frame) {
    constructor(containerId: Int, inventory: Inventory, buf: RegistryFriendlyByteBuf) :
        this(containerId, inventory, readBlockEntity<FrameBlockEntity>(inventory, buf))

    var progress = 0

    init {
        frame?.let { f -> trackInt({ f.progress }, { progress = it }) }
    }
}

/**
 * Places frames for the selected multiblock; sneak-use picks the multiblock. Port of 1.7.10 `ItemFrame`.
 */
class FrameItem(properties: Properties) : Item(properties) {
    fun getFrameType(stack: ItemStack): String = "Basic"

    fun getSelectedMultiblock(stack: ItemStack): String? = stack.get(FemtoComponents.FRAME_SELECTION.get())

    fun setSelectedMultiblock(stack: ItemStack, name: String?) {
        if (name == null) stack.remove(FemtoComponents.FRAME_SELECTION.get()) else stack.set(FemtoComponents.FRAME_SELECTION.get(), name)
    }

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResult {
        if (!player.isShiftKeyDown) return InteractionResult.PASS
        openSelection(player, hand)
        return InteractionResult.SUCCESS
    }

    private fun openSelection(player: Player, hand: InteractionHand) {
        if (player.level().isClientSide) return
        player.openMenu(
            SimpleMenuProvider({ id, inv, _ -> MultiblockSelectionMenu(id, inv, hand) }, Component.translatable("gui.femtocraft.multiblock_selection")),
        ) { buf -> buf.writeEnum(hand) }
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        val player = context.player
        val stack = context.itemInHand
        if (player != null && player.isShiftKeyDown) {
            openSelection(player, context.hand)
            return InteractionResult.SUCCESS
        }
        val multi = FrameMultiblockRegistry.getMultiblock(getSelectedMultiblock(stack)) ?: return InteractionResult.PASS
        val level = context.level
        val controller = placementPos(context)
        if (!multi.canPlaceAtLocation(level, controller)) return InteractionResult.FAIL
        val creative = player?.abilities?.instabuild == true
        if (!creative && stack.count < multi.numFrames) return InteractionResult.FAIL
        if (level.isClientSide) return InteractionResult.SUCCESS
        if (!creative) stack.shrink(multi.numFrames)
        placeFrames(level, multi, controller)
        level.playSound(null, controller, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1f, .2f)
        return InteractionResult.SUCCESS
    }

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, display: TooltipDisplay, builder: Consumer<Component>, flag: TooltipFlag) {
        builder.accept(Component.translatable("tooltip.femtocraft.frame.type", getFrameType(stack)))
        builder.accept(Component.translatable("tooltip.femtocraft.frame.selected", getSelectedMultiblock(stack) ?: "none"))
    }

    companion object {
        /**
         * The clicked block if it is replaceable, else the block against the clicked face (1.7.10 logic).
         */
        fun placementPos(context: UseOnContext): BlockPos {
            val pos = context.clickedPos
            return if (context.level.getBlockState(pos).canBeReplaced()) pos else pos.relative(context.clickedFace)
        }

        /**
         * Puts frame blocks over the multiblock's footprint, all controlled from [controller].
         */
        fun placeFrames(level: Level, multi: IFrameMultiblock, controller: BlockPos) {
            val locations = multi.getTakenLocations(controller)
            locations.forEach { level.setBlockAndUpdate(it, FemtoBlocks.FRAME.get().defaultBlockState()) }
            locations.forEach { loc ->
                val frame = level.getBlockEntity(loc) as? FrameBlockEntity ?: return@forEach
                frame.multiBlockName = multi.name
                frame.multiblock.form(controller)
            }
        }
    }
}

/**
 * A formed multiblock in item form. Port of 1.7.10 `ItemMultiblock`.
 */
class MultiblockItem(properties: Properties) : Item(properties) {
    fun getMultiblock(stack: ItemStack): String? = stack.get(FemtoComponents.MULTIBLOCK.get())

    override fun getName(stack: ItemStack): Component =
        getMultiblock(stack)?.let { Component.literal(it) } ?: Component.translatable("item.femtocraft.multiblock.invalid")

    override fun useOn(context: UseOnContext): InteractionResult {
        val multi = FrameMultiblockRegistry.getMultiblock(getMultiblock(context.itemInHand)) ?: return InteractionResult.PASS
        val level = context.level
        val pos = FrameItem.placementPos(context)
        if (!multi.canPlaceAtLocation(level, pos)) return InteractionResult.FAIL
        if (level.isClientSide) return InteractionResult.SUCCESS
        val stack = context.itemInHand
        multi.formAtLocationFromItem(level, pos, stack)
        if (context.player?.abilities?.instabuild != true) stack.shrink(1)
        level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1f, .2f)
        return InteractionResult.SUCCESS
    }
}

/**
 * Picks the frame item's multiblock. Button id = index into [choices]; [CLEAR] clears it. Port of 1.7.10
 * `GuiMultiblockSelection` + `MessageMultiblockSelection`, using vanilla menu button clicks instead of a custom packet.
 */
class MultiblockSelectionMenu(containerId: Int, private val inventory: Inventory, val hand: InteractionHand) :
    AbstractContainerMenu(FemtoMenus.MULTIBLOCK_SELECTION.get(), containerId) {
    constructor(containerId: Int, inventory: Inventory, buf: RegistryFriendlyByteBuf) :
        this(containerId, inventory, buf.readEnum(InteractionHand::class.java))

    private fun frame(): ItemStack = inventory.player.getItemInHand(hand)

    val choices: List<IFrameMultiblock>
        get() = (frame().item as? FrameItem)?.let { FrameMultiblockRegistry.getMultiblocksForFrameType(it.getFrameType(frame())) }.orEmpty()

    val selected: String? get() = (frame().item as? FrameItem)?.getSelectedMultiblock(frame())

    override fun clickMenuButton(player: Player, id: Int): Boolean {
        val stack = frame()
        val item = stack.item as? FrameItem ?: return false
        when {
            id == CLEAR -> item.setSelectedMultiblock(stack, null)
            id in choices.indices -> item.setSelectedMultiblock(stack, choices[id].name)
            else -> return false
        }
        return true
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack = ItemStack.EMPTY

    override fun stillValid(player: Player): Boolean = player.getItemInHand(hand).item is FrameItem

    companion object {
        const val CLEAR = 999
    }
}
