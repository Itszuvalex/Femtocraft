package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.FemtoComponents
import com.itszuvalex.femtocraft.FemtoItems
import com.itszuvalex.femtocraft.core.FragMultiBlock
import com.itszuvalex.itszulib.api.Components
import com.itszuvalex.itszulib.core.BlockEntityCore
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponentMap
import net.minecraft.core.component.DataComponentPatch
import net.minecraft.world.Containers
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block

/**
 * A machine built from frames: frames placed over [getTakenLocations] collect [getRequiredResources], then turn into
 * the machine's blocks. Port of 1.7.10 `IFrameMultiblock`. Positions are relative to the controller position passed
 * in.
 */
interface IFrameMultiblock {
    val name: String
    val allowedFrameTypes: Set<String>
    val numFrames: Int

    fun getTakenLocations(controller: BlockPos): List<BlockPos>

    fun canPlaceAtLocation(level: Level, controller: BlockPos): Boolean =
        getTakenLocations(controller).all { level.getBlockState(it).canBeReplaced() }

    fun getRequiredResources(): List<ItemStack>

    /**
     * Places the machine's blocks and forms the multiblock.
     */
    fun formAtLocation(level: Level, controller: BlockPos): Boolean

    fun formAtLocationFromItem(level: Level, controller: BlockPos, stack: ItemStack): Boolean = formAtLocation(level, controller)

    /**
     * Called when any part breaks; removes the rest.
     */
    fun onMultiblockBroken(level: Level, controller: BlockPos) = MultiblockGuard.run {
        getTakenLocations(controller).forEach { level.removeBlock(it, false) }
    }
}

/**
 * Suppresses the per-part break handlers while a multiblock removes its own blocks (1.7.10 used static `breaking`
 * flags for the same purpose).
 */
object MultiblockGuard {
    var breaking = false
        private set

    fun run(block: () -> Unit) {
        if (breaking) return
        breaking = true
        try {
            block()
        } finally {
            breaking = false
        }
    }
}

/**
 * Block entities that belong to a frame multiblock.
 */
interface IMultiblockPart {
    val multiblock: FragMultiBlock
}

/**
 * Cuboid multiblock of [block]s, [sx] x [sy] x [sz], starting at controller + ([ox], 0, [oz]).
 */
abstract class CuboidMultiblock(
    override val name: String,
    private val sx: Int,
    private val sy: Int,
    private val sz: Int,
    private val ox: Int = 0,
    private val oz: Int = 0,
) : IFrameMultiblock {
    abstract fun block(): Block

    override val allowedFrameTypes: Set<String> = setOf("Basic", "Cyber")
    override val numFrames: Int get() = sx * sy * sz

    override fun getTakenLocations(controller: BlockPos): List<BlockPos> =
        (0 until sx).flatMap { x -> (0 until sy).flatMap { y -> (0 until sz).map { z -> controller.offset(ox + x, y, oz + z) } } }

    override fun formAtLocation(level: Level, controller: BlockPos): Boolean {
        val locations = getTakenLocations(controller)
        var ok = true
        MultiblockGuard.run { ok = locations.all { level.setBlockAndUpdate(it, block().defaultBlockState()) } }
        if (!ok) return false
        locations.forEach { (level.getBlockEntity(it) as? IMultiblockPart)?.multiblock?.form(controller) }
        return true
    }
}

object MultiblockArcFurnace : CuboidMultiblock("Arc Furnace", 2, 3, 2) {
    override fun block(): Block = FemtoBlocks.ARC_FURNACE.get()
    override fun getRequiredResources(): List<ItemStack> = listOf(
        ItemStack(FemtoItems.CYBERWEAVE.get(), 20),
        ItemStack(Items.IRON_INGOT, 32),
        ItemStack(Items.GOLD_INGOT, 4),
        ItemStack(Items.REDSTONE, 18),
    )
}

object MultiblockCentrifuge : CuboidMultiblock("Centrifuge", 3, 5, 3, -1, -1) {
    override fun block(): Block = FemtoBlocks.CENTRIFUGE.get()
    override fun getRequiredResources(): List<ItemStack> = emptyList()
}

object MultiblockCrystallizationChamber : CuboidMultiblock("Crystallization Chamber", 5, 3, 3, -2, -1) {
    override fun block(): Block = FemtoBlocks.CRYSTALLIZATION_CHAMBER.get()
    override val numFrames: Int get() = 3 * 5 * 3
    override fun getRequiredResources(): List<ItemStack> = emptyList()
}

/**
 * Keeps its inventory when broken: drops a multiblock item carrying the controller's ITEM-scope data, which is
 * applied again when the item re-forms it.
 */
object MultiblockMaterialProcessor : CuboidMultiblock("Material Processor", 2, 3, 2) {
    override fun block(): Block = FemtoBlocks.MATERIAL_PROCESSOR.get()
    override fun getRequiredResources(): List<ItemStack> = listOf(ItemStack(Items.COBBLESTONE, 24))

    override fun formAtLocationFromItem(level: Level, controller: BlockPos, stack: ItemStack): Boolean {
        val ret = formAtLocation(level, controller)
        val data = stack.get(Components.FRAGMENT_DATA.get())
        val be = level.getBlockEntity(controller) as? BlockEntityCore
        if (ret && data != null && be != null) {
            be.applyComponents(DataComponentMap.builder().set(Components.FRAGMENT_DATA.get(), data).build(), DataComponentPatch.EMPTY)
            be.setChanged()
        }
        return ret
    }

    override fun onMultiblockBroken(level: Level, controller: BlockPos) {
        val stack = FrameMultiblockRegistry.makeMultiblockItem(name)
        (level.getBlockEntity(controller) as? BlockEntityCore)?.collectComponents()?.get(Components.FRAGMENT_DATA.get())?.let {
            stack.set(Components.FRAGMENT_DATA.get(), it)
        }
        super.onMultiblockBroken(level, controller)
        Containers.dropItemStack(level, controller.x.toDouble(), controller.y.toDouble(), controller.z.toDouble(), stack)
    }
}

object FrameMultiblockRegistry {
    private val frameMap = LinkedHashMap<String, IFrameMultiblock>()

    fun getMultiblock(name: String?): IFrameMultiblock? = name?.let { frameMap[it] }

    fun getMultiblocksForFrameType(type: String): List<IFrameMultiblock> = frameMap.values.filter { type in it.allowedFrameTypes }

    fun registerMultiblock(multi: IFrameMultiblock) {
        frameMap[multi.name] = multi
    }

    fun makeMultiblockItem(name: String): ItemStack =
        ItemStack(FemtoItems.MULTIBLOCK.get()).also { it.set(FemtoComponents.MULTIBLOCK.get(), name) }

    init {
        registerMultiblock(MultiblockArcFurnace)
        registerMultiblock(MultiblockCentrifuge)
        registerMultiblock(MultiblockCrystallizationChamber)
        registerMultiblock(MultiblockMaterialProcessor)
    }
}
