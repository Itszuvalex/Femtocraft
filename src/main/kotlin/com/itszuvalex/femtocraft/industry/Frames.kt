package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.FemtoRegistries
import com.itszuvalex.femtocraft.logistics.FluidReservoirState
import com.itszuvalex.femtocraft.logistics.ItemVaultState
import com.itszuvalex.femtocraft.logistics.NaniteVaultState
import com.itszuvalex.femtocraft.core.FemtoBlockEntity
import com.itszuvalex.femtocraft.core.FemtoEntityBlock
import com.itszuvalex.itszulib.api.adapters.IItemStack
import com.itszuvalex.itszulib.api.adapters.ILevel
import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.itszulib.api.Modules
import com.itszuvalex.itszulib.api.multiblock.IMultiblockState
import com.itszuvalex.itszulib.api.multiblock.MultiblockBreakPolicy
import com.itszuvalex.itszulib.api.multiblock.MultiblockInstance
import com.itszuvalex.itszulib.api.multiblock.MultiblockManager
import com.itszuvalex.itszulib.api.multiblock.MultiblockRoleRef
import com.itszuvalex.itszulib.api.multiblock.MultiblockShape
import net.minecraft.resources.Identifier
import com.itszuvalex.itszulib.api.storage.DynamicIItemStorage
import com.itszuvalex.itszulib.api.storage.IItemStorage
import com.itszuvalex.itszulib.api.storage.ItemStorageArray
import com.itszuvalex.itszulib.core.frag.FragMenu
import com.itszuvalex.itszulib.core.frag.FragMultiblockPart
import com.itszuvalex.itszulib.core.frag.FragMultiblockTickable
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
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.neoforged.neoforge.registries.DeferredHolder
import java.util.function.Consumer

/**
 * A multiblock built from frames. Port of v3's `IFrameMultiblock`/`RectangularSimpleFrameMultiblock`: a box of
 * [size] blocks of [block], anchored (home member) at its lowest corner, built after the frame receives [required].
 *
 * Two ItszuLib multiblock shapes: [frameShape] for the frames while they collect resources and build (shared state
 * [FrameState]), and [shape] for the finished machine (shared state from [state]). Both are formed explicitly (by the
 * frame item and by finishing the build), and breaking any block destroys the rest; the shared state drops the
 * contents.
 */
class FrameMultiblock(
    val id: String,
    val frameTypes: Set<String>,
    val size: Triple<Int, Int, Int>,
    private val requiredItems: () -> List<ItemStack>,
    private val blockSupplier: () -> Block,
    state: (Runnable) -> IMultiblockState,
) {
    val displayName: Component get() = Component.translatable("multiblock.femtocraft.$id")

    val numFrames: Int get() = size.first * size.second * size.third

    fun required(): List<ItemStack> = requiredItems().map { it.copy() }

    val block: Block get() = blockSupplier()

    val frameShape: MultiblockShape = MultiblockShape.register(
        Identifier.fromNamespaceAndPath(Femtocraft.ID, "frame_$id"), MultiblockShape.box(size.first, size.second, size.third, FRAME_ROLE),
        MultiblockBreakPolicy.DESTROY_ALL,
    ) { FrameState(this, it) }

    val shape: MultiblockShape = MultiblockShape.register(
        Identifier.fromNamespaceAndPath(Femtocraft.ID, id), MultiblockShape.box(size.first, size.second, size.third, MACHINE_ROLE),
        MultiblockBreakPolicy.DESTROY_ALL, state,
    )

    fun takenLocations(anchor: BlockPos): List<BlockPos> = shape.positions(anchor)

    fun canPlaceAt(level: Level, anchor: BlockPos): Boolean = takenLocations(anchor).all { level.getBlockState(it).canBeReplaced() }

    /**
     * Replaces the frames anchored at [anchor] with [block] and forms the machine (v3's `formMultiblockWithLocsAtLoc`).
     * The frame structure is disbanded first, so replacing its blocks breaks nothing.
     */
    /** The machine's block state at its anchor ([home]) or elsewhere. */
    fun machineState(home: Boolean): BlockState {
        val state = block.defaultBlockState()
        return if (state.hasProperty(HOME)) state.setValue(HOME, home) else state
    }

    fun formAt(level: Level, anchor: BlockPos): Boolean {
        val lvl = ILevel.of(level)
        lvl.getIBlockEntity(anchor)?.getModule(Modules.MULTIBLOCK_MEMBER, null)?.let { MultiblockManager.SERVER.disband(lvl, anchor, it) }
        takenLocations(anchor).forEach { pos -> level.setBlockAndUpdate(pos, machineState(pos == anchor)) }
        return MultiblockManager.SERVER.form(lvl, shape, anchor) != null
    }

    companion object {
        const val FRAME_ROLE = "frame"
        const val MACHINE_ROLE = "machine"

        /**
         * On machine blocks whose home block's model draws the whole machine: true on the home block (the anchor).
         */
        @JvmField
        val HOME: net.minecraft.world.level.block.state.properties.BooleanProperty =
            net.minecraft.world.level.block.state.properties.BooleanProperty.create("home")
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
        { listOf(ItemStack(IndustryContent.RIFTIRON_INGOT_ACTIVATED.get(), 10)) }, { IndustryContent.GERMINATION_CHAMBER.get() }, ::GerminationState))

    @JvmField
    val CRYSTAL_FOCUSING_CHAMBER = register(FrameMultiblock("crystal_focusing_chamber", setOf(BASIC), Triple(2, 2, 2),
        { listOf() }, { IndustryContent.CRYSTAL_FOCUSING_CHAMBER.get() }, ::FocusingState))

    // Storage multiblocks (logistics). Costs are placeholders until the storage technologies are designed.
    @JvmField
    val ITEM_VAULT = register(FrameMultiblock("item_vault", setOf(BASIC), Triple(3, 3, 3),
        { listOf(ItemStack(IndustryContent.RIFTIRON_INGOT_ACTIVATED.get(), 16), ItemStack(IndustryContent.BASIC_CIRCUIT.get(), 4)) },
        { com.itszuvalex.femtocraft.logistics.LogisticsContent.ITEM_VAULT.get() }, ::ItemVaultState))

    @JvmField
    val FLUID_RESERVOIR = register(FrameMultiblock("fluid_reservoir", setOf(BASIC), Triple(3, 3, 3),
        { listOf(ItemStack(IndustryContent.RIFTIRON_INGOT_ACTIVATED.get(), 16), ItemStack(IndustryContent.NANO_CHANNEL.get(), 4)) },
        { com.itszuvalex.femtocraft.logistics.LogisticsContent.FLUID_RESERVOIR.get() }, ::FluidReservoirState))

    @JvmField
    val NANITE_VAULT = register(FrameMultiblock("nanite_vault", setOf(BASIC), Triple(3, 3, 3),
        { listOf(ItemStack(IndustryContent.PHASEMETAL_INGOT_ACTIVATED.get(), 8), ItemStack(IndustryContent.NANITE_BEACON.get(), 2)) },
        { com.itszuvalex.femtocraft.logistics.LogisticsContent.NANITE_VAULT.get() }, ::NaniteVaultState))

    fun register(multi: FrameMultiblock): FrameMultiblock = multi.also { all += it }

    fun get(id: String?): FrameMultiblock? = all.firstOrNull { it.id == id }

    fun all(): List<FrameMultiblock> = all

    fun forFrameType(type: String): List<FrameMultiblock> = all.filter { type in it.frameTypes }

    /**
     * Registers the shapes; call during mod construction, before any world loads.
     */
    fun init() {}
}

/**
 * Shared state of a frame structure, on its home frame. Port of v3's `TileFrameState`: a 9-slot inventory for the
 * required resources and build progress. Breaking the structure drops the frames (keeping their selection), the
 * inventory, and the resources if building had started.
 */
class FrameState(val multiblock: FrameMultiblock, onChanged: Runnable) : IMultiblockState {
    @JvmField
    val storage = ItemStorageArray(9, onChanged)

    var building = false
    var progress = 0
    var ticks = 0

    fun drops(): List<ItemStack> {
        val drops = ArrayList<ItemStack>()
        repeat(multiblock.numFrames) { drops += FrameItem.withSelection(ItemStack(IndustryContent.FRAME_ITEM.get()), multiblock.id) }
        if (building) drops += multiblock.required()
        for (i in 0 until storage.size()) drops += storage.get(i).toMinecraft().copy()
        return drops.filter { !it.isEmpty }
    }

    override fun onBreak(level: ILevel, anchor: BlockPos, brokenAt: BlockPos) =
        drops().forEach { Block.popResource(level.toMinecraft(), anchor, it) }

    override fun serialize(output: ValueOutput) {
        storage.serialize(output.child("Storage"))
        output.putBoolean("Building", building)
        output.putInt("BuildProgress", progress)
    }

    override fun deserialize(input: ValueInput) {
        input.child("Storage").ifPresent(storage::deserialize)
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
 * One block of a frame structure. Port of v3's `TileFrame`: the structure checks its inventory for the selected
 * multiblock's resources every [FrameState.TICKS_TO_CHECK] ticks, then builds for [FrameState.BUILD_TIME] ticks and
 * replaces the frames with the multiblock. Breaking any frame removes the structure (see [FrameState]).
 */
class FrameBlockEntity(pos: BlockPos, state: BlockState) : FemtoBlockEntity(IndustryContent.FRAME_BE.get(), pos, state) {
    companion object {
        /** How often a building frame re-sends its progress to clients. */
        const val SYNC_TICKS = 20
    }

    @JvmField
    val part = FragMultiblockPart(FrameMultiblocks.all().map { MultiblockRoleRef(it.frameShape, FrameMultiblock.FRAME_ROLE) }, autoForm = false)

    @JvmField
    val storage: IItemStorage = DynamicIItemStorage { frameState()?.storage ?: IItemStorage.Empty }

    init {
        fragList.addFragment(part)
        fragList.addFragment(FragMenu(Component.translatable("block.femtocraft.frame"), { id, inv, _ -> FrameMenu(id, inv, this) }, part))
        fragList.addInternalFragment(com.itszuvalex.femtocraft.core.FragData("BuildView", setOf(com.itszuvalex.itszulib.api.utility.NBTSerializationScope.DESCRIPTION), { _, o ->
            val s = frameState()
            o.putBoolean("Building", s?.building == true)
            o.putInt("Progress", s?.progress ?: 0)
        }, { _, i ->
            clientBuilding = i.getBooleanOr("Building", false)
            clientProgress = i.getIntOr("Progress", 0).toFloat()
        }))
        fragList.addTickableFragment(object : FragMultiblockTickable(part) {
            override fun name(): String = "FrameBuild"
            override fun serverStructureTick(level: ILevel, instance: MultiblockInstance) = buildTick(level.toMinecraft(), instance)
        })
    }

    /**
     * The frame structure's shared state (null while not formed, or while its home frame's chunk loads).
     */
    fun frameState(): FrameState? = part.sharedState() as? FrameState

    fun multiblock(): FrameMultiblock? = FrameMultiblocks.all().firstOrNull { it.frameShape == part.membership?.shape }

    /**
     * Client side, on the home frame: whether the structure is building and how far (in ticks, advanced between
     * syncs), for the machine preview ([com.itszuvalex.femtocraft.client.FemtoRenderers.FrameRenderer]).
     */
    var clientBuilding = false
        private set
    var clientProgress = 0f
        private set

    override fun clientTick() {
        if (clientBuilding && clientProgress < FrameState.BUILD_TIME) clientProgress++
    }

    private fun buildTick(level: Level, instance: MultiblockInstance) {
        val s = instance.state as? FrameState ?: return
        val multi = s.multiblock
        val anchor = instance.anchorPos
        if (!s.building) {
            s.ticks = Math.floorMod(s.ticks - 1, FrameState.TICKS_TO_CHECK)
            if (s.ticks == 0 && StorageUtils.removeItemsFromStorage(s.storage, multi.required().map(IItemStack::of))) {
                // v3 dropped whatever else was in the frame once building started.
                for (i in 0 until s.storage.size()) {
                    val left = s.storage.get(i).toMinecraft()
                    if (!left.isEmpty) Block.popResource(level, anchor, left.copy())
                    s.storage.setSlot(i, IItemStack.Empty)
                }
                s.building = true
                (level.getBlockEntity(anchor) as? FrameBlockEntity)?.markDirtyAndSync()
            }
            return
        }
        s.progress++
        // Clients draw the machine taking shape; they advance the progress themselves between these syncs.
        val home = level.getBlockEntity(anchor) as? FrameBlockEntity
        if (s.progress % SYNC_TICKS == 0) home?.markDirtyAndSync() else home?.markDirty()
        // Nanites at work: now and then one in a random frame block (v3 `BlockFrame.randomDisplayTick`).
        if (level is net.minecraft.server.level.ServerLevel) {
            for (pos in multi.takenLocations(anchor)) {
                if (level.random.nextInt(6) != 0) continue
                val at = net.minecraft.world.phys.Vec3(pos.x + level.random.nextDouble(), pos.y + level.random.nextDouble(), pos.z + level.random.nextDouble())
                com.itszuvalex.femtocraft.core.FemtoParticles.sendNanite(level, at, com.itszuvalex.femtocraft.core.FemtoParticles.randomColor(level.random, 128))
            }
        }
        if (s.progress >= FrameState.BUILD_TIME) multi.formAt(level, anchor)
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
        if (multi != null) {
            builder.accept(Component.translatable("tooltip.femtocraft.frame.needs").withStyle(ChatFormatting.GRAY))
            builder.accept(Component.translatable("tooltip.femtocraft.frame.needs.frames", multi.numFrames).withStyle(ChatFormatting.DARK_GRAY))
            for (need in multi.required()) {
                builder.accept(Component.translatable("tooltip.femtocraft.frame.needs.item", need.count, need.hoverName).withStyle(ChatFormatting.DARK_GRAY))
            }
        }
    }

    companion object {
        @JvmField
        val SELECTION: DeferredHolder<DataComponentType<*>, DataComponentType<String>> =
            FemtoRegistries.DATA_COMPONENTS.registerComponentType("frame_selection") { b -> b.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8) }

        fun selection(stack: ItemStack): String? = stack.get(SELECTION.get())

        fun withSelection(stack: ItemStack, id: String): ItemStack = stack.also { it.set(SELECTION.get(), id) }

        /**
         * Places the frame blocks of [multi] anchored at [anchor] and forms them.
         */
        fun place(level: Level, anchor: BlockPos, multi: FrameMultiblock) {
            multi.takenLocations(anchor).forEach { level.setBlockAndUpdate(it, IndustryContent.FRAME.get().defaultBlockState()) }
            MultiblockManager.SERVER.form(ILevel.of(level), multi.frameShape, anchor)
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

    private var synced: FrameMultiblock? = null

    /** The multiblock being built (on the client, the synced copy), or null. */
    val multiblock: FrameMultiblock? get() = synced ?: blockEntity?.multiblock()

    init {
        addStorageSlots(be?.storage ?: IItemStorage.Empty, 62, 17, columns = 3, count = if (be == null) 0 else 9)
        addPlayerInventorySlots(inventory)
        addSync(com.itszuvalex.itszulib.menu.MenuSyncs.int({ be?.frameState()?.progress ?: 0 }, { progress = it }))
        addSync(com.itszuvalex.itszulib.menu.MenuSyncs.boolean({ be?.frameState()?.building ?: false }, { building = it }))
        addSync(com.itszuvalex.itszulib.menu.MenuSyncs.string({ be?.multiblock()?.id ?: "" }, { synced = FrameMultiblocks.get(it) }))
    }

    /**
     * What the multiblock still needs: each required stack with how many of it the frame holds (up to the amount).
     */
    fun requirements(): List<Pair<ItemStack, Int>> {
        val multi = multiblock ?: return listOf()
        val held = (0 until FRAME_SLOTS).map { slots[it].item }
        return multi.required().map { need ->
            need to minOf(need.count, held.filter { ItemStack.isSameItemSameComponents(it, need) }.sumOf { it.count })
        }
    }

    companion object {
        const val FRAME_SLOTS = 9
    }
}
