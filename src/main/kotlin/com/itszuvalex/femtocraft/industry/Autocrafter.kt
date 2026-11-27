package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.FemtoRegistries
import com.itszuvalex.femtocraft.computation.ComputationParticipant
import com.itszuvalex.femtocraft.computation.FragComputationLeaf
import com.itszuvalex.femtocraft.core.FemtoBlockEntity
import com.itszuvalex.femtocraft.core.FemtoHorizontalEntityBlock
import com.itszuvalex.femtocraft.core.FemtoMenu
import com.itszuvalex.femtocraft.core.FragData
import com.itszuvalex.femtocraft.core.FragDerivedColor
import com.itszuvalex.femtocraft.power.FragPowerStorage
import com.itszuvalex.femtocraft.power.FragWiredPowerLeafNode
import com.itszuvalex.femtocraft.power.FragWirelessPowerLeafNode
import com.itszuvalex.femtocraft.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.power.addWirelessLeaf
import com.itszuvalex.femtocraft.power.parentColor
import com.itszuvalex.itszulib.api.Modules
import com.itszuvalex.itszulib.api.adapters.IItemStack
import com.itszuvalex.itszulib.api.storage.IItemStorage
import com.itszuvalex.itszulib.api.storage.ItemStorageArray
import com.itszuvalex.itszulib.api.storage.ItemStorageSlice
import com.itszuvalex.itszulib.api.storage.PowerBattery
import com.itszuvalex.itszulib.api.wrappers.WrapperContainerIItemStorage
import com.itszuvalex.itszulib.core.Distributable
import com.itszuvalex.itszulib.core.DistributionRole
import com.itszuvalex.itszulib.core.HorizontalFacing
import com.itszuvalex.itszulib.core.SidedItemStorageConfiguration
import com.itszuvalex.itszulib.core.frag.FragDropInventory
import com.itszuvalex.itszulib.core.frag.FragItemAutoIO
import com.itszuvalex.itszulib.core.frag.FragItemStorage
import com.itszuvalex.itszulib.core.frag.FragMenu
import com.itszuvalex.itszulib.core.frag.FragSidedConfiguration
import com.itszuvalex.itszulib.core.frag.addItemStorage
import com.itszuvalex.itszulib.core.frag.addTickableFragment
import com.itszuvalex.itszulib.menu.EnergyView
import com.itszuvalex.itszulib.menu.MenuSync
import com.itszuvalex.itszulib.menu.MenuSyncs
import com.itszuvalex.itszulib.menu.StorageSlot
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.NonNullList
import net.minecraft.core.component.DataComponentType
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.Container
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.ItemContainerContents
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.item.crafting.CraftingInput
import net.minecraft.world.item.crafting.CraftingRecipe
import net.minecraft.world.item.crafting.RecipeCache
import net.minecraft.world.item.crafting.RecipeHolder
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.registries.DeferredHolder
import java.util.function.Consumer
import kotlin.math.min

/**
 * A crafting recipe as a crafting template carries it (DECISIONS D31): the 3x3 [grid] of example items (one each,
 * row by row) and the [result] it made when it was set, for tooltips and the screen. The recipe itself is looked up
 * again from the grid, so a datapack change takes effect.
 */
data class CraftingPattern(val grid: ItemContainerContents, val result: ItemContainerContents) {
    /** The nine example items (empty where the recipe has nothing). */
    fun items(): List<ItemStack> = NonNullList.withSize(Autocrafter.GRID, ItemStack.EMPTY).also(grid::copyInto)

    fun resultStack(): ItemStack = result.copyOne()

    companion object {
        @JvmField
        val CODEC: Codec<CraftingPattern> = RecordCodecBuilder.create { i ->
            i.group(
                ItemContainerContents.CODEC.fieldOf("grid").forGetter(CraftingPattern::grid),
                ItemContainerContents.CODEC.fieldOf("result").forGetter(CraftingPattern::result),
            ).apply(i, ::CraftingPattern)
        }

        @JvmField
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, CraftingPattern> = StreamCodec.composite(
            ItemContainerContents.STREAM_CODEC, CraftingPattern::grid,
            ItemContainerContents.STREAM_CODEC, CraftingPattern::result,
            ::CraftingPattern,
        )

        fun of(grid: List<ItemStack>, result: ItemStack): CraftingPattern =
            CraftingPattern(ItemContainerContents.fromItems(grid.map { it.copyWithCount(1) }), ItemContainerContents.fromItems(listOf(result.copy())))
    }
}

/**
 * The crafting template: blank, or holding a [CraftingPattern] (`femtocraft:crafting_pattern`). An autocrafter crafts
 * only while an encoded template is in its template slot; the template is the extra cost of a recipe and carries it
 * between autocrafters.
 */
class CraftingTemplateItem(properties: Properties) : Item(properties) {
    @Deprecated("Deprecated in Java")
    override fun appendHoverText(stack: ItemStack, context: TooltipContext, display: TooltipDisplay, tooltip: Consumer<Component>, flag: TooltipFlag) {
        val pattern = pattern(stack)
        if (pattern == null) {
            tooltip.accept(Component.translatable("tooltip.femtocraft.crafting_template.blank").withStyle(ChatFormatting.GRAY))
            return
        }
        val result = pattern.resultStack()
        tooltip.accept(Component.translatable("tooltip.femtocraft.crafting_template.crafts", result.count, result.hoverName).withStyle(ChatFormatting.GRAY))
        pattern.items().filter { !it.isEmpty }.groupBy { it.item }.forEach { (_, stacks) ->
            tooltip.accept(Component.translatable("tooltip.femtocraft.crafting_template.needs", stacks.size, stacks[0].hoverName).withStyle(ChatFormatting.DARK_GRAY))
        }
    }

    override fun isFoil(stack: ItemStack): Boolean = pattern(stack) != null

    companion object {
        @JvmField
        val PATTERN: DeferredHolder<DataComponentType<*>, DataComponentType<CraftingPattern>> =
            FemtoRegistries.DATA_COMPONENTS.registerComponentType("crafting_pattern") { b -> b.persistent(CraftingPattern.CODEC).networkSynchronized(CraftingPattern.STREAM_CODEC) }

        fun pattern(stack: ItemStack): CraftingPattern? = if (stack.item is CraftingTemplateItem) stack.get(PATTERN.get()) else null

        fun isBlank(stack: ItemStack): Boolean = stack.item is CraftingTemplateItem && !stack.has(PATTERN.get())
    }
}

/**
 * FLOPS an autocrafter has gathered towards its next craft: a computation job that takes up to [cost] while [wants]
 * (a craft could go), at most [cost] a tick. Pure, for unit tests.
 */
class CraftJob(private val cost: Double, private val wants: () -> Boolean) : Distributable {
    var stored = 0.0

    override val max: Double get() = if (wants()) cost else stored
    override val amount: Double get() = stored
    override val transferMax: Double get() = cost

    override fun add(amount: Double): Double {
        if (!wants()) return 0.0
        val taken = min(amount, cost - stored).coerceAtLeast(0.0)
        stored += taken
        return taken
    }

    override fun remove(amount: Double): Double = 0.0

    fun ready(): Boolean = stored >= cost - EPSILON

    /** Spends one craft's FLOPS. */
    fun spend() {
        stored = (stored - cost).coerceAtLeast(0.0)
    }

    companion object {
        private const val EPSILON = 1e-6
    }
}

/**
 * The rules of the autocrafter's grid, shared by the block entity and the menu.
 */
object Autocrafter {
    const val GRID = 9
    const val OUTPUTS = 9

    /** Storage index of the first output slot and of the template slot. */
    const val FIRST_OUTPUT = GRID
    const val TEMPLATE_SLOT = GRID + OUTPUTS
    const val SLOTS = TEMPLATE_SLOT + 1

    /**
     * M: how many crafts the basic autocrafter buffers (DECISIONS D31; maintainer: 4 for basic machines). A holder slot
     * holds N x M of its item, capped at a stack of it.
     */
    const val BUFFERED_CRAFTS = 4

    /** FLOPS and power per craft (proposed). */
    const val FLOPS_PER_CRAFT = 100.0
    const val POWER_PER_CRAFT = 40.0

    const val BATTERY_SIZE = 4000.0
    const val LEAF_TRANSFER_RATE = 50.0

    /**
     * N for one slot of a pattern: how many of its item one craft takes from that slot. Vanilla crafting takes one
     * from each filled slot; the count stays per slot so recipe kinds that take more can say so.
     */
    fun perCraft(example: ItemStack): Int = if (example.isEmpty) 0 else 1

    /** Default item faces (relative to a north-facing block): top and back take ingredients, the rest give results. */
    fun itemFaces(face: Direction): String = when (face) {
        Direction.UP, Direction.SOUTH -> ProcessingMachineBlockEntity.INPUT
        else -> ProcessingMachineBlockEntity.OUTPUT
    }

    /**
     * Whether all of [stacks] fit into [slots] (copies of the output slots, each holding up to its stack size). Fills
     * [slots] as it goes.
     */
    fun fitsInto(slots: MutableList<ItemStack>, stacks: List<ItemStack>): Boolean {
        for (stack in stacks) {
            var left = stack.count
            for (i in slots.indices) {
                if (left <= 0) break
                val there = slots[i]
                if (there.isEmpty || !ItemStack.isSameItemSameComponents(there, stack)) continue
                val moved = min(left, there.maxStackSize - there.count)
                if (moved > 0) {
                    slots[i] = there.copyWithCount(there.count + moved)
                    left -= moved
                }
            }
            for (i in slots.indices) {
                if (left <= 0) break
                if (!slots[i].isEmpty) continue
                val moved = min(left, stack.maxStackSize)
                slots[i] = stack.copyWithCount(moved)
                left -= moved
            }
            if (left > 0) return false
        }
        return true
    }
}

/**
 * The autocrafter (DECISIONS D31): crafts one vanilla crafting recipe whenever it has the ingredients, room for the
 * results, power and FLOPS.
 *
 * Storage: holder slots 0-8 (the grid), output slots 9-17, the template slot 18. The recipe is the encoded crafting
 * template's; without one the grid is a recipe editor: [ghosts] say what each slot wants (set by clicking with an item,
 * nothing taken), and [setRecipe] writes the grid onto a blank template, taking real items in the grid as the pattern
 * where there are any. Holder slots take whatever the recipe accepts in that place and hold N x M of it.
 */
class AutocrafterBlockEntity(pos: BlockPos, state: BlockState) : FemtoBlockEntity(IndustryContent.AUTOCRAFTER_BE.get(), pos, state) {
    /** Ghosts for editing a recipe without an encoded template (saved). */
    private val ghosts: MutableList<ItemStack> = MutableList(Autocrafter.GRID) { ItemStack.EMPTY }

    @JvmField
    val inventory: ItemStorageArray = object : ItemStorageArray(Autocrafter.SLOTS, { markDirty() }) {
        override fun canInsert(index: Int, stack: IItemStack): Boolean = when {
            stack.isEmpty() -> true
            index < Autocrafter.GRID -> accepts(index, stack.toMinecraft())
            index == Autocrafter.TEMPLATE_SLOT -> stack.toMinecraft().item is CraftingTemplateItem
            else -> false
        }

        override fun maxStackSize(index: Int): Int = when {
            index < Autocrafter.GRID -> capacity(index)
            index == Autocrafter.TEMPLATE_SLOT -> 1
            else -> super.maxStackSize(index)
        }
    }

    @JvmField
    val holders: IItemStorage = ItemStorageSlice(inventory, IntArray(Autocrafter.GRID) { it })

    @JvmField
    val outputs: IItemStorage = ItemStorageSlice(inventory, IntArray(Autocrafter.OUTPUTS) { Autocrafter.FIRST_OUTPUT + it })

    @JvmField
    val battery = PowerBattery(Autocrafter.BATTERY_SIZE) { markDirty() }

    @JvmField
    val job = CraftJob(Autocrafter.FLOPS_PER_CRAFT) { canCraft() }

    @JvmField
    val leaf = FragWirelessPowerLeafNode({ battery }, PowerStorageNodeType.CONSUMER, rate = { Autocrafter.LEAF_TRANSFER_RATE })

    /** Crafts done, for the screen and tests. */
    var crafted = 0L
        private set

    private val recipes = RecipeCache(2)
    private var checkedAt = Long.MIN_VALUE
    private var checked = false

    init {
        fragList.addFragment(FragSidedConfiguration(
            "ItemConfig",
            SidedItemStorageConfiguration(Autocrafter::itemFaces, mapOf(
                ProcessingMachineBlockEntity.NONE to IItemStorage.Empty,
                ProcessingMachineBlockEntity.INPUT to holders,
                ProcessingMachineBlockEntity.OUTPUT to outputs,
            )) { HorizontalFacing.front(blockState) },
            Modules.ITEM_STORAGE_CONFIGURABLE,
        ))
        fragList.addItemStorage(FragItemStorage(inventory))
        fragList.addInternalFragment(FragDropInventory(inventory))
        fragList.addTickableFragment(FragItemAutoIO())
        fragList.addFragment(FragPowerStorage({ battery }))
        fragList.addWirelessLeaf(leaf)
        fragList.addFragment(FragDerivedColor { parentColor(this, leaf) })
        fragList.addFragment(FragWiredPowerLeafNode({ battery }, PowerStorageNodeType.CONSUMER) { Autocrafter.LEAF_TRANSFER_RATE })
        fragList.addFragment(FragComputationLeaf { if (canCraft()) ComputationParticipant(DistributionRole.CONSUMER, job) else null })
        fragList.addFragment(FragMenu(Component.translatable("block.femtocraft.autocrafter"), { id, inv, _ -> AutocrafterMenu(id, inv, this) }))
        fragList.addInternalFragment(FragData("Autocrafter", FragData.LEVEL, { _, o ->
            o.store(GHOSTS_KEY, ItemContainerContents.CODEC, ItemContainerContents.fromItems(ghosts))
            o.putDouble(FLOPS_KEY, job.stored)
            o.putLong(CRAFTED_KEY, crafted)
        }, { _, i ->
            val read = NonNullList.withSize(Autocrafter.GRID, ItemStack.EMPTY)
            i.read(GHOSTS_KEY, ItemContainerContents.CODEC).ifPresent { it.copyInto(read) }
            for (k in 0 until Autocrafter.GRID) ghosts[k] = read[k]
            job.stored = i.getDoubleOr(FLOPS_KEY, 0.0)
            crafted = i.getLongOr(CRAFTED_KEY, 0L)
        }))
    }

    fun template(): ItemStack = inventory.get(Autocrafter.TEMPLATE_SLOT).toMinecraft()

    /** The encoded template's pattern, if there is one. */
    fun pattern(): CraftingPattern? = CraftingTemplateItem.pattern(template())

    fun encoded(): Boolean = pattern() != null

    /**
     * What each grid slot wants: the encoded template's items, or the ghosts while editing.
     */
    fun wanted(): List<ItemStack> = pattern()?.items() ?: ghosts.map(ItemStack::copy)

    /** The ghost of grid slot [index] (editing only). */
    fun ghost(index: Int): ItemStack = ghosts[index]

    /**
     * Sets the ghost of grid slot [index] to one of [stack] (empty clears it). Refused while an encoded template is in.
     */
    fun setGhost(index: Int, stack: ItemStack): Boolean {
        if (encoded() || index !in 0 until Autocrafter.GRID) return false
        ghosts[index] = stack.copyWithCount(1)
        markDirty()
        return true
    }

    /** How many items grid slot [index] holds: N x M for its item, at most a stack of it. */
    fun capacity(index: Int): Int {
        val example = pattern()?.items()?.get(index) ?: return 64
        if (example.isEmpty) return 0
        return min(Autocrafter.perCraft(example) * Autocrafter.BUFFERED_CRAFTS, example.maxStackSize)
    }

    /**
     * The grid as the recipe sees it, the real items where a slot holds some and the [ghosts] elsewhere (for setting a
     * recipe).
     */
    fun editPattern(): List<ItemStack> = (0 until Autocrafter.GRID).map { i ->
        val real = holders.get(i).toMinecraft()
        if (real.isEmpty) ghosts[i].copyWithCount(1) else real.copyWithCount(1)
    }

    private fun lookup(grid: List<ItemStack>): RecipeHolder<CraftingRecipe>? {
        val level = level as? ServerLevel ?: return null
        val input = CraftingInput.of(3, 3, grid)
        return recipes.get(level, input).orElse(null)
    }

    /** The encoded template's recipe, if it still exists. */
    fun recipe(): RecipeHolder<CraftingRecipe>? = pattern()?.let { lookup(it.items()) }

    /**
     * What the screen shows as the result: the encoded template's, or what the grid being edited would make.
     */
    fun previewResult(): ItemStack {
        pattern()?.let { return it.resultStack() }
        val grid = editPattern()
        val recipe = lookup(grid) ?: return ItemStack.EMPTY
        return recipe.value().assemble(CraftingInput.of(3, 3, grid))
    }

    /**
     * Whether [stack] may go into grid slot [index]: the encoded recipe needs something there and still matches with
     * [stack] in place of the example (any planks where the recipe takes planks), making the same result.
     */
    fun accepts(index: Int, stack: ItemStack): Boolean {
        val pattern = pattern() ?: return false
        val items = pattern.items()
        val example = items[index]
        if (example.isEmpty) return false
        if (ItemStack.isSameItemSameComponents(example, stack)) return true
        val level = level as? ServerLevel ?: return false
        val recipe = recipe()?.value() ?: return false
        val grid = items.toMutableList().also { it[index] = stack.copyWithCount(1) }
        val input = CraftingInput.of(3, 3, grid)
        return recipe.matches(input, level) && ItemStack.isSameItem(recipe.assemble(input), pattern.resultStack())
    }

    /**
     * The grid's items for one craft (one of each where the pattern wants something), if every slot has enough.
     */
    private fun craftInput(pattern: CraftingPattern): CraftingInput? {
        val items = pattern.items()
        val grid = items.mapIndexed { i, example ->
            val need = Autocrafter.perCraft(example)
            if (need == 0) return@mapIndexed ItemStack.EMPTY
            val have = holders.get(i).toMinecraft()
            if (have.count < need) return null
            have.copyWithCount(1)
        }
        return CraftingInput.of(3, 3, grid)
    }

    private data class Craft(val input: CraftingInput, val result: ItemStack, val remainders: List<ItemStack>)

    private fun plan(): Craft? {
        val level = level as? ServerLevel ?: return null
        val pattern = pattern() ?: return null
        val recipe = recipe()?.value() ?: return null
        val input = craftInput(pattern) ?: return null
        if (!recipe.matches(input, level)) return null
        val result = recipe.assemble(input)
        if (result.isEmpty) return null
        val remainders = recipe.getRemainingItems(input).filter { !it.isEmpty }
        val slots = (0 until Autocrafter.OUTPUTS).map { outputs.get(it).toMinecraft().copy() }.toMutableList()
        if (!Autocrafter.fitsInto(slots, listOf(result) + remainders)) return null
        return Craft(input, result, remainders)
    }

    /**
     * Whether a craft could go now: an encoded recipe, its ingredients, room for the results and power. Worked out once
     * a tick.
     */
    fun canCraft(): Boolean {
        val level = level ?: return false
        if (level.gameTime != checkedAt) {
            checkedAt = level.gameTime
            checked = battery.storage() >= Autocrafter.POWER_PER_CRAFT && plan() != null
        }
        return checked
    }

    /**
     * Crafts once if it can and has the FLOPS: takes N from each wanted slot, spends power and FLOPS, puts the result
     * and the remainders (empty buckets) into the output.
     *
     * @return True if it crafted.
     */
    fun tryCraft(): Boolean {
        if (!job.ready() || battery.storage() < Autocrafter.POWER_PER_CRAFT) return false
        val pattern = pattern() ?: return false
        val craft = plan() ?: return false
        pattern.items().forEachIndexed { i, example ->
            val need = Autocrafter.perCraft(example)
            if (need > 0) holders.split(i, need)
        }
        for (stack in listOf(craft.result) + craft.remainders) insertOutput(stack)
        battery.drain(Autocrafter.POWER_PER_CRAFT)
        job.spend()
        crafted++
        checkedAt = Long.MIN_VALUE
        markDirty()
        return true
    }

    private fun insertOutput(stack: ItemStack) {
        var left = IItemStack.of(stack.copy())
        for (i in 0 until Autocrafter.OUTPUTS) {
            if (left.isEmpty()) return
            left = inventory.insertUnchecked(Autocrafter.FIRST_OUTPUT + i, left)
        }
    }

    /**
     * Writes the grid's pattern ([editPattern]) onto the blank template in the template slot. Real items in the grid
     * stay as the first buffered craft.
     *
     * @return False without a blank template or if the grid makes no crafting recipe.
     */
    fun setRecipe(): Boolean {
        val template = template()
        if (!CraftingTemplateItem.isBlank(template)) return false
        val grid = editPattern()
        val recipe = lookup(grid) ?: return false
        val result = recipe.value().assemble(CraftingInput.of(3, 3, grid))
        if (result.isEmpty) return false
        val encoded = template.copyWithCount(1)
        encoded.set(CraftingTemplateItem.PATTERN.get(), CraftingPattern.of(grid, result))
        inventory.setSlot(Autocrafter.TEMPLATE_SLOT, IItemStack.of(encoded))
        for (i in 0 until Autocrafter.GRID) ghosts[i] = grid[i]
        checkedAt = Long.MIN_VALUE
        markDirty()
        return true
    }

    /**
     * Blanks the encoded template and hands the grid's items to [player] (dropped where they do not fit). The ghosts
     * keep the old pattern, to edit.
     *
     * @return False without an encoded template.
     */
    fun clearRecipe(player: Player?): Boolean {
        val pattern = pattern() ?: return false
        val blank = template().copyWithCount(1)
        blank.remove(CraftingTemplateItem.PATTERN.get())
        inventory.setSlot(Autocrafter.TEMPLATE_SLOT, IItemStack.of(blank))
        val items = pattern.items()
        for (i in 0 until Autocrafter.GRID) {
            ghosts[i] = items[i]
            val held = holders.get(i).toMinecraft()
            if (held.isEmpty) continue
            holders.setSlot(i, IItemStack.Empty)
            if (player != null) {
                if (!player.inventory.add(held)) player.drop(held, false)
            } else {
                net.minecraft.world.Containers.dropItemStack(level!!, blockPos.x + 0.5, blockPos.y + 1.0, blockPos.z + 0.5, held)
            }
        }
        job.stored = 0.0
        checkedAt = Long.MIN_VALUE
        markDirty()
        return true
    }

    override fun serverTick() {
        tryCraft()
    }

    companion object {
        const val GHOSTS_KEY = "Ghosts"
        const val FLOPS_KEY = "Flops"
        const val CRAFTED_KEY = "Crafted"
    }
}

class AutocrafterBlock(p: BlockBehaviour.Properties) : FemtoHorizontalEntityBlock<AutocrafterBlockEntity>(p, { IndustryContent.AUTOCRAFTER_BE.get() })

/**
 * A grid slot: shows what it wants faded while empty (the template's item, or the ghost being edited) and, while the
 * recipe is being edited, takes any item a player puts in (the shorthand for setting a recipe from real items).
 */
class HolderSlot(storage: IItemStorage, container: Container, index: Int, x: Int, y: Int, private val menu: AutocrafterMenu) :
    StorageSlot(storage, container, index, x, y) {
    override fun hint(): ItemStack = menu.wanted[containerSlot]

    override fun mayPlace(stack: ItemStack): Boolean = if (menu.editing()) true else super.mayPlace(stack)

    override fun getMaxStackSize(): Int = if (menu.editing()) 64 else super.getMaxStackSize()

    override fun getMaxStackSize(stack: ItemStack): Int = if (menu.editing()) stack.maxStackSize else super.getMaxStackSize(stack)
}

/**
 * The autocrafter's menu: the grid, the template slot, the output, the battery, FLOPS towards the next craft, and the
 * Set recipe ([ACTION_SET]) and Clear recipe ([ACTION_CLEAR]) buttons.
 *
 * While no encoded template is in, clicking a grid slot with an item sets its ghost (nothing is taken), and clicking an
 * empty one with an empty hand clears it; shift-clicking or dragging puts real items in, which Set recipe reads first.
 */
class AutocrafterMenu(containerId: Int, inventory: Inventory, be: AutocrafterBlockEntity?) :
    FemtoMenu<AutocrafterBlockEntity>(IndustryContent.AUTOCRAFTER_MENU.get(), containerId, inventory, be) {
    @JvmField
    var battery = EnergyView()

    /** Client copies. */
    @JvmField
    val wanted: MutableList<ItemStack> = MutableList(Autocrafter.GRID) { ItemStack.EMPTY }
    var result: ItemStack = ItemStack.EMPTY
    var flops = 0.0

    private val gridSlots: IntRange

    init {
        val storage = be?.inventory ?: IItemStorage.Empty
        val container = WrapperContainerIItemStorage(storage)
        val start = slots.size
        if (be != null) {
            for (i in 0 until Autocrafter.GRID) addBlockSlot(HolderSlot(storage, container, i, GRID_X + (i % 3) * 18, GRID_Y + (i / 3) * 18, this))
            addStorageSlots(storage, OUTPUT_X, GRID_Y, columns = 3, first = Autocrafter.FIRST_OUTPUT, count = Autocrafter.OUTPUTS, output = true)
            addStorageSlots(storage, TEMPLATE_X, TEMPLATE_Y, first = Autocrafter.TEMPLATE_SLOT, count = 1, hint = ItemStack(IndustryContent.CRAFTING_TEMPLATE.get()))
        }
        gridSlots = start until start + (if (be != null) Autocrafter.GRID else 0)
        addPlayerInventorySlots(inventory, 8, INVENTORY_Y)
        if (be != null) {
            battery = syncEnergy { be.battery }
            for (i in 0 until Autocrafter.GRID) addSync(stackSync({ be.wanted()[i] }) { wanted[i] = it })
            addSync(stackSync(be::previewResult) { result = it })
            addSync(MenuSyncs.double({ be.job.stored }) { flops = it })
        }
    }

    /** True while the recipe is being edited (no encoded template in the slot). */
    fun editing(): Boolean = blockEntity != null && !blockEntity.encoded()

    override fun clicked(slotIndex: Int, buttonNum: Int, containerInput: ContainerInput, player: Player) {
        val be = blockEntity
        if (be != null && slotIndex in gridSlots && containerInput == ContainerInput.PICKUP && editing()) {
            val slot = slots[slotIndex]
            val carried = carried
            if (!carried.isEmpty || !slot.hasItem()) {
                if (!player.level().isClientSide) be.setGhost(slot.containerSlot, carried)
                return
            }
        }
        super.clicked(slotIndex, buttonNum, containerInput, player)
    }

    override fun handleAction(player: Player, action: Int, data: Int): Boolean {
        val be = blockEntity ?: return false
        return when (action) {
            ACTION_SET -> be.setRecipe()
            ACTION_CLEAR -> be.clearRecipe(player)
            else -> false
        }
    }

    private fun stackSync(getter: () -> ItemStack, setter: (ItemStack) -> Unit): MenuSync<ItemStack> =
        MenuSync(getter, setter, ItemStack.OPTIONAL_STREAM_CODEC, ItemStack::matches, ItemStack::copy)

    companion object {
        const val ACTION_SET = 0
        const val ACTION_CLEAR = 1

        const val HEIGHT = 186
        const val GRID_X = 26
        const val GRID_Y = 17
        const val OUTPUT_X = 116
        const val TEMPLATE_X = 89
        const val TEMPLATE_Y = 53
        const val RESULT_X = 89
        const val RESULT_Y = 22
        const val INVENTORY_Y = 104
    }
}
