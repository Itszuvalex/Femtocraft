package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.Femtocraft
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart
import net.minecraft.resources.Identifier
import net.neoforged.neoforge.client.event.ModelEvent
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey

/**
 * Single OBJ groups baked on their own, for block entity renderers to draw with a transform: the models at
 * models/block/part/<name>.json, written by tools/gen_assets.py (`part_models`). [NAMES] must list them all.
 */
object ObjParts {
    const val MOUNT_CRYSTAL = "crystal_mount_crystal"
    const val MOUNT_BOTTOM_GRIP = "crystal_mount_bottom_grip"
    const val MOUNT_TOP_GRIP = "crystal_mount_top_grip"

    fun sprinkler(i: Int) = "germination_chamber_sprinkler$i"

    fun clusterCrystal(i: Int) = "crystal_cluster_$i"

    /**
     * The frame's groups: four vertical edges, eight horizontal edges and eight corners, named by their sides
     * (B/T, N/S, E/W).
     */
    val FRAME_GROUPS = listOf(
        "NE", "NW", "SW", "SE", "BN", "BS", "TS", "TN", "TE", "BE", "BW", "TW",
        "BNE", "TNE", "TNW", "BNW", "BSW", "TSW", "TSE", "BSE",
    )

    fun frame(group: String) = "frame_${group.lowercase()}"

    val NAMES: List<String> = listOf(MOUNT_CRYSTAL, MOUNT_BOTTOM_GRIP, MOUNT_TOP_GRIP) +
        (1..3).map(::sprinkler) + (1..10).map(::clusterCrystal) + FRAME_GROUPS.map(::frame)

    private val keys = NAMES.associateWith { name -> StandaloneModelKey<BlockStateModelPart> { "${Femtocraft.ID}:part/$name" } }

    fun register(event: ModelEvent.RegisterStandalone) {
        keys.forEach { (name, key) ->
            event.register(key, SimpleUnbakedStandaloneModel.simpleModelWrapper(Identifier.fromNamespaceAndPath(Femtocraft.ID, "block/part/$name")))
        }
    }

    /**
     * The baked part, or null before models load.
     */
    fun get(name: String): BlockStateModelPart? = keys[name]?.let { Minecraft.getInstance().modelManager.getStandaloneModel(it) }
}
