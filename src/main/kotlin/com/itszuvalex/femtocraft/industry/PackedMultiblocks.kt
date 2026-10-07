package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.FemtoRegistries
import com.itszuvalex.itszulib.api.adapters.ILevel
import com.itszuvalex.itszulib.api.multiblock.IMultiblockState
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.core.component.DataComponentType
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.ProblemReporter
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.TagValueInput
import net.minecraft.world.level.storage.TagValueOutput
import net.neoforged.neoforge.registries.DeferredHolder
import java.util.function.Consumer

/**
 * The state of a multiblock that keeps its contents when it is broken (the storage multiblocks): breaking any block, or
 * a wrench, drops one item for the whole machine ([FrameMultiblock.packedItem]) carrying the state
 * ([PackedMultiblocks.STATE]), and using that item builds the machine again with its contents. The machine item is
 * rendered as the whole structure at the size of one block.
 */
interface PackedState : IMultiblockState {
    /** Whether there is nothing to carry, so the dropped item is plain and stacks. */
    fun isPackedEmpty(): Boolean

    /** Lines saying how much is used of how much it holds, for the item's tooltip. */
    fun describe(): List<Component>
}

object PackedMultiblocks {
    /** Called from content init so the data component registers with the others. */
    fun init() {}

    /** The packed state of a multiblock item: what [IMultiblockState.serialize] wrote. */
    @JvmField
    val STATE: DeferredHolder<DataComponentType<*>, DataComponentType<CustomData>> =
        FemtoRegistries.DATA_COMPONENTS.registerComponentType("packed_state") { b ->
            b.persistent(CustomData.CODEC).networkSynchronized(CustomData.STREAM_CODEC)
        }

    /**
     * The machine item of [multiblock] carrying [state] (a plain item when the state is empty).
     */
    @JvmStatic
    fun pack(multiblock: FrameMultiblock, state: PackedState, registries: HolderLookup.Provider): ItemStack {
        val item = multiblock.packedItem?.invoke() ?: return ItemStack.EMPTY
        val stack = ItemStack(item)
        if (!state.isPackedEmpty()) {
            val output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries)
            state.serialize(output)
            stack.set(STATE.get(), CustomData.of(output.buildResult()))
        }
        return stack
    }

    /** Drops the machine item for a broken [multiblock] at its [anchor]. */
    @JvmStatic
    fun drop(level: ILevel, anchor: BlockPos, multiblock: FrameMultiblock, state: PackedState) {
        val mc = level.toMinecraft()
        val stack = pack(multiblock, state, mc.registryAccess())
        if (!stack.isEmpty) Block.popResource(mc, anchor, stack)
    }

    /** Puts what [stack] carries into the formed [multiblock] at [anchor]. */
    @JvmStatic
    fun restore(level: Level, multiblock: FrameMultiblock, anchor: BlockPos, stack: ItemStack) {
        val data = stack.get(STATE.get()) ?: return
        val home = level.getBlockEntity(anchor) as? FrameMachineBlockEntity<*> ?: return
        val state = home.state() ?: return
        state.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), data.copyTag()))
        home.setChanged()
    }

    /** A state made only to read [stack]'s contents (for a tooltip), or a fresh one. */
    @JvmStatic
    fun read(multiblock: FrameMultiblock, stack: ItemStack, registries: HolderLookup.Provider?): PackedState? {
        val state = multiblock.newState() as? PackedState ?: return null
        val data = stack.get(STATE.get())
        if (data != null && registries != null) state.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, registries, data.copyTag()))
        return state
    }
}

/**
 * A storage multiblock as one item: using it where a frame would go builds the whole machine at once, if there is room,
 * with the contents it carries. Dropped by the machine when it is broken or wrenched ([PackedState]).
 */
class PackedMultiblockItem(private val multiblock: () -> FrameMultiblock, properties: Properties) : Item(properties) {
    override fun useOn(context: UseOnContext): InteractionResult {
        val level = context.level
        val multi = multiblock()
        val clicked = context.clickedPos
        val anchor = if (level.getBlockState(clicked).canBeReplaced(BlockPlaceContext(context))) clicked else clicked.relative(context.clickedFace)
        if (!multi.canPlaceAt(level, anchor)) return InteractionResult.FAIL
        if (level.isClientSide) return InteractionResult.SUCCESS
        val stack = context.itemInHand
        multi.formAt(level, anchor)
        PackedMultiblocks.restore(level, multi, anchor, stack)
        if (context.player?.abilities?.instabuild != true) stack.shrink(1)
        level.playSound(null, anchor, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1f, 0.8f)
        return InteractionResult.SUCCESS
    }

    override fun appendHoverText(stack: ItemStack, context: TooltipContext, display: TooltipDisplay, builder: Consumer<Component>, flag: TooltipFlag) {
        val multi = multiblock()
        val (x, y, z) = multi.size
        builder.accept(Component.translatable("tooltip.femtocraft.packed.size", x, y, z).withStyle(ChatFormatting.GRAY))
        PackedMultiblocks.read(multi, stack, context.registries())?.describe()?.forEach { builder.accept(it.copy().withStyle(ChatFormatting.GRAY)) }
    }
}
