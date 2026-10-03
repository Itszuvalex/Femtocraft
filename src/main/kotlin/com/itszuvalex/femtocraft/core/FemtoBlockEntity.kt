package com.itszuvalex.femtocraft.core

import com.itszuvalex.itszulib.api.adapters.ILevel
import com.itszuvalex.itszulib.api.utility.Loc4
import com.itszuvalex.itszulib.api.utility.NBTSerializationScope
import com.itszuvalex.itszulib.core.TickableBlockEntityCore
import com.itszuvalex.itszulib.core.frag.InternalBlockEntityFragment
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

/**
 * Femtocraft's block entity base: a fragment-composed [TickableBlockEntityCore] with the hooks v3's
 * `TileEntityCoreTickable` offered (split server/client updates, activation, placement), mapped onto 26.1.
 */
abstract class FemtoBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) :
    TickableBlockEntityCore(type, pos, state) {

    /**
     * Location of this block entity. Only valid once it is in a level.
     */
    val loc: Loc4 get() = Loc4.of(level!!, blockPos)

    val isServer: Boolean get() = level?.isClientSide == false

    final override fun tick(level: ILevel, blockPos: BlockPos, blockState: BlockState) {
        super.tick(level, blockPos, blockState)
        if (level.isClientSide()) {
            watchTint()
            clientTick()
        } else {
            serverTick()
        }
    }

    private var lastTint: Int? = null

    /**
     * Block tints are baked into the chunk mesh, so a colour worked out from synced data (a crystal, a power parent)
     * would only show at the section's next rebuild. Twice a second, if this block's colour changed, ask for one.
     */
    private fun watchTint() {
        val level = level ?: return
        if ((level.gameTime + blockPos.hashCode()) % TINT_CHECK_TICKS != 0L) return
        val color = getModule(com.itszuvalex.itszulib.api.Modules.COLORABLE, null)?.getColor()?.toInt() ?: return
        val last = lastTint
        lastTint = color
        if (last != null && last != color) level.sendBlockUpdated(blockPos, blockState, blockState, net.minecraft.world.level.block.Block.UPDATE_CLIENTS)
    }

    /**
     * Called every tick on the server, after the fragments' ticks.
     */
    open fun serverTick() {}

    /**
     * Called every tick on the client, after the fragments' ticks.
     */
    open fun clientTick() {}

    /**
     * Right-click with an empty hand, when the block has no menu.
     */
    open fun onUse(player: Player): InteractionResult = InteractionResult.PASS

    /**
     * Placed by an entity, after the block entity exists.
     */
    open fun onPlaced(placer: LivingEntity?, stack: ItemStack) {}

    /**
     * Saves, and on the server re-sends client (DESCRIPTION) data.
     */
    fun sync() = markDirtyAndSync()

    companion object {
        const val TINT_CHECK_TICKS = 10
    }
}

/**
 * A fragment defined by lambdas, for block-entity-level state that does not warrant its own fragment class.
 */
class FragData(
    private val name: String,
    private val scopes: Set<NBTSerializationScope>,
    private val write: (NBTSerializationScope, ValueOutput) -> Unit,
    private val read: (NBTSerializationScope, ValueInput) -> Unit,
) : InternalBlockEntityFragment() {
    override fun name(): String = name
    override fun handlesScope(scope: NBTSerializationScope): Boolean = scope in scopes
    override fun serializeTo(scope: NBTSerializationScope, output: ValueOutput) = write(scope, output)
    override fun deserialize(input: ValueInput, scope: NBTSerializationScope) = read(scope, input)

    companion object {
        @JvmField
        val LEVEL = setOf(NBTSerializationScope.LEVEL)

        @JvmField
        val LEVEL_AND_DESCRIPTION = setOf(NBTSerializationScope.LEVEL, NBTSerializationScope.DESCRIPTION)
    }
}
