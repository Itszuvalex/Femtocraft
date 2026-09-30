package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.FemtoBlockEntities
import com.itszuvalex.femtocraft.FemtoComponents
import com.itszuvalex.femtocraft.FemtoMenus
import com.itszuvalex.femtocraft.FemtoModules
import com.itszuvalex.femtocraft.FemtoTags
import com.itszuvalex.femtocraft.core.FemtoMenu
import com.itszuvalex.femtocraft.core.FragData
import com.itszuvalex.femtocraft.core.module
import com.itszuvalex.femtocraft.industry.item.AssemblyData
import com.itszuvalex.femtocraft.power.FragPowerNode
import com.itszuvalex.femtocraft.power.PowerNodeRules
import com.itszuvalex.femtocraft.power.PowerStorage
import com.itszuvalex.femtocraft.power.item.IPowerStorage
import com.itszuvalex.femtocraft.power.item.PowerCrystalItem
import com.itszuvalex.femtocraft.power.tile.ITilePower
import com.itszuvalex.itszulib.api.adapters.IItemStack
import com.itszuvalex.itszulib.api.storage.ItemStorageArray
import com.itszuvalex.itszulib.api.utility.NBTSerializationScope
import com.itszuvalex.itszulib.api.wrappers.WrapperResourceHandlerIItemStorage
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.TagKey
import net.minecraft.world.InteractionResult
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.SingleRecipeInput
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.capabilities.Capabilities
import kotlin.math.min

/**
 * A machine that runs assemblies on input items. Port of 1.7.10 `ITileAssemblyArray`.
 */
interface ITileAssemblyArray : ITilePower {
    fun getPowerMultiplier(): Double
    fun getTimeMultiplier(): Double
    fun getInputSlots(): Int
    fun getInputItem(slot: Int): ItemStack

    /**
     * @return Removed items.
     */
    fun removeInputItem(slot: Int, amt: Int): ItemStack
    fun getOutputSlots(): Int
    fun getOutputItem(slot: Int): ItemStack

    /**
     * Merges into [slot] or fills it if empty.
     *
     * @return The part of [item] that did not fit (empty if all fit).
     */
    fun addOrMergeOutputItem(item: ItemStack, slot: Int): ItemStack

    /**
     * Merges into matching output slots first, then empty ones.
     *
     * @return Leftover (empty if all fit).
     */
    fun addOutputItem(item: ItemStack): ItemStack {
        var ret = item
        for (slot in 0 until getOutputSlots()) {
            if (getOutputItem(slot).isEmpty) continue
            ret = addOrMergeOutputItem(ret, slot)
            if (ret.isEmpty) return ret
        }
        for (slot in 0 until getOutputSlots()) {
            if (!getOutputItem(slot).isEmpty) continue
            ret = addOrMergeOutputItem(ret, slot)
            if (ret.isEmpty) return ret
        }
        return ret
    }
}

/**
 * An item that does a machine's work while slotted in an assembly array. Port of 1.7.10 `IItemAssembly` and the
 * shared logic of `ItemFurnaceAssembly`/`ItemGrinderAssembly`. State lives in the `femtocraft:assembly` component.
 */
abstract class AssemblyItem(properties: Properties, val assemblyType: String) : Item(properties) {
    /**
     * @return The result of processing one of [input], or empty if this assembly cannot process it.
     */
    abstract fun resultFor(level: Level, input: ItemStack): ItemStack

    fun data(stack: ItemStack): AssemblyData = stack.get(FemtoComponents.ASSEMBLY.get()) ?: AssemblyData.EMPTY

    fun setData(stack: ItemStack, data: AssemblyData) {
        if (data == AssemblyData.EMPTY) stack.remove(FemtoComponents.ASSEMBLY.get()) else stack.set(FemtoComponents.ASSEMBLY.get(), data)
    }

    /**
     * One tick of work: flush a finished result, else draw power toward finishing the current item, else take an input.
     */
    fun onTick(stack: ItemStack, tile: ITileAssemblyArray, level: Level) {
        val d = data(stack)
        if (d.isWorking) {
            if (!d.result.isEmpty) {
                setData(stack, d.copy(result = tile.addOutputItem(d.result.copy())))
                return
            }
            val time = TICKS_REQUIRED * tile.getTimeMultiplier()
            val power = POWER_REQUIRED * tile.getPowerMultiplier()
            val powerThisTick = min(power / time, power - d.progress)
            val consumed = tile.drain(powerThisTick, true)
            val next = d.progress + consumed
            if (next >= power) {
                val result = resultFor(level, d.working)
                if (result.isEmpty) {
                    setData(stack, AssemblyData.EMPTY)
                } else {
                    val left = tile.addOutputItem(result)
                    setData(stack, AssemblyData(ItemStack.EMPTY, left, 0.0))
                }
            } else {
                setData(stack, d.copy(progress = next))
            }
        } else {
            for (i in 0 until tile.getInputSlots()) {
                val input = tile.getInputItem(i)
                if (input.isEmpty || resultFor(level, input).isEmpty) continue
                setData(stack, AssemblyData(input.copyWithCount(1), ItemStack.EMPTY, 0.0))
                tile.removeInputItem(i, 1)
                break
            }
        }
    }

    override fun isBarVisible(stack: ItemStack): Boolean = data(stack).progress > 0

    override fun getBarWidth(stack: ItemStack): Int = (13.0 * data(stack).progress / POWER_REQUIRED).toInt().coerceIn(0, 13)

    override fun getBarColor(stack: ItemStack): Int = 0x55FF55

    companion object {
        const val TICKS_REQUIRED = 20 * 10
        const val POWER_REQUIRED = TICKS_REQUIRED * 10
    }
}

/**
 * Smelts, using the level's smelting recipes. Port of 1.7.10 `ItemFurnaceAssembly`.
 */
class FurnaceAssemblyItem(properties: Properties) : AssemblyItem(properties, "Furnace") {
    override fun resultFor(level: Level, input: ItemStack): ItemStack {
        val server = level as? ServerLevel ?: return ItemStack.EMPTY
        val single = SingleRecipeInput(input.copyWithCount(1))
        return server.recipeAccess().getRecipeFor(RecipeType.SMELTING, single, server)
            .map { it.value().assemble(single) }.orElse(ItemStack.EMPTY)
    }
}

/**
 * Grinds ores to dusts. Port of 1.7.10 `ItemGrinderAssembly`.
 */
class GrinderAssemblyItem(properties: Properties) : AssemblyItem(properties, "Grinder") {
    override fun resultFor(level: Level, input: ItemStack): ItemStack = DustRecipes.getDust(input)
}

/**
 * Ore -> dust mapping by convention tags: an item in `c:ores/<x>` grinds to the first item in `c:dusts/<x>`, 2 of
 * them (6 for redstone). Port of 1.7.10 `DustRecipeRegistry`, which matched ore dictionary `ore<X>`/`dust<X>` names.
 */
object DustRecipes {
    const val DEFAULT_DUST = 2
    private val overrides = mapOf("redstone" to 6)

    fun getDust(item: ItemStack): ItemStack {
        for (tag in item.tags().toList()) {
            val id = tag.location()
            if (id.namespace != "c" || !id.path.startsWith("ores/")) continue
            val ore = id.path.removePrefix("ores/")
            val dustTag = TagKey.create(net.minecraft.core.registries.Registries.ITEM, net.minecraft.resources.Identifier.fromNamespaceAndPath("c", "dusts/$ore"))
            val dust = BuiltInRegistries.ITEM.getTagOrEmpty(dustTag).firstOrNull() ?: continue
            return ItemStack(dust.value(), overrides[ore] ?: DEFAULT_DUST)
        }
        return ItemStack.EMPTY
    }
}

/**
 * Material processor: a 2x3x2 frame machine running up to two assemblies. Slots on the controller: 4 inputs,
 * 2 assemblies, 4 outputs, 1 power storage, 1 nanite strain. Power comes from the item in the power slot, else from
 * the parent power node. Port of 1.7.10 `TileMaterialProcessor`; its inventory travels with the multiblock item.
 */
class MaterialProcessorBlockEntity(pos: BlockPos, state: BlockState) :
    MultiblockPartBlockEntity(FemtoBlockEntities.MATERIAL_PROCESSOR.get(), pos, state, { MultiblockMaterialProcessor }),
    ITileAssemblyArray, MenuProvider {

    val inventory = object : ItemStorageArray(INVENTORY_SIZE, Runnable { setChanged() }) {
        override fun canInsert(index: Int, stack: IItemStack): Boolean = isItemValidForSlot(index, stack.toMinecraft())
    }
    val itemHandler = WrapperResourceHandlerIItemStorage.of(inventory)
    val powerNode = FragPowerNode(PowerNodeRules.DIFFUSION_TARGET, SlotOrParentStorage())

    init {
        fragList.addFragment(powerNode)
        fragList.addInternalFragment(FragData("Inventory", setOf(NBTSerializationScope.LEVEL, NBTSerializationScope.ITEM), { _, out ->
            inventory.serialize(out)
        }, { _, input -> inventory.deserialize(input) }))
        fragList.addCapability(Capabilities.Item.BLOCK) { controllerOrNull()?.itemHandler }
    }

    fun controllerOrNull(): MaterialProcessorBlockEntity? = if (multiblock.isController) this else multiblock.controller(level)

    fun isItemValidForSlot(slot: Int, item: ItemStack): Boolean {
        if (item.isEmpty) return true
        return when (slot) {
            in INPUT_START until INPUT_START + NUM_INPUT -> true
            in ASSEMBLY_START until ASSEMBLY_START + NUM_ASSEMBLY -> (item.item as? AssemblyItem)?.assemblyType in ACCEPTED_ASSEMBLY_TYPES
            in OUTPUT_START until OUTPUT_START + NUM_OUTPUT -> true
            POWER_SLOT -> item.item is IPowerStorage
            NANITE_SLOT -> item.`is`(FemtoTags.Items.NANITE_STRAINS)
            else -> false
        }
    }

    override fun onServerLoad() {
        if (multiblock.isController) powerNode.onServerLoad()
    }

    override fun onServerUnload() = powerNode.onServerUnload()

    override fun serverTick() {
        if (!multiblock.isController) return
        val lvl = level ?: return
        for (i in 0 until NUM_ASSEMBLY) {
            val stack = inventory.get(ASSEMBLY_START + i).toMinecraft()
            val assembly = stack.item as? AssemblyItem ?: continue
            assembly.onTick(stack, this, lvl)
        }
        val power = inventory.get(POWER_SLOT).toMinecraft()
        (power.item as? PowerCrystalItem)?.onTick(power)
        setChanged()
    }

    override fun onUse(player: Player): InteractionResult {
        val controller = controllerOrNull() ?: return InteractionResult.PASS
        if (isServer) player.openMenu(controller, controller.blockPos)
        return InteractionResult.SUCCESS
    }

    override fun getDisplayName(): Component = Component.translatable("block.femtocraft.material_processor")

    override fun createMenu(containerId: Int, inventory: Inventory, player: Player): AbstractContainerMenu =
        MaterialProcessorMenu(containerId, inventory, this)

    // ITileAssemblyArray, on the controller's inventory

    override fun getPowerMultiplier(): Double = 1.0
    override fun getTimeMultiplier(): Double = 1.0
    override fun getInputSlots(): Int = NUM_INPUT
    override fun getInputItem(slot: Int): ItemStack = inventory.get(INPUT_START + slot).toMinecraft()
    override fun getOutputSlots(): Int = NUM_OUTPUT
    override fun getOutputItem(slot: Int): ItemStack = inventory.get(OUTPUT_START + slot).toMinecraft()

    override fun removeInputItem(slot: Int, amt: Int): ItemStack = inventory.split(INPUT_START + slot, amt).toMinecraft()

    override fun addOrMergeOutputItem(item: ItemStack, slot: Int): ItemStack =
        inventory.insert(OUTPUT_START + slot, IItemStack.of(item.copy())).toMinecraft()

    override fun getCurrentPower(): Double = powerNode.getPowerCurrent()
    override fun getMaximumPower(): Double = powerNode.getPowerMax()
    override fun drain(amt: Double, doDrain: Boolean): Double = powerNode.usePower(amt, doDrain)
    override fun charge(amt: Double, doCharge: Boolean): Double = powerNode.addPower(amt, doCharge)

    /**
     * Power from the power slot item if present, else from the parent node.
     */
    private inner class SlotOrParentStorage : PowerStorage {
        private fun slot(): Pair<ItemStack, IPowerStorage>? =
            inventory.get(POWER_SLOT).toMinecraft().let { s -> (s.item as? IPowerStorage)?.let { s to it } }

        private fun parent() = level?.let { powerNode.getParentLoc()?.module(it, FemtoModules.POWER_NODE) }

        override fun current(): Double = slot()?.let { (s, p) -> p.getStorageCurrent(s) } ?: parent()?.getPowerCurrent() ?: 0.0
        override fun max(): Double = slot()?.let { (s, p) -> p.getStorageMax(s) } ?: parent()?.getPowerMax() ?: 0.0
        override fun set(amount: Double) {
            slot()?.let { (s, p) -> p.setStorageCurrent(s, amount) } ?: parent()?.setPower(amount)
        }
        override fun add(amount: Double, doFill: Boolean): Double =
            slot()?.let { (s, p) -> p.store(s, amount, doFill) } ?: parent()?.addPower(amount, doFill) ?: 0.0
        override fun use(amount: Double, doUse: Boolean): Double =
            slot()?.let { (s, p) -> p.consume(s, amount, doUse) } ?: parent()?.usePower(amount, doUse) ?: 0.0
    }

    companion object {
        val ACCEPTED_ASSEMBLY_TYPES = setOf("Furnace", "Grinder")
        const val NUM_INPUT = 4
        const val NUM_OUTPUT = 4
        const val NUM_ASSEMBLY = 2
        const val INPUT_START = 0
        const val ASSEMBLY_START = NUM_INPUT
        const val OUTPUT_START = NUM_INPUT + NUM_ASSEMBLY
        const val POWER_SLOT = NUM_INPUT + NUM_ASSEMBLY + NUM_OUTPUT
        const val NANITE_SLOT = POWER_SLOT + 1
        const val INVENTORY_SIZE = NANITE_SLOT + 1
    }
}

/**
 * Port of 1.7.10 `ContainerMaterialProcessor` (same slot layout).
 */
class MaterialProcessorMenu(containerId: Int, inventory: Inventory, val processor: MaterialProcessorBlockEntity?) :
    FemtoMenu(FemtoMenus.MATERIAL_PROCESSOR.get(), containerId, inventory, processor) {
    constructor(containerId: Int, inventory: Inventory, buf: RegistryFriendlyByteBuf) :
        this(containerId, inventory, readBlockEntity<MaterialProcessorBlockEntity>(inventory, buf))

    var power = 0L
    var powerMax = 0L

    init {
        processor?.let { p ->
            val inv = p.inventory
            addStorageSlot(inv, 0, 35, 8)
            addStorageSlot(inv, 1, 53, 8)
            addStorageSlot(inv, 2, 35, 26)
            addStorageSlot(inv, 3, 53, 26)
            addStorageSlot(inv, 4, 152, 45)
            addStorageSlot(inv, 5, 152, 63)
            addOutputSlot(inv, 6, 35, 45)
            addOutputSlot(inv, 7, 53, 45)
            addOutputSlot(inv, 8, 35, 63)
            addOutputSlot(inv, 9, 53, 63)
            addStorageSlot(inv, 10, 10, 64)
            addStorageSlot(inv, 11, 152, 8)
            trackLong({ p.getCurrentPower().toLong() }, { power = it })
            trackLong({ p.getMaximumPower().toLong() }, { powerMax = it })
        }
        addPlayerInventory()
    }
}
