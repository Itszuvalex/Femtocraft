package com.itszuvalex.femtocraft.core

import com.itszuvalex.itszulib.api.utility.NBTSerializationScope
import com.itszuvalex.itszulib.core.frag.InternalBlockEntityFragment
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

/**
 * Multiblock membership of one block entity: whether it is part of a formed multiblock and where the controller is.
 * Port of ItszuLib 1.7.10's `MultiBlockInfo` / `MultiBlockComponent`, same keys (`isFormed`, `c_x`, `c_y`, `c_z`).
 * Saved and synced to clients.
 */
class FragMultiBlock(private val selfPos: () -> BlockPos) : InternalBlockEntityFragment() {
    var isFormed = false
        private set

    /**
     * Controller position; meaningful only while [isFormed].
     */
    var controllerPos: BlockPos = BlockPos.ZERO
        private set

    val isController: Boolean get() = isFormed && controllerPos == selfPos()

    /**
     * @return True if this block now belongs to the multiblock controlled at [controller]. Fails if it already belongs
     * to a different one.
     */
    fun form(controller: BlockPos): Boolean {
        if (isFormed && controllerPos != controller) return false
        isFormed = true
        controllerPos = controller.immutable()
        markDirtyAndSync()
        return true
    }

    /**
     * @return True if this block left the multiblock controlled at [controller] (or was in none).
     */
    fun breakFrom(controller: BlockPos): Boolean {
        if (isFormed && controllerPos != controller) return false
        isFormed = false
        markDirtyAndSync()
        return true
    }

    /**
     * The controller's block entity, if formed and loaded.
     */
    inline fun <reified T : BlockEntity> controller(level: Level?): T? {
        if (!isFormed || level == null) return null
        return level.getBlockEntity(controllerPos) as? T
    }

    override fun name(): String = NAME

    override fun handlesScope(scope: NBTSerializationScope): Boolean = scope != NBTSerializationScope.ITEM

    override fun serializeTo(scope: NBTSerializationScope, output: ValueOutput) {
        output.putBoolean(FORMED_KEY, isFormed)
        output.putInt(X_KEY, controllerPos.x)
        output.putInt(Y_KEY, controllerPos.y)
        output.putInt(Z_KEY, controllerPos.z)
    }

    override fun deserialize(input: ValueInput, scope: NBTSerializationScope) {
        isFormed = input.getBooleanOr(FORMED_KEY, false)
        controllerPos = BlockPos(input.getIntOr(X_KEY, 0), input.getIntOr(Y_KEY, 0), input.getIntOr(Z_KEY, 0))
    }

    companion object {
        const val NAME = "MultiBlock"
        const val FORMED_KEY = "isFormed"
        const val X_KEY = "c_x"
        const val Y_KEY = "c_y"
        const val Z_KEY = "c_z"
    }
}
