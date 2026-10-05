package com.itszuvalex.femtocraft.nanite

import com.itszuvalex.itszulib.api.adapters.IBlockEntity
import com.itszuvalex.itszulib.api.adapters.ILevel
import com.itszuvalex.itszulib.api.adapters.IModule
import com.itszuvalex.itszulib.api.utility.NBTSerializationScope
import com.itszuvalex.itszulib.core.BreakBehavior
import com.itszuvalex.itszulib.core.EnumAutomaticIO
import com.itszuvalex.itszulib.core.IBreakContents
import com.itszuvalex.itszulib.core.IBlockEntityTickable
import com.itszuvalex.itszulib.core.frag.BlockEntityFragment
import com.itszuvalex.itszulib.core.frag.FragAutoIO
import com.itszuvalex.itszulib.core.frag.InternalBlockEntityFragment
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

/**
 * Exposes a nanite tank through [NaniteModules.NANITE_TANK], per side following the block's nanite configuration, and
 * saves it (LEVEL). Port of v3's `ModuleINaniteTank`.
 */
class FragNaniteTank @JvmOverloads constructor(
    val tank: INaniteTank,
    private val name: String = "NaniteTank",
    val persist: Boolean = true,
    override val breakBehavior: BreakBehavior = BreakBehavior.DISCARD,
) : BlockEntityFragment<INaniteTank>(), IBreakContents {
    init {
        require(breakBehavior != BreakBehavior.DROP) { "Nanites cannot be dropped; use KEEP or DISCARD" }
    }

    override fun isContentEmpty(): Boolean = tank.amount <= 0

    override fun describe(): List<net.minecraft.network.chat.Component> =
        listOf(net.minecraft.network.chat.Component.translatable("tooltip.femtocraft.contents.nanites", tank.amount, tank.capacity))

    fun tankFor(side: Direction?): INaniteTank? {
        if (side == null) return tank
        val config = host?.blockEntity()?.getModule(NaniteModules.NANITE_STORAGE_CONFIGURABLE, null) ?: return tank
        return config.getStorageForGlobalFacing(side)
    }

    override fun name(): String = name
    override fun module(): IModule<INaniteTank> = NaniteModules.NANITE_TANK
    override fun faceToModuleMapper(be: IBlockEntity): (Direction?) -> INaniteTank? = ::tankFor
    override fun handlesScope(scope: NBTSerializationScope): Boolean =
        persist && (scope == NBTSerializationScope.LEVEL || (scope == NBTSerializationScope.ITEM && breakBehavior == BreakBehavior.KEEP))
    override fun serializeTo(scope: NBTSerializationScope, output: ValueOutput) = tank.serialize(output)
    override fun deserialize(input: ValueInput, scope: NBTSerializationScope) = tank.deserialize(input)
}

/**
 * Moves nanites through faces set to INPUT or OUTPUT in the nanite configuration, every [ticksPerOperation] ticks, up
 * to [amountPerOperation] each way. Port of v3's `ModuleNaniteAutoIO` and `TileEntityUtils.checkDoNanite*IO`. Never
 * loads a chunk.
 */
class FragNaniteAutoIO @JvmOverloads constructor(
    private val ticksPerOperation: Int = TICKS_DEFAULT,
    private val amountPerOperation: Int = AMOUNT_DEFAULT,
) : InternalBlockEntityFragment(), IBlockEntityTickable {
    var ticks = 0
        private set

    override fun tick(level: ILevel, blockPos: BlockPos, blockState: BlockState) {
        if (level.isClientSide()) return
        ticks = FragAutoIO.incrementTicks(ticks, ticksPerOperation)
        if (ticks != 0) return
        val config = host?.blockEntity()?.getModule(NaniteModules.NANITE_STORAGE_CONFIGURABLE, null) ?: return
        var input = amountPerOperation
        var output = amountPerOperation
        for (face in Direction.entries) {
            val io = config.getIOForAbsoluteFacing(face)
            if (io == EnumAutomaticIO.NONE) continue
            val ours = config.getStorageForGlobalFacing(face) ?: continue
            val at = blockPos.relative(face)
            if (!level.isLoaded(at)) continue
            val theirs = level.getIBlockEntity(at)?.getModule(NaniteModules.NANITE_TANK, face.opposite) ?: continue
            if (io == EnumAutomaticIO.INPUT && input > 0) input -= move(theirs, ours, input)
            if (io == EnumAutomaticIO.OUTPUT && output > 0) output -= move(ours, theirs, output)
        }
        if (input != amountPerOperation || output != amountPerOperation) markDirty()
    }

    override fun name(): String = "NaniteAutoIO"
    override fun handlesScope(scope: NBTSerializationScope): Boolean = scope == NBTSerializationScope.LEVEL
    override fun serializeTo(scope: NBTSerializationScope, output: ValueOutput) = output.putInt("Ticks", ticks)
    override fun deserialize(input: ValueInput, scope: NBTSerializationScope) {
        ticks = input.getIntOr("Ticks", 0)
    }

    companion object {
        const val TICKS_DEFAULT = 20
        const val AMOUNT_DEFAULT = 1

        /**
         * Moves up to [amount] nanites, strain by strain, from [from] to [to].
         *
         * @return Amount moved.
         */
        @JvmStatic
        fun move(from: INaniteTank, to: INaniteTank, amount: Int): Int {
            var left = amount
            for (stack in from.contents()) {
                if (left <= 0) break
                if (!to.canFill(stack)) continue
                val offered = from.drain(stack.withAmount(left), false)
                if (offered.isEmpty) continue
                val moved = offered.amount - to.fill(offered, true).amount
                if (moved > 0) from.drain(stack.withAmount(moved), true)
                left -= moved
            }
            return amount - left
        }
    }
}

/**
 * Which items yield which nanites in the nanite extractor. Port of v3's `CybermaterialRegistry` nanite maps; the cyber
 * area registers the cybermaterials.
 */
object CybermaterialNanites {
    private val pending = ArrayList<Pair<() -> Item, NaniteStack>>()
    private val items = HashMap<Item, NaniteStack>()

    fun register(item: () -> Item, nanites: NaniteStack) {
        pending += item to nanites
    }

    fun register(item: Item, nanites: NaniteStack) {
        items[item] = nanites
    }

    fun unregister(item: Item) {
        items.remove(item)
    }

    fun nanitesFor(stack: ItemStack): NaniteStack? {
        if (stack.isEmpty) return null
        resolvePending()
        return items[stack.item]
    }

    /**
     * Every registered cybermaterial with its nanites, for recipe viewers.
     */
    fun all(): Map<Item, NaniteStack> {
        resolvePending()
        return items.toMap()
    }

    private fun resolvePending() {
        if (pending.isNotEmpty()) {
            pending.forEach { (i, n) -> items[i()] = n }
            pending.clear()
        }
    }
}
