package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.core.FragDerivedColor
import com.itszuvalex.femtocraft.industry.FrameBlockEntity
import com.itszuvalex.femtocraft.industry.FrameMultiblock
import com.itszuvalex.femtocraft.industry.GerminationChamberBlockEntity
import com.itszuvalex.femtocraft.industry.IndustryContent
import com.itszuvalex.femtocraft.power.CrystalMountBlock
import com.itszuvalex.femtocraft.power.CrystalMountBlockEntity
import com.itszuvalex.femtocraft.power.PowerContent
import com.itszuvalex.femtocraft.worldgen.CrystalClusterBlockEntity
import com.itszuvalex.femtocraft.worldgen.WorldgenContent
import com.itszuvalex.itszulib.api.Modules
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState
import net.minecraft.client.renderer.feature.ModelFeatureRenderer
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import org.joml.Quaternionf
import org.joml.Vector3f

/**
 * Block entity renderers for the moving parts of v3's OBJ models (PORTING, follow-up rendering, pass 2). Each draws
 * [ObjParts] with a transform per frame; the static parts are block models. Ports of v3's `CrystalMountRenderer`,
 * `CrystalRenderer`, `MultiblockGerminationChamberRenderer` (sprinklers) and `FrameRenderer` (edges).
 */
object FemtoRenderers {
    fun register(event: EntityRenderersEvent.RegisterRenderers) {
        event.registerBlockEntityRenderer(PowerContent.CRYSTAL_MOUNT_BE.get()) { CrystalMountRenderer() }
        event.registerBlockEntityRenderer(WorldgenContent.CRYSTAL_CLUSTER_BE.get()) { CrystalClusterRenderer() }
        event.registerBlockEntityRenderer(IndustryContent.GERMINATION_CHAMBER_BE.get()) { GerminationChamberRenderer() }
        event.registerBlockEntityRenderer(IndustryContent.FRAME_BE.get()) { FrameRenderer() }
    }

    /**
     * World time in ticks, with the partial tick, as v3's renderers used it.
     */
    fun time(be: BlockEntity, partialTicks: Float): Float = ((be.level?.gameTime ?: 0L) % 24000L).toFloat() + partialTicks

    /**
     * Draws [part] (if baked) with [tint] for tint index 0.
     */
    fun draw(poseStack: PoseStack, collector: SubmitNodeCollector, part: String, tint: Int, light: Int) {
        val model = ObjParts.get(part) ?: return
        collector.submitMultiLayerBlockModel(poseStack, listOf(model), true, intArrayOf(tint), light, OverlayTexture.NO_OVERLAY, 0)
    }

    /**
     * Rotates [poseStack] by [degrees] about [axis] through [pivot] (block coordinates).
     */
    fun rotateAbout(poseStack: PoseStack, pivot: Vector3f, axis: Vector3f, degrees: Float) {
        poseStack.translate(pivot.x, pivot.y, pivot.z)
        poseStack.mulPose(Quaternionf().rotateAxis(Mth.DEG_TO_RAD * degrees, Vector3f(axis).normalize()))
        poseStack.translate(-pivot.x, -pivot.y, -pivot.z)
    }

    private const val OPAQUE = 0xFF000000.toInt()

    // --- Crystal mount ---------------------------------------------------------------------------------------------

    class CrystalMountState : BlockEntityRenderState() {
        var top = false
        var bottom = true
        var rotation = 0f
        var crystalColor: Int? = null
        var time = 0f

        /**
         * Power beam targets (child nodes) and diffusion beam targets (leaves), relative to the mount.
         */
        val beams = ArrayList<Vec3>()
        val leaves = ArrayList<Vec3>()
    }

    /**
     * The grips turn one degree a tick about the mount's axis, holding the crystal in its color (fullbright, as v3 drew
     * it without the light map). Without a crystal, the crystal's shape tumbles and pulses in the end portal's starfield
     * (v3 drew it with its portal shader).
     */
    class CrystalMountRenderer : BlockEntityRenderer<CrystalMountBlockEntity, CrystalMountState> {
        override fun createRenderState() = CrystalMountState()

        override fun extractRenderState(
            be: CrystalMountBlockEntity, state: CrystalMountState, partialTicks: Float, cameraPosition: Vec3,
            breakProgress: ModelFeatureRenderer.CrumblingOverlay?,
        ) {
            super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress)
            val blockState = be.blockState
            state.top = blockState.hasProperty(CrystalMountBlock.TOP) && blockState.getValue(CrystalMountBlock.TOP)
            state.bottom = !blockState.hasProperty(CrystalMountBlock.BOTTOM) || blockState.getValue(CrystalMountBlock.BOTTOM)
            state.rotation = time(be, partialTicks)
            state.time = state.rotation
            state.crystalColor = be.crystal()?.color?.let { it or OPAQUE }
            val origin = Vec3.atLowerCornerOf(be.blockPos)
            state.beams.clear()
            be.node.renderLocations.forEach { state.beams += Vec3.atLowerCornerOf(it.pos).subtract(origin) }
            state.leaves.clear()
            be.node.leafLocs().forEach { state.leaves += Vec3.atLowerCornerOf(it.pos).subtract(origin) }
        }

        override fun submit(state: CrystalMountState, poseStack: PoseStack, collector: SubmitNodeCollector, camera: CameraRenderState) {
            poseStack.pushPose()
            rotateAbout(poseStack, Vector3f(.5f, 0f, .5f), Vector3f(0f, 1f, 0f), state.rotation)
            if (state.bottom || !state.top) draw(poseStack, collector, ObjParts.MOUNT_BOTTOM_GRIP, -1, state.lightCoords)
            if (state.top) draw(poseStack, collector, ObjParts.MOUNT_TOP_GRIP, -1, state.lightCoords)
            val crystalColor = state.crystalColor
            if (crystalColor != null) draw(poseStack, collector, ObjParts.MOUNT_CRYSTAL, crystalColor, state.lightCoords)
            else portalCrystal(poseStack, collector, state.rotation)
            poseStack.popPose()
            val color = state.crystalColor ?: BEAM_OUTER_COLOR
            for (target in state.beams) {
                Beams.submit(poseStack, collector, camera, state.blockPos, target, state.time, POWER_BEAM_OUTER, BEAM_OUTER_COLOR)
                Beams.submit(poseStack, collector, camera, state.blockPos, target, state.time, POWER_BEAM_COLORED, color)
            }
            for (target in state.leaves) {
                Beams.submit(poseStack, collector, camera, state.blockPos, target, state.time, DIFFUSION_BEAM, (color and 0xFFFFFF) or (64 shl 24))
            }
        }

        /**
         * v3: about the crystal's middle, turned [rotation] degrees about (.25, 2, 3), squashed to half height and pulsing
         * between 95% and 110%.
         */
        private fun portalCrystal(poseStack: PoseStack, collector: SubmitNodeCollector, rotation: Float) {
            val part = ObjParts.get(ObjParts.MOUNT_CRYSTAL) ?: return
            val quads = (listOf<net.minecraft.core.Direction?>(null) + net.minecraft.core.Direction.entries).flatMap { part.getQuads(it) }
            val scale = kotlin.math.abs(Mth.sin(rotation / 10.0)) * .15f + .95f
            poseStack.pushPose()
            poseStack.translate(.5f, .5f, .5f)
            poseStack.mulPose(Quaternionf().rotateAxis(Mth.DEG_TO_RAD * rotation, Vector3f(.25f, 2f, 3f).normalize()))
            poseStack.scale(scale, .5f * scale, scale)
            poseStack.translate(-.5f, -.5f, -.5f)
            collector.submitCustomGeometry(poseStack, net.minecraft.client.renderer.rendertype.RenderTypes.endPortal()) { pose, buffer ->
                for (quad in quads) for (i in 0..3) buffer.addVertex(pose, Vector3f(quad.position(i)))
            }
            poseStack.popPose()
        }

        override fun shouldRenderOffScreen(): Boolean = true

        override fun getRenderBoundingBox(be: CrystalMountBlockEntity): AABB = AABB(be.blockPos).inflate(CrystalMountBlockEntity.RANGE.toDouble() + 1)

        companion object {
            private val POWER_BEAM_OUTER = net.minecraft.resources.Identifier.fromNamespaceAndPath(com.itszuvalex.femtocraft.Femtocraft.ID, "textures/power_beam_outer.png")
            private val POWER_BEAM_COLORED = net.minecraft.resources.Identifier.fromNamespaceAndPath(com.itszuvalex.femtocraft.Femtocraft.ID, "textures/power_beam_colored.png")
            private val DIFFUSION_BEAM = net.minecraft.resources.Identifier.fromNamespaceAndPath(com.itszuvalex.femtocraft.Femtocraft.ID, "textures/diffusion_particles_colored.png")

            /**
             * v3's outer layer color, (180, 255, 255).
             */
            private const val BEAM_OUTER_COLOR = 0xFFB4FFFF.toInt()
        }
    }

    /**
     * v3's wireless beams (`WirelessPowerBeamRenderer`, `FemtoRenderUtils.drawBeam`): a textured quad from block centre
     * to block centre, turned to face the camera, its texture scrolling along it; fullbright and translucent.
     */
    object Beams {
        const val WIDTH = .1f

        /**
         * Draws a beam from the centre of the block at [origin] to the centre of the block at [origin] + [target].
         */
        fun submit(
            poseStack: PoseStack, collector: SubmitNodeCollector, camera: CameraRenderState, origin: BlockPos, target: Vec3,
            time: Float, texture: net.minecraft.resources.Identifier, color: Int,
        ) {
            val start = Vec3(.5, .5, .5)
            val end = target.add(.5, .5, .5)
            val dir = end.subtract(start)
            val length = dir.length()
            if (length < 1e-3) return
            val centre = Vec3.atLowerCornerOf(origin).add(start).add(dir.scale(.5))
            val side = camera.pos.subtract(centre).cross(dir).normalize().scale(WIDTH.toDouble())
            if (side.lengthSqr() < 1e-8) return
            val scroll = -time * .2f - Mth.floor(-time * .1f).toFloat()
            val vMin = (-1f + scroll) % 1f
            val vMax = (length * (1 / (2 * WIDTH))).toFloat() + vMin
            val corners = listOf(start.subtract(side), end.subtract(side), end.add(side), start.add(side))
            val uvs = listOf(0f to vMin, 0f to vMax, 1f to vMax, 1f to vMin)
            collector.submitCustomGeometry(poseStack, net.minecraft.client.renderer.rendertype.RenderTypes.beaconBeam(texture, true)) { pose, buffer ->
                // Both windings, so the beam shows from either side.
                for (order in listOf(listOf(0, 1, 2, 3), listOf(3, 2, 1, 0))) {
                    for (i in order) {
                        val c = corners[i]
                        buffer.addVertex(pose, c.x.toFloat(), c.y.toFloat(), c.z.toFloat())
                            .setColor(color)
                            .setUv(uvs[i].first, uvs[i].second)
                            .setOverlay(OverlayTexture.NO_OVERLAY)
                            .setLight(0xF000F0)
                            .setNormal(pose, 0f, 1f, 0f)
                    }
                }
            }
        }
    }

    // --- Crystal cluster -------------------------------------------------------------------------------------------

    class CrystalClusterState : BlockEntityRenderState() {
        var time = 0f
        var color = FemtoTints.CLUSTER_DEFAULT
        var seed = 0
    }

    /**
     * Each crystal bobs on its own phase and the first one turns; each is tinted the cluster's color plus a small
     * offset of its own (v3 kept random offsets per cluster; here they come from the position, so they need no sync).
     */
    class CrystalClusterRenderer : BlockEntityRenderer<CrystalClusterBlockEntity, CrystalClusterState> {
        override fun createRenderState() = CrystalClusterState()

        override fun extractRenderState(
            be: CrystalClusterBlockEntity, state: CrystalClusterState, partialTicks: Float, cameraPosition: Vec3,
            breakProgress: ModelFeatureRenderer.CrumblingOverlay?,
        ) {
            super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress)
            state.time = time(be, partialTicks)
            state.color = be.color or OPAQUE
            state.seed = be.blockPos.hashCode()
        }

        override fun submit(state: CrystalClusterState, poseStack: PoseStack, collector: SubmitNodeCollector, camera: CameraRenderState) {
            val pos = state.blockPos
            for (i in 1..10) {
                val offset = (i * 97) % 10
                val dir = if (i % 2 == 0) -1 else 1
                // v3: 4 units at 1/100 scale.
                val height = Mth.sin((state.time + offset + pos.x + pos.y + pos.z) * .1) * .04f * dir
                poseStack.pushPose()
                poseStack.translate(0f, height, 0f)
                if (i == 1) rotateAbout(poseStack, Vector3f(.5f, 0f, .5f), Vector3f(0f, 1f, 0f), state.time)
                draw(poseStack, collector, ObjParts.clusterCrystal(i), shifted(state.color, state.seed * 31 + i), state.lightCoords)
                poseStack.popPose()
            }
        }

        /**
         * [color] with each channel moved by -15..+15, from [seed] (v3: `color + offset - 15`, offsets 0..30).
         */
        private fun shifted(color: Int, seed: Int): Int {
            var h = seed * -0x61c88647
            fun channel(shift: Int): Int {
                h = h xor (h ushr 15)
                h *= 0x2c1b3c6d
                val delta = Math.floorMod(h ushr 8, 31) - 15
                return Mth.clamp(((color shr shift) and 255) + delta, 0, 255) shl shift
            }
            return OPAQUE or channel(16) or channel(8) or channel(0)
        }
    }

    // --- Germination chamber ---------------------------------------------------------------------------------------

    class ChamberState : BlockEntityRenderState() {
        var home = false
        var time = 0f
        var color = FemtoTints.CHAMBER_DEFAULT
    }

    /**
     * The three sprinklers under the chamber's top swing back and forth, each about its own hinge (v3's
     * `renderSprinklers`; v3 skipped them with particles off). Only the home block draws, for the whole chamber.
     */
    class GerminationChamberRenderer : BlockEntityRenderer<GerminationChamberBlockEntity, ChamberState> {
        override fun createRenderState() = ChamberState()

        override fun extractRenderState(
            be: GerminationChamberBlockEntity, state: ChamberState, partialTicks: Float, cameraPosition: Vec3,
            breakProgress: ModelFeatureRenderer.CrumblingOverlay?,
        ) {
            super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress)
            val blockState = be.blockState
            state.home = blockState.hasProperty(FrameMultiblock.HOME) && blockState.getValue(FrameMultiblock.HOME)
            state.time = time(be, partialTicks)
            val color = be.getModule(Modules.COLORABLE, null)?.getColor()
            state.color = if (color == null || color == FragDerivedColor.NONE) FemtoTints.CHAMBER_DEFAULT else color.toInt() or OPAQUE
        }

        override fun submit(state: ChamberState, poseStack: PoseStack, collector: SubmitNodeCollector, camera: CameraRenderState) {
            if (!state.home) return
            val t = state.time * .05
            // Hinges and axes relative to the chamber's centre (1, _, 1), as in v3.
            sprinkler(poseStack, collector, state, 1, Vector3f(1f, 2.9f, 1.6f), Vector3f(1f, 0f, 0f), (1 + Mth.sin(t)) * 20)
            sprinkler(poseStack, collector, state, 2, Vector3f(1 - .5196f, 2.9f, .7f), Vector3f(-.57735f, 0f, 1f), (1 + Mth.sin(t + 1)) * 20)
            sprinkler(poseStack, collector, state, 3, Vector3f(1 + .5196f, 2.9f, .7f), Vector3f(.57735f, 0f, 1f), -(1 + Mth.sin(t + 2)) * 20)
        }

        private fun sprinkler(poseStack: PoseStack, collector: SubmitNodeCollector, state: ChamberState, i: Int, hinge: Vector3f, axis: Vector3f, degrees: Float) {
            poseStack.pushPose()
            rotateAbout(poseStack, hinge, axis, degrees)
            draw(poseStack, collector, ObjParts.sprinkler(i), state.color, state.lightCoords)
            poseStack.popPose()
        }

        override fun getRenderBoundingBox(be: GerminationChamberBlockEntity): AABB =
            AABB(be.blockPos).expandTowards(1.0, 2.0, 1.0)
    }

    // --- Frame -----------------------------------------------------------------------------------------------------

    class FrameState : BlockEntityRenderState() {
        /**
         * Which [ObjParts.FRAME_GROUPS] to draw.
         */
        val groups = BooleanArray(ObjParts.FRAME_GROUPS.size)
    }

    /**
     * Draws the edges of the frame structure's bounding box (v3's render marks): an edge where both of its sides are on
     * the box, a corner where at least two of its three sides are, so long edges stay continuous across blocks. A frame
     * outside a structure draws every edge.
     */
    class FrameRenderer : BlockEntityRenderer<FrameBlockEntity, FrameState> {
        override fun createRenderState() = FrameState()

        override fun extractRenderState(
            be: FrameBlockEntity, state: FrameState, partialTicks: Float, cameraPosition: Vec3,
            breakProgress: ModelFeatureRenderer.CrumblingOverlay?,
        ) {
            super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress)
            val membership = be.part.membership
            if (membership == null) {
                state.groups.fill(true)
                return
            }
            val o = membership.offset
            val max = membership.shape.slots.keys.fold(BlockPos.ZERO) { m, p -> BlockPos(maxOf(m.x, p.x), maxOf(m.y, p.y), maxOf(m.z, p.z)) }
            val sides = mapOf('B' to (o.y == 0), 'T' to (o.y == max.y), 'N' to (o.z == 0), 'S' to (o.z == max.z), 'W' to (o.x == 0), 'E' to (o.x == max.x))
            ObjParts.FRAME_GROUPS.forEachIndexed { i, group ->
                val onBox = group.count { sides.getValue(it) }
                state.groups[i] = if (group.length == 2) onBox == 2 else onBox >= 2
            }
        }

        override fun submit(state: FrameState, poseStack: PoseStack, collector: SubmitNodeCollector, camera: CameraRenderState) {
            ObjParts.FRAME_GROUPS.forEachIndexed { i, group ->
                if (state.groups[i]) draw(poseStack, collector, ObjParts.frame(group), -1, state.lightCoords)
            }
        }
    }
}
