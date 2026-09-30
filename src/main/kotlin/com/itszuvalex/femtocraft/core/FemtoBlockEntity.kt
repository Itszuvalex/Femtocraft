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
 * Femtocraft's block entity base: a fragment-composed [TickableBlockEntityCore] with the lifecycle hooks the 1.7.10
 * `TileEntityBase` offered (split server/client updates, activation, placement), mapped onto 26.1.
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
        if (level.isClientSide()) clientTick() else serverTick()
    }

    /**
     * Called every tick on the server.
     */
    open fun serverTick() {}

    /**
     * Called every tick on the client.
     */
    open fun clientTick() {}

    /**
     * Right-click with an empty hand (or an item the block does not handle).
     */
    open fun onUse(player: Player): InteractionResult = InteractionResult.PASS

    /**
     * Placed by an entity, after the block entity exists.
     */
    open fun onPlaced(placer: LivingEntity?, stack: ItemStack) {}

    override fun onLoad() {
        super.onLoad()
        if (isServer) onServerLoad()
    }

    /**
     * Server side, when this block entity is added to a loaded level (placement or chunk load). Register with
     * server-side managers here.
     */
    open fun onServerLoad() {}

    override fun setRemoved() {
        if (isServer) onServerUnload()
        super.setRemoved()
    }

    /**
     * Server side, when this block entity leaves the level (broken or chunk unloaded). Unregister here.
     */
    open fun onServerUnload() {}

    /**
     * Saves, and on the server re-sends client (DESCRIPTION) data.
     */
    fun sync() = markDirtyAndSync()
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
