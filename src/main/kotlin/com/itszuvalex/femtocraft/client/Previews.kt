package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.industry.FrameItem
import com.itszuvalex.femtocraft.industry.FrameMultiblocks
import com.itszuvalex.femtocraft.industry.ShiftItem
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent
import org.joml.Vector3f

/**
 * Previews of where held items act (v3's `IPreviewable` items): a frame item outlines the multiblock it would place
 * where the player is looking, green where it fits and red where it does not (v3 `FramePreviewableRenderer`); a shift
 * device outlines where it would put the player (v3 `ItemShiftTest`'s previewable).
 */
object Previews {
    private const val FITS = 0xFF40FF40.toInt()
    private const val BLOCKED = 0xFFFF4040.toInt()
    private const val INNER_ALPHA = 0x60000000
    private const val SHIFT = 0xFF40E0FF.toInt()

    fun submit(event: SubmitCustomGeometryEvent) {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return
        val level = mc.level ?: return
        val camera = event.levelRenderState.cameraRenderState.pos
        val held = listOf(player.mainHandItem, player.offhandItem)

        held.firstOrNull { it.item is FrameItem }?.let { stack ->
            if (player.isShiftKeyDown) return@let
            val multi = FrameMultiblocks.get(FrameItem.selection(stack)) ?: return@let
            val hit = mc.hitResult as? BlockHitResult ?: return@let
            if (hit.type != HitResult.Type.BLOCK) return@let
            val anchor = if (level.getBlockState(hit.blockPos).canBeReplaced()) hit.blockPos else hit.blockPos.relative(hit.direction)
            val color = if (multi.canPlaceAt(level, anchor) && hasFrames(player, stack, multi.numFrames)) FITS else BLOCKED
            val (x, y, z) = multi.size
            val boxes = ArrayList<Pair<AABB, Int>>()
            boxes += AABB(anchor).expandTowards(x - 1.0, y - 1.0, z - 1.0) to color
            multi.takenLocations(anchor).forEach { boxes += AABB(it).deflate(.05) to ((color and 0xFFFFFF) or INNER_ALPHA) }
            lines(event, camera, boxes)
            return
        }

        held.firstOrNull { it.item is ShiftItem }?.let {
            val dest = ShiftItem.destination(level, player) ?: return@let
            val w = player.bbWidth / 2.0
            val box = AABB(dest.x + .5 - w, dest.y.toDouble(), dest.z + .5 - w, dest.x + .5 + w, dest.y + player.bbHeight.toDouble(), dest.z + .5 + w)
            lines(event, camera, listOf(box to SHIFT))
        }
    }

    private fun hasFrames(player: net.minecraft.world.entity.player.Player, stack: ItemStack, needed: Int) =
        player.abilities.instabuild || stack.count >= needed

    private fun lines(event: SubmitCustomGeometryEvent, camera: Vec3, boxes: List<Pair<AABB, Int>>) {
        event.submitNodeCollector.submitCustomGeometry(event.poseStack, RenderTypes.lines()) { pose, buffer ->
            for ((box, color) in boxes) {
                val b = box.move(-camera.x, -camera.y, -camera.z)
                for ((from, to) in edges(b)) {
                    val normal = Vector3f((to.x - from.x).toFloat(), (to.y - from.y).toFloat(), (to.z - from.z).toFloat()).normalize()
                    buffer.addVertex(pose, from.x.toFloat(), from.y.toFloat(), from.z.toFloat()).setColor(color).setNormal(pose, normal).setLineWidth(2f)
                    buffer.addVertex(pose, to.x.toFloat(), to.y.toFloat(), to.z.toFloat()).setColor(color).setNormal(pose, normal).setLineWidth(2f)
                }
            }
        }
    }

    /**
     * The twelve edges of [b].
     */
    private fun edges(b: AABB): List<Pair<Vec3, Vec3>> {
        val c = listOf(
            Vec3(b.minX, b.minY, b.minZ), Vec3(b.maxX, b.minY, b.minZ), Vec3(b.maxX, b.minY, b.maxZ), Vec3(b.minX, b.minY, b.maxZ),
            Vec3(b.minX, b.maxY, b.minZ), Vec3(b.maxX, b.maxY, b.minZ), Vec3(b.maxX, b.maxY, b.maxZ), Vec3(b.minX, b.maxY, b.maxZ),
        )
        return listOf(0 to 1, 1 to 2, 2 to 3, 3 to 0, 4 to 5, 5 to 6, 6 to 7, 7 to 4, 0 to 4, 1 to 5, 2 to 6, 3 to 7).map { (a, z) -> c[a] to c[z] }
    }
}
