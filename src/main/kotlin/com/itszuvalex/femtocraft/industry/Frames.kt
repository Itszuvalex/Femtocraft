package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.FemtoRegistries
import com.itszuvalex.femtocraft.core.FemtoBlockEntity
import com.itszuvalex.femtocraft.core.FemtoEntityBlock
import com.itszuvalex.itszulib.api.adapters.IItemStack
import com.itszuvalex.itszulib.api.adapters.ILevel
import com.itszuvalex.itszulib.api.multiblock.BlockPatternStatic
import com.itszuvalex.itszulib.api.multiblock.MultiblockStatic
import com.itszuvalex.itszulib.api.storage.DynamicIItemStorage
import com.itszuvalex.itszulib.api.storage.IItemStorage
import com.itszuvalex.itszulib.api.storage.ItemStorageArray
import com.itszuvalex.itszulib.api.utility.NBTSerializationScope
import com.itszuvalex.itszulib.core.frag.FragMenu
import com.itszuvalex.itszulib.core.frag.FragMultiBlockInfo
import com.itszuvalex.itszulib.core.frag.FragMultiblockState
import com.itszuvalex.itszulib.core.frag.FragMultiblockTickable
import com.itszuvalex.itszulib.core.frag.InternalBlockEntityFragment
import com.itszuvalex.itszulib.core.frag.addTickableFragment
import com.itszuvalex.itszulib.menu.MenuCore
import com.itszuvalex.itszulib.util.StorageUtils
import com.mojang.serialization.Codec
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponentType
import net.minecraft.network.chat.Component
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.neoforged.neoforge.registries.DeferredHolder
import java.util.function.Consumer

/**
 * A multiblock built from frames. Port of v3's `IFrameMultiblock`/`RectangularSimpleFrameMultiblock`: a box of
 * [size] blocks of [block], with its controller at the lowest corner, built after the frame receives [required].
 */
class FrameMultiblock(
    val id: String,
    val frameTypes: Set<String>,
    val size: Triple<Int, Int, Int>,
    private val requiredItems: () -> List<ItemStack>,
    private val blockSupplier: () -> Block,
) {
    val displayName: Component get() = Component.translatable("multiblock.femtocraft.$id")

    val numFrames: Int get() = size.first * size.second * size.third

    fun required(): List<ItemStack> = requiredItems().map { it.copy() }

    val block: Block get() = blockSupplier()

    fun takenLocations(controller: BlockPos): List<BlockPos> {
        val (x, y, z) = size
        return (0 until x).flatMap { dx -> (0 until y).flatMap { dy -> (0 until z).map { dz -> controller.offset(dx, dy, dz) } } }
    }

    fun canPlaceAt(level: Level, controller: BlockPos): Boolean = takenLocations(controller).all { level.getBlockState(it).canBeReplaced() }

    /**
     * Replaces the frames with [block] and forms the multiblock (v3's `formMultiblockWithLocsAtLoc`).
     */
    fun formAt(level: Level, controller: BlockPos): Boolean = MultiblockBreak.suppress {
        takenLocations(controller).forEach { level.setBlockAndUpdate(it, block.defaultBlockState()) }
        val pattern = BlockPatternStatic(takenLocations(controller).associate { it.subtract(controller) to block })
        MultiblockStatic(pattern).form(ILevel.of(level), controller, pattern)
    }
}

/**
 * The frame multiblocks. Port of v3's `FrameMultiblockRegistry`.
 */
object FrameMultiblocks {
    const val BASIC = "Basic"

    private val all = ArrayList<FrameMultiblock>()

    @JvmField
    val GERMINATION_CHAMBER = register(FrameMultiblock("germination_chamber", setOf(BASIC), Triple(2, 3, 2),
        { listOf(ItemStack(IndustryContent.RIFTIRON_INGOT_ACTIVATED.get(), 10)) }, { IndustryContent.GERMINATION_CHAMBER.get() }))

    @JvmField
    val CRYSTAL_FOCUSING_CHAMBER = register(FrameMultiblock("crystal_focusing_chamber", setOf(BASIC), Triple(2, 2, 2),
        { listOf() }, { IndustryContent.CRYSTAL_FOCUSING_CHAMBER.get() }))

    fun register(multi: FrameMultiblock): FrameMultiblock = multi.also { all += it }

    fun get(id: String?): FrameMultiblock? = all.firstOrNull { it.id == id }

    fun forFrameType(type: String): List<FrameMultiblock> = all.filter { type in it.frameTypes }
}

/**
 * Guards multiblock teardown, so the blocks it removes do not each start their own (v3's `TileFrame.shouldFullyRemove`
 * and `MultiBlockComponent` flags).
 */
object MultiblockBreak {
    private var active = false

    val isActive: Boolean get() = active

    fun <T> suppress(action: () -> T): T {
        val was = active
        active = true
        try {
            return action()
        } finally {
            active = was
        }
    }
}

/**
 * Tears the whole multiblock down when any of its blocks is removed (server side): every other block becomes air and
 * the controller's [drops] are dropped. Port of v3's `onBlockBreak` handlers on frames and frame multiblocks.
 */
class FragMultiblockTeardown(
    private val info: FragMultiBlockInfo,
    private val locations: (controller: BlockPos) -> List<BlockPos>,
    private val drops: (level: Level, controller: BlockPos) -> List<ItemStack>,
) : InternalBlockEntityFragment() {
    override fun name(): String = "Teardown"

    override fun onRemove(level: ILevel, pos: BlockPos, blockStatePrev: BlockState) {
        if (MultiblockBreak.isActive) return
        val controller = info.info.controller ?: return
        val lvl = level.toMinecraft()
        MultiblockBreak.suppress {
            val toDrop = drops(lvl, controller)
            locations(controller).filter { it != pos && lvl.isLoaded(it) }.forEach { lvl.setBlockAndUpdate(it, Blocks.AIR.defaultBlockState()) }
            toDrop.filter { !it.isEmpty }.forEach { Block.popResource(lvl, controller, it) }
        }
    }
}

/**
 * Multiblock state of a frame structure, held by its controller. Port of v3's `TileFrameState`: a 9-slot inventory
 * for the required resources, the selected multiblock, and build progress.
 */
class FrameState(onChanged: Runnable) : net.neoforged.neoforge.common.util.ValueIOSerializable {
    @JvmField
    val storage = ItemStorageArray(9, onChanged)

    var multiblock: String = ""
    var building = false
    var progress = 0
    var ticks = 0

    override fun serialize(output: ValueOutput) {
        storage.serialize(output.child("Storage"))
        output.putString("Multiblock", multiblock)
        output.putBoolean("Building", building)
        output.putInt("BuildProgress", progress)
    }

    override fun deserialize(input: ValueInput) {
        input.child("Storage").ifPresent(storage::deserialize)
        multiblock = input.getStringOr("Multiblock", "")
        building = input.getBooleanOr("Building", false)
        progress = input.getIntOr("BuildProgress", 0)
    }

    companion object {
        const val TICKS_TO_CHECK = 40
        const val BUILD_TIME = 10 * 20
    }
}

class FrameBlock(properties: BlockBehaviour.Properties) : FemtoEntityBlock<FrameBlockEntity>(properties, { IndustryContent.FRAME_BE.get() })

/**
 * One block of a frame structure. Port of v3's `TileFrame`: the controller checks its inventory for the selected
 * multiblock's resources every [FrameState.TICKS_TO_CHECK] ticks, then builds for [FrameState.BUILD_TIME] ticks and
 * replaces the frames with the multiblock. Breaking any frame removes the structure and drops the frames (keeping their
 * selection), the inventory, and the resources if building had started.
 */
class FrameBlockEntity(pos: BlockPos, state: BlockState) : FemtoBlockEntity(IndustryContent.FRAME_BE.get(), pos, state) {
    @JvmField
    val info = FragMultiBlockInfo()

    @JvmField
    val frameState: FragMultiblockState<FrameState> = FragMultiblockState(info, { FrameState { markDirty() } }, { (it as? FrameBlockEntity)?.frameState })

    @JvmField
    val storage: IItemStorage = DynamicIItemStorage { frameState.get()?.storage ?: IItemStorage.Empty }

    init {
        fragList.addFragment(info)
        fragList.addInternalFragment(frameState)
        fragList.addFragment(FragMenu(Component.translatable("block.femtocraft.frame"), { id, inv, _ -> FrameMenu(id, inv, this) }, info))
        fragList.addInternalFragment(FragMultiblockTeardown(info, ::locations, ::teardownDrops))
        fragList.addTickableFragment(object : FragMultiblockTickable(info.info) {
            override fun name(): String = "FrameBuild"
            override fun serverControllerTick(level: ILevel, pos: BlockPos) = buildTick(level.toMinecraft(), pos)
        })
    }

    fun multiblock(): FrameMultiblock? = FrameMultiblocks.get(frameState.get()?.multiblock)

    private fun locations(controller: BlockPos): List<BlockPos> = controllerEntity(controller)?.multiblock()?.takenLocations(controller) ?: listOf()

    private fun controllerEntity(controller: BlockPos): FrameBlockEntity? =
        if (controller == blockPos) this else level?.getBlockEntity(controller) as? FrameBlockEntity

    private fun teardownDrops(level: Level, controller: BlockPos): List<ItemStack> {
        val ctrl = controllerEntity(controller) ?: return listOf()
        val s = ctrl.frameState.get() ?: return listOf()
        val multi = ctrl.multiblock() ?: return listOf()
        val drops = ArrayList<ItemStack>()
        repeat(multi.numFrames) { drops += FrameItem.withSelection(ItemStack(IndustryContent.FRAME_ITEM.get()), multi.id) }
        if (s.building) drops += multi.required()
        for (i in 0 until s.storage.size()) drops += s.storage.get(i).toMinecraft().copy()
        return drops
    }

    private fun buildTick(level: Level, pos: BlockPos) {
        val s = frameState.get() ?: return
        val multi = FrameMultiblocks.get(s.multiblock) ?: return
        if (!s.building) {
            s.ticks = Math.floorMod(s.ticks - 1, FrameState.TICKS_TO_CHECK)
            if (s.ticks == 0 && StorageUtils.removeItemsFromStorage(s.storage, multi.required().map(IItemStack::of))) {
                // v3 dropped whatever else was in the frame once building started.
                for (i in 0 until s.storage.size()) {
                    val left = s.storage.get(i).toMinecraft()
                    if (!left.isEmpty) Block.popResource(level, pos, left.copy())
                    s.storage.setSlot(i, IItemStack.Empty)
                }
                s.building = true
                markDirtyAndSync()
            }
            return
        }
        s.progress++
        markDirty()
        if (s.progress >= FrameState.BUILD_TIME) multi.formAt(level, pos)
    }
}

/**
 * The frame item: places a frame structure for its selected multiblock (using one frame per block), and opens the
 * multiblock selection when used while sneaking. Port of v3's `ItemFrame`; the selection is the
 * `femtocraft:frame_selection` data component.
 */
class FrameItem(properties: Properties) : Item(properties) {
    override fun use(level: Level, player: Player, hand: net.minecraft.world.InteractionHand): InteractionResult {
        if (!player.isShiftKeyDown) return super.use(level, player, hand)
        if (!level.isClientSide) openSelection(player)
        return InteractionResult.SUCCESS
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        val player = context.player
        val level = context.level
        if (player != null && player.isShiftKeyDown) {
            if (!level.isClientSide) openSelection(player)
            return InteractionResult.SUCCESS
        }
        val stack = context.itemInHand
        val multi = FrameMultiblocks.get(selection(stack)) ?: return InteractionResult.PASS
        val clicked = context.clickedPos
        val controller = if (level.getBlockState(clicked).canBeReplaced(BlockPlaceContext(context))) clicked else clicked.relative(context.clickedFace)
        if (!multi.canPlaceAt(level, controller)) return InteractionResult.PASS
        val creative = player?.abilities?.instabuild == true
        if (!creative && stack.count < multi.numFrames) return InteractionResult.PASS
        if (level.isClientSide) return InteractionResult.SUCCESS
        if (!creative) stack.shrink(multi.numFrames)
        place(level, controller, multi)
        level.playSound(null, controller, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1f, 0.2f)
        return InteractionResult.SUCCESS
    }

    private fun openSelection(player: Player) {
        player.openMenu(SimpleMenuProvider({ id, inv, _ -> FrameSelectionMenu(id, inv) }, Component.translatable("gui.femtocraft.multiblock_selection")))
    }

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, display: TooltipDisplay, builder: Consumer<Component>, flag: TooltipFlag) {
        builder.accept(Component.translatable("tooltip.femtocraft.frame.type", FrameMultiblocks.BASIC).withStyle(ChatFormatting.GRAY))
        val multi = FrameMultiblocks.get(selection(stack))
        builder.accept(Component.translatable("tooltip.femtocraft.frame.selected", multi?.displayName ?: Component.translatable("tooltip.femtocraft.none")).withStyle(ChatFormatting.GRAY))
    }

    companion object {
        @JvmField
        val SELECTION: DeferredHolder<DataComponentType<*>, DataComponentType<String>> =
            FemtoRegistries.DATA_COMPONENTS.registerComponentType("frame_selection") { b -> b.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8) }

        fun selection(stack: ItemStack): String? = stack.get(SELECTION.get())

        fun withSelection(stack: ItemStack, id: String): ItemStack = stack.also { it.set(SELECTION.get(), id) }

        /**
         * Places the frame blocks of [multi] with their controller at [controller] and forms them.
         */
        fun place(level: Level, controller: BlockPos, multi: FrameMultiblock) {
            val locations = multi.takenLocations(controller)
            locations.forEach { level.setBlockAndUpdate(it, IndustryContent.FRAME.get().defaultBlockState()) }
            for (loc in locations) {
                val frame = level.getBlockEntity(loc) as? FrameBlockEntity ?: continue
                frame.info.info.form(loc, controller)
            }
            (level.getBlockEntity(controller) as? FrameBlockEntity)?.frameState?.get()?.let { it.multiblock = multi.id }
        }
    }
}

/**
 * Picks the multiblock a held frame builds; button `i` selects the `i`-th multiblock for the frame type. Port of v3's
 * `GuiMultiblockSelection` + `MessageMultiblockSelection`, on vanilla `clickMenuButton`.
 */
class FrameSelectionMenu(containerId: Int, inventory: Inventory) : MenuCore(IndustryContent.FRAME_SELECTION_MENU.get(), containerId, inventory.player) {
    val options: List<FrameMultiblock> = FrameMultiblocks.forFrameType(FrameMultiblocks.BASIC)

    private fun heldFrame(player: Player): ItemStack =
        listOf(player.mainHandItem, player.offhandItem).firstOrNull { it.item is FrameItem } ?: ItemStack.EMPTY

    override fun clickMenuButton(player: Player, id: Int): Boolean {
        val multi = options.getOrNull(id) ?: return false
        val stack = heldFrame(player)
        if (stack.isEmpty) return false
        FrameItem.withSelection(stack, multi.id)
        return true
    }

    override fun stillValid(player: Player): Boolean = !heldFrame(player).isEmpty

    override fun quickMoveStack(player: Player, index: Int): ItemStack = ItemStack.EMPTY
}

/**
 * A frame structure's resource inventory and build progress. Port of v3's `ContainerFrame` and
 * `ContainerFrameConstructing` (one menu: the slots stay usable while building).
 */
class FrameMenu(containerId: Int, inventory: Inventory, be: FrameBlockEntity?) :
    com.itszuvalex.femtocraft.core.FemtoMenu<FrameBlockEntity>(IndustryContent.FRAME_MENU.get(), containerId, inventory, be) {
    var progress = 0
    var building = false

    init {
        addStorageSlots(be?.storage ?: IItemStorage.Empty, 62, 17, columns = 3, count = if (be == null) 0 else 9)
        addPlayerInventorySlots(inventory)
        addSync(com.itszuvalex.itszulib.menu.MenuSyncs.int({ be?.frameState?.get()?.progress ?: 0 }, { progress = it }))
        addSync(com.itszuvalex.itszulib.menu.MenuSyncs.boolean({ be?.frameState?.get()?.building ?: false }, { building = it }))
    }
}
