package com.itszuvalex.femtocraft.core

import com.itszuvalex.itszulib.api.adapters.IBlockEntity
import com.itszuvalex.itszulib.api.adapters.IModule
import com.itszuvalex.itszulib.core.frag.BlockEntityFragment
import net.minecraft.core.Direction

/**
 * Exposes an object the block entity already owns as a module, on every side. No state of its own.
 */
class FragExpose<T : Any>(private val name: String, private val module: IModule<T>, private val value: () -> T?) :
    BlockEntityFragment<T>() {
    override fun name(): String = name
    override fun module(): IModule<T> = module
    override fun faceToModuleMapper(be: IBlockEntity): (Direction?) -> T? = { value() }
}
