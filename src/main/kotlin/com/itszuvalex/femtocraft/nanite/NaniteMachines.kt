package com.itszuvalex.femtocraft.nanite

import com.itszuvalex.femtocraft.core.FemtoHorizontalEntityBlock
import com.itszuvalex.femtocraft.core.FragDerivedColor
import com.itszuvalex.femtocraft.industry.IndustryContent
import com.itszuvalex.femtocraft.industry.ItemProcessingMachineBlockEntity
import com.itszuvalex.femtocraft.industry.ProcessingMachineBlockEntity
import com.itszuvalex.femtocraft.power.FragPowerStorage
import com.itszuvalex.femtocraft.power.FragWirelessPowerLeafNode
import com.itszuvalex.femtocraft.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.power.addWirelessLeaf
import com.itszuvalex.femtocraft.power.parentColor
import com.itszuvalex.itszulib.api.Modules
import com.itszuvalex.itszulib.api.adapters.IBattery
import com.itszuvalex.itszulib.api.storage.IItemStorage
import com.itszuvalex.itszulib.api.storage.PowerBattery
import com.itszuvalex.itszulib.core.HorizontalFacing
import com.itszuvalex.itszulib.core.SidedItemStorageConfiguration
import com.itszuvalex.itszulib.core.frag.FragDropInventory
import com.itszuvalex.itszulib.core.frag.FragItemAutoIO
import com.itszuvalex.itszulib.core.frag.FragItemStorage
import com.itszuvalex.itszulib.core.frag.FragMenu
import com.itszuvalex.itszulib.core.frag.FragSidedConfiguration
import com.itszuvalex.itszulib.core.frag.addItemStorage
import com.itszuvalex.itszulib.core.frag.addTickableFragment
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

/**
 * Nanite infusion recipes. Port of v3's `NaniteInfusionRecipeRegistry`: one dumb nanite turns devoid ingots
 * activated and makes the replacement dusts.
 */
object NaniteInfusionRecipes {
    data class Recipe(val input: () -> Item, val nanites: NaniteStack, val output: () -> Item)

    private val recipes = listOf(
        Recipe({ IndustryContent.RIFTIRON_INGOT_DEVOID.get() }, NaniteRegistry.dumb(1), { IndustryContent.RIFTIRON_INGOT_ACTIVATED.get() }),
        Recipe({ IndustryContent.PHASEMETAL_INGOT_DEVOID.get() }, NaniteRegistry.dumb(1), { IndustryContent.PHASEMETAL_INGOT_ACTIVATED.get() }),
        Recipe({ Items.REDSTONE }, NaniteRegistry.dumb(1), { IndustryContent.REDSTONEREPLACEMENT_DUST.get() }),
        Recipe({ Items.LAPIS_LAZULI }, NaniteRegistry.dumb(1), { IndustryContent.LAPISREPLACEMENT_DUST.get() }),
        Recipe({ IndustryContent.DIAMOND_DUST.get() }, NaniteRegistry.dumb(1), { IndustryContent.DIAMONDREPLACEMENT_DUST.get() }),
    )

    fun find(stack: ItemStack): Recipe? = if (stack.isEmpty) null else recipes.firstOrNull { stack.`is`(it.input()) }
}

/**
 * The two nanite machines: a nanite tank, its sided configuration (every face starts on the tank), nanite auto IO, a
 * battery and a wireless consumer leaf.
 */
interface NaniteMachine {
    val naniteTank: NaniteTank
}

private fun addNaniteMachineFragments(
    fl: com.itszuvalex.itszulib.core.BlockEntityFragmentCollection,
    be: ProcessingMachineBlockEntity,
    tank: NaniteTank,
    leaf: FragWirelessPowerLeafNode,
    battery: () -> IBattery,
) {
    fl.addFragment(FragSidedConfiguration(
        "NaniteConfig",
        SidedNaniteStorageConfiguration({ TANK }, mapOf(NONE to INaniteTank.EMPTY, TANK to tank)) { HorizontalFacing.front(be.blockState) },
        NaniteModules.NANITE_STORAGE_CONFIGURABLE,
    ))
    fl.addFragment(FragNaniteTank(tank))
    fl.addTickableFragment(FragNaniteAutoIO())
    fl.addFragment(FragPowerStorage(battery))
    fl.addWirelessLeaf(leaf)
    fl.addFragment(FragDerivedColor { parentColor(be, leaf) })
}

private const val TANK = "Tank"
private const val NONE = "None"

/**
 * Extracts nanites from cybermaterials ([CybermaterialNanites]) into a 50-nanite tank. Port of v3's
 * `TileNaniteExtractor`: items enter from the top and back.
 *
 * Difference from v3: only items that yield nanites are taken (v3 consumed any item that reached the slot and produced
 * nothing for those without a mapping).
 */
class NaniteExtractorBlockEntity(pos: BlockPos, state: BlockState) :
    ProcessingMachineBlockEntity(NaniteContent.NANITE_EXTRACTOR_BE.get(), pos, state, 1), NaniteMachine {
    override val battery: IBattery = PowerBattery(5000.0) { markDirty() }
    override val naniteTank = NaniteTank(TANK_SIZE, { markDirty() })

    @JvmField
    val leaf = FragWirelessPowerLeafNode({ battery }, PowerStorageNodeType.CONSUMER, rate = { 80.0 })

    private var pending: NaniteStack = NaniteStack.EMPTY

    init {
        fragList.addFragment(FragSidedConfiguration(
            "ItemConfig",
            SidedItemStorageConfiguration({ if (it == Direction.UP || it == Direction.SOUTH) INPUT else NONE }, mapOf(NONE to IItemStorage.Empty, INPUT to input)) { HorizontalFacing.front(blockState) },
            Modules.ITEM_STORAGE_CONFIGURABLE,
        ))
        fragList.addItemStorage(FragItemStorage(inventory))
        fragList.addInternalFragment(FragDropInventory(inventory))
        fragList.addTickableFragment(FragItemAutoIO())
        addNaniteMachineFragments(fragList, this, naniteTank, leaf) { battery }
        fragList.addFragment(FragMenu(Component.translatable("block.femtocraft.nanite_extractor"), { id, inv, _ -> NaniteMachineMenu(id, inv, this) }))
    }

    override fun canInsertSlot(index: Int, stack: ItemStack): Boolean = hasRecipe(stack)
    override fun hasRecipe(stack: ItemStack): Boolean = CybermaterialNanites.nanitesFor(stack) != null

    override fun computeResult(stack: ItemStack): Boolean {
        pending = CybermaterialNanites.nanitesFor(stack) ?: NaniteStack.EMPTY
        return !pending.isEmpty
    }

    override fun pushResult(): Boolean {
        pending = naniteTank.fill(pending, true)
        return pending.isEmpty
    }

    override fun clearResult() {
        pending = NaniteStack.EMPTY
    }

    override fun saveResult(output: ValueOutput) = output.store(PENDING_KEY, NaniteStack.CODEC, pending)
    override fun loadResult(input: ValueInput) {
        pending = input.read(PENDING_KEY, NaniteStack.CODEC).orElse(NaniteStack.EMPTY)
    }

    companion object {
        const val TANK_SIZE = 50
    }
}

/**
 * Infuses items with nanites ([NaniteInfusionRecipes]), paying the nanites when it starts. Port of v3's
 * `TileNaniteInfuser` (battery 4000, 50-nanite tank, 20-second infusions).
 */
class NaniteInfuserBlockEntity(pos: BlockPos, state: BlockState) :
    ItemProcessingMachineBlockEntity(NaniteContent.NANITE_INFUSER_BE.get(), pos, state, 2), NaniteMachine {
    override val battery: IBattery = PowerBattery(4000.0) { markDirty() }
    override val naniteTank = NaniteTank(NaniteExtractorBlockEntity.TANK_SIZE, { markDirty() })

    @JvmField
    val leaf = FragWirelessPowerLeafNode({ battery }, PowerStorageNodeType.CONSUMER, rate = { 50.0 })

    init {
        task.minTicks = TICKS_REQ
        task.baseGoal = POWER_REQ.toDouble()
        addItemMachineFragments("block.femtocraft.nanite_infuser") { id, inv -> NaniteMachineMenu(id, inv, this) }
        addNaniteMachineFragments(fragList, this, naniteTank, leaf) { battery }
    }

    override fun canInsertSlot(index: Int, stack: ItemStack): Boolean = index == 0 && hasRecipe(stack)
    override fun hasRecipe(stack: ItemStack): Boolean = NaniteInfusionRecipes.find(stack) != null

    override fun payStartCost(stack: ItemStack): Boolean {
        val need = NaniteInfusionRecipes.find(stack)?.nanites ?: return false
        if (naniteTank.drain(need, false).amount < need.amount) return false
        naniteTank.drain(need, true)
        return true
    }

    override fun resultFor(stack: ItemStack): ItemStack = NaniteInfusionRecipes.find(stack)?.let { ItemStack(it.output()) } ?: ItemStack.EMPTY

    companion object {
        const val TICKS_REQ = 20 * 20
        const val POWER_REQ = TICKS_REQ * 10
    }
}

class NaniteExtractorBlock(p: BlockBehaviour.Properties) : FemtoHorizontalEntityBlock<NaniteExtractorBlockEntity>(p, { NaniteContent.NANITE_EXTRACTOR_BE.get() })
class NaniteInfuserBlock(p: BlockBehaviour.Properties) : FemtoHorizontalEntityBlock<NaniteInfuserBlockEntity>(p, { NaniteContent.NANITE_INFUSER_BE.get() })

