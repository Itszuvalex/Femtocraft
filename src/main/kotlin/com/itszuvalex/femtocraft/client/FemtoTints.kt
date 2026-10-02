package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.core.FragDerivedColor
import com.itszuvalex.femtocraft.industry.IndustryContent
import com.itszuvalex.femtocraft.logistics.LogisticsContent
import com.itszuvalex.femtocraft.power.PowerContent
import com.itszuvalex.femtocraft.worldgen.CrystalClusterBlockEntity
import com.itszuvalex.femtocraft.worldgen.WorldgenContent
import com.itszuvalex.itszulib.api.Modules
import com.itszuvalex.itszulib.api.adapters.IBlockEntity
import net.minecraft.client.color.block.BlockTintSource
import net.minecraft.client.renderer.block.BlockAndTintGetter
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent

/**
 * Block tints for the OBJ models' tinted materials (tint index 0, see tools/gen_obj.py): v3 drew those layers in the
 * block entity's color. The defaults are what items and unknown colors use, and match the item models' tints
 * (tools/gen_assets.py ITEM_TINTS).
 */
object FemtoTints {
    const val CLUSTER_DEFAULT = 0xFF73E6FF.toInt()
    const val POWER_CONDUIT_DEFAULT = 0xFF33CCFF.toInt()
    const val LOGISTICS_CONDUIT_DEFAULT = 0xFFFFB040.toInt()
    const val CHAMBER_DEFAULT = 0xFF60E060.toInt()

    fun register(event: RegisterColorHandlersEvent.BlockTintSources) {
        event.register(listOf(ClusterTint), WorldgenContent.CRYSTAL_CLUSTER.get())
        event.register(listOf(ColorableTint(POWER_CONDUIT_DEFAULT)), PowerContent.POWER_CONDUIT.get())
        event.register(listOf(ColorableTint(LOGISTICS_CONDUIT_DEFAULT)), LogisticsContent.CONDUIT.get())
        event.register(listOf(ColorableTint(CHAMBER_DEFAULT)), IndustryContent.GERMINATION_CHAMBER.get())
    }

    /**
     * The cluster's own color (v3 `TileCrystalsWorldgen.color`).
     */
    private object ClusterTint : BlockTintSource {
        override fun color(state: BlockState): Int = CLUSTER_DEFAULT

        override fun colorInWorld(state: BlockState, level: BlockAndTintGetter, pos: BlockPos): Int =
            (level.getBlockEntity(pos) as? CrystalClusterBlockEntity)?.color?.let { it or 0xFF000000.toInt() } ?: CLUSTER_DEFAULT
    }

    /**
     * The block entity's [Modules.COLORABLE] color; [default] without one, or for v3's "no color" (opaque black).
     */
    private class ColorableTint(private val default: Int) : BlockTintSource {
        override fun color(state: BlockState): Int = default

        override fun colorInWorld(state: BlockState, level: BlockAndTintGetter, pos: BlockPos): Int {
            val be = level.getBlockEntity(pos) as? IBlockEntity ?: return default
            val color = be.getModule(Modules.COLORABLE, null)?.getColor() ?: return default
            return if (color == FragDerivedColor.NONE) default else color.toInt() or 0xFF000000.toInt()
        }
    }
}
