package com.itszuvalex.femtocraft.core

import com.itszuvalex.itszulib.api.Modules
import com.itszuvalex.itszulib.api.adapters.IBlockEntity
import com.itszuvalex.itszulib.api.adapters.IColorable
import com.itszuvalex.itszulib.api.adapters.IModule
import com.itszuvalex.itszulib.core.frag.BlockEntityFragment
import com.itszuvalex.itszulib.util.Color
import net.minecraft.core.Direction
import net.minecraft.world.level.Level

/**
 * Exposes an object the block entity already owns as a module, on every side. No state of its own.
 */
class FragExpose<T : Any>(private val name: String, private val module: IModule<T>, private val value: () -> T?) :
    BlockEntityFragment<T>() {
    override fun name(): String = name
    override fun module(): IModule<T> = module
    override fun faceToModuleMapper(be: IBlockEntity): (Direction?) -> T? = { value() }
}

/**
 * A read-only color, worked out on demand (e.g. from the block's crystal or its power parent). Port of v3's
 * `ModuleColorableFrom*` modules. Setting it does nothing.
 */
class FragDerivedColor(private val color: () -> Color) : BlockEntityFragment<IColorable>(), IColorable {
    override fun name(): String = "Color"
    override fun module(): IModule<IColorable> = Modules.COLORABLE
    override fun faceToModuleMapper(be: IBlockEntity): (Direction?) -> IColorable? = { this }
    override fun getColor(): Color = color()
    override fun setColor(color: Color) {}

    companion object {
        /**
         * v3's color for "no crystal" / "no parent": opaque black.
         */
        @JvmField
        val NONE = Color(255.toByte(), 0, 0, 0)
    }
}

/**
 * The level of the block entity hosting a fragment, if it is in one.
 */
fun IBlockEntity.levelOrNull(): Level? = toMinecraft().level
