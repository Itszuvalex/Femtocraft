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
        event.registerBlockEntityRenderer(com.itszuvalex.femtocraft.logistics.LogisticsContent.FLUID_RESERVOIR_BE.get()) { ReservoirRenderer() }
        event.registerBlockEntityRenderer(com.itszuvalex.femtocraft.logistics.LogisticsContent.CONDUIT_BE.get()) { ConduitChipRenderer() }
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

    /** One line of a frame's needs list: the item, its (possibly scrolled) name, and have/need. */
    class NeedLine(
        val item: net.minecraft.client.renderer.item.ItemStackRenderState,
        val name: net.minecraft.util.FormattedCharSequence,
        val count: net.minecraft.util.FormattedCharSequence,
        val countWidth: Int,
    )

    class FrameState : BlockEntityRenderState() {
        /**
         * Which [ObjParts.FRAME_GROUPS] to draw.
         */
        val groups = BooleanArray(ObjParts.FRAME_GROUPS.size)

        /** On a building home frame: the machine's blocks (offset from the anchor, model parts) and how solid. */
        val ghost = ArrayList<Pair<BlockPos, List<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart>>>()
        var ghostAlpha = 0f

        /** On a home frame still collecting: the lines of what it needs that show now. */
        val needs = ArrayList<NeedLine>()

        /** The list's width in text pixels. */
        var needsWidth = 0

        /** Where the list floats, from the anchor: the structure's middle. */
        var needsAt: Vec3 = Vec3.ZERO
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
            state.ghost.clear()
            state.needs.clear()
            val multi = be.multiblock()
            if (membership.isHome && !be.clientBuilding && multi != null && cameraPosition.distanceToSqr(Vec3.atCenterOf(be.blockPos)) < NEEDS_RANGE * NEEDS_RANGE) {
                extractNeeds(be, multi, state, time(be, partialTicks))
            }
            if (membership.isHome && be.clientBuilding && multi != null) {
                val level = be.level as? net.minecraft.client.multiplayer.ClientLevel ?: return
                val models = net.minecraft.client.Minecraft.getInstance().modelManager.blockStateModelSet
                for (offset in multi.shape.slots.keys) {
                    val machine = multi.machineState(offset == BlockPos.ZERO)
                    val parts = ArrayList<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart>()
                    val at = be.blockPos.offset(offset)
                    models.get(machine).collectParts(level, at, machine, net.minecraft.util.RandomSource.create(at.asLong()), parts)
                    if (parts.isNotEmpty()) state.ghost += offset to parts
                }
                // Fades in as it builds, with a slow shimmer: nanites at work.
                val progress = (be.clientProgress + partialTicks) / com.itszuvalex.femtocraft.industry.FrameState.BUILD_TIME
                val shimmer = 0.08f * kotlin.math.sin(time(be, partialTicks) * 0.25f)
                state.ghostAlpha = (GHOST_MIN + (GHOST_MAX - GHOST_MIN) * progress.coerceIn(0f, 1f) + shimmer).coerceIn(0f, 1f)
            }
        }

        /**
         * The floating list: each requirement slot's item, name and `have/need` (red until met, then green), from the
         * counts the home frame syncs. It stays inside the structure: at most as wide as its narrower side and as tall
         * as it is. A name too long for its line scrolls through it a character at a time; with more lines than fit,
         * the list steps through them a line at a time ([time] in ticks).
         */
        private fun extractNeeds(be: FrameBlockEntity, multi: FrameMultiblock, state: FrameState, time: Float) {
            val mc = net.minecraft.client.Minecraft.getInstance()
            val font = mc.font
            val size = multi.size
            val maxWidth = ((minOf(size.first, size.third) - MARGIN) / TEXT_SCALE).toInt()
            val maxLines = maxOf(1, ((size.second - MARGIN) / (LINE * TEXT_SCALE)).toInt())
            val needs = multi.requirementSlots()
            val first = if (needs.size > maxLines) (time / LINE_STEP_TICKS).toInt() % (needs.size - maxLines + 1) else 0
            val shown = needs.indices.drop(first).take(maxLines)
            val counts = shown.associateWith { i ->
                val have = be.clientHave.getOrElse(i) { 0 }
                net.minecraft.network.chat.Component.literal("$have/${needs[i].count}")
                    .withStyle(if (have >= needs[i].count) net.minecraft.ChatFormatting.GREEN else net.minecraft.ChatFormatting.RED)
            }
            val countWidth = counts.values.maxOf { font.width(it) }
            val nameRoom = maxWidth - ICON - GAP - countWidth - GAP
            var widest = 0
            for (i in shown) {
                val need = needs[i]
                val item = net.minecraft.client.renderer.item.ItemStackRenderState()
                mc.itemModelResolver.updateForTopItem(item, need, net.minecraft.world.item.ItemDisplayContext.GUI, be.level, null, i)
                val full = need.hoverName.string
                val name = if (font.width(full) <= nameRoom) full else {
                    val loop = full + MARQUEE_GAP
                    val offset = (time / TICKS_PER_CHAR).toInt() % loop.length
                    font.plainSubstrByWidth((loop + full).substring(offset), nameRoom)
                }
                widest = maxOf(widest, font.width(name))
                val count = counts.getValue(i)
                state.needs += NeedLine(item, net.minecraft.util.FormattedCharSequence.forward(name, net.minecraft.network.chat.Style.EMPTY), count.visualOrderText, font.width(count))
            }
            state.needsWidth = ICON + GAP + widest + GAP + countWidth
            state.needsAt = Vec3(size.first / 2.0, size.second / 2.0, size.third / 2.0)
        }

        override fun submit(state: FrameState, poseStack: PoseStack, collector: SubmitNodeCollector, camera: CameraRenderState) {
            ObjParts.FRAME_GROUPS.forEachIndexed { i, group ->
                if (state.groups[i]) draw(poseStack, collector, ObjParts.frame(group), -1, state.lightCoords)
            }
            if (state.needs.isNotEmpty()) submitNeeds(state, poseStack, collector, camera)
            if (state.ghost.isEmpty()) return
            // The machine taking shape inside the frames: its models, translucent, untinted, full bright.
            val quad = com.mojang.blaze3d.vertex.QuadInstance()
            quad.setColor(((state.ghostAlpha * 255).toInt() shl 24) or 0xFFFFFF)
            quad.setLightCoords(FULL_BRIGHT)
            quad.setOverlayCoords(OverlayTexture.NO_OVERLAY)
            for ((offset, parts) in state.ghost) {
                poseStack.pushPose()
                poseStack.translate(offset.x.toFloat(), offset.y.toFloat(), offset.z.toFloat())
                collector.submitCustomGeometry(poseStack, net.minecraft.client.renderer.rendertype.RenderTypes.translucentMovingBlock()) { pose, buffer ->
                    for (part in parts) for (side in GHOST_SIDES) for (baked in part.getQuads(side)) buffer.putBakedQuad(pose, baked, quad)
                }
                poseStack.popPose()
            }
        }

        /**
         * The list, facing the camera and centred in the structure: a line per requirement, the item icon on the left,
         * its name, and have/need on the right, over a faint background as a name tag's.
         */
        private fun submitNeeds(state: FrameState, poseStack: PoseStack, collector: SubmitNodeCollector, camera: CameraRenderState) {
            poseStack.pushPose()
            poseStack.translate(state.needsAt.x, state.needsAt.y, state.needsAt.z)
            poseStack.mulPose(camera.orientation)
            val left = -state.needsWidth / 2f
            val right = state.needsWidth / 2f
            val top = -state.needs.size * LINE / 2f
            state.needs.forEachIndexed { i, line ->
                val y = top + i * LINE
                // Icons: an item's GUI model is one unit across; this one is ICON text pixels, centred on its line.
                poseStack.pushPose()
                poseStack.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE)
                poseStack.translate(left + ICON / 2f, y + LINE / 2f - 1f, 0f)
                poseStack.scale(ICON.toFloat(), -ICON.toFloat(), 0.01f)
                line.item.submit(poseStack, collector, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0)
                poseStack.popPose()
                poseStack.pushPose()
                poseStack.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE)
                collector.submitText(poseStack, left + ICON + GAP, y + 1f, line.name, false, net.minecraft.client.gui.Font.DisplayMode.NORMAL,
                    FULL_BRIGHT, -1, NEEDS_BACKGROUND, 0)
                collector.submitText(poseStack, right - line.countWidth, y + 1f, line.count, false, net.minecraft.client.gui.Font.DisplayMode.NORMAL,
                    FULL_BRIGHT, -1, NEEDS_BACKGROUND, 0)
                poseStack.popPose()
            }
            poseStack.popPose()
        }

        override fun shouldRenderOffScreen(): Boolean = true

        override fun getRenderBoundingBox(be: FrameBlockEntity): AABB {
            val multi = be.multiblock() ?: return AABB(be.blockPos)
            return AABB(be.blockPos).expandTowards(multi.size.first - 1.0, multi.size.second - 1.0, multi.size.third - 1.0)
        }

        companion object {
            const val GHOST_MIN = 0.15f
            const val GHOST_MAX = 0.65f
            private const val FULL_BRIGHT = 0xF000F0

            /** The needs list: blocks per text pixel (a name tag's), line height and icon size in text pixels. */
            private const val TEXT_SCALE = 0.025f
            private const val LINE = 10
            private const val ICON = 8
            private const val NEEDS_BACKGROUND = 0x40000000
            private const val NEEDS_RANGE = 24.0
            private const val GAP = 3

            /** Blocks left free around the list inside the structure. */
            private const val MARGIN = 0.25f
            private const val TICKS_PER_CHAR = 4f
            private const val LINE_STEP_TICKS = 40f
            private const val MARQUEE_GAP = "   "
            private val GHOST_SIDES: List<net.minecraft.core.Direction?> = net.minecraft.core.Direction.entries + listOf(null)
        }
    }

    /**
     * Axis-aligned boxes drawn as quads, each face tiled in one-block squares of a sprite (so textures keep their size on
     * faces larger than a block).
     */
    object Boxes {
        /**
         * The faces of the box ([x0], [y0], [z0]) to ([x1], [y1], [z1]) in [faces], facing out of the box, or into it if
         * [inward].
         */
        fun box(
            pose: PoseStack.Pose, buffer: com.mojang.blaze3d.vertex.VertexConsumer, sprite: net.minecraft.client.renderer.texture.TextureAtlasSprite,
            x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float, color: Int, light: Int,
            faces: Set<net.minecraft.core.Direction> = net.minecraft.core.Direction.entries.toSet(), inward: Boolean = false,
        ) {
            val dx = x1 - x0
            val dy = y1 - y0
            val dz = z1 - z0
            for (face in faces) {
                // Origin and two edges whose cross product points out of the box (see the face list).
                val (o, u, v) = when (face) {
                    net.minecraft.core.Direction.UP -> Triple(Vector3f(x0, y1, z0), Vector3f(0f, 0f, dz), Vector3f(dx, 0f, 0f))
                    net.minecraft.core.Direction.DOWN -> Triple(Vector3f(x0, y0, z0), Vector3f(dx, 0f, 0f), Vector3f(0f, 0f, dz))
                    net.minecraft.core.Direction.NORTH -> Triple(Vector3f(x0, y0, z0), Vector3f(0f, dy, 0f), Vector3f(dx, 0f, 0f))
                    net.minecraft.core.Direction.SOUTH -> Triple(Vector3f(x0, y0, z1), Vector3f(dx, 0f, 0f), Vector3f(0f, dy, 0f))
                    net.minecraft.core.Direction.WEST -> Triple(Vector3f(x0, y0, z0), Vector3f(0f, 0f, dz), Vector3f(0f, dy, 0f))
                    net.minecraft.core.Direction.EAST -> Triple(Vector3f(x1, y0, z0), Vector3f(0f, dy, 0f), Vector3f(0f, 0f, dz))
                }
                val n = face.unitVec3f
                if (inward) face(pose, buffer, sprite, o, v, u, -n.x(), -n.y(), -n.z(), color, light)
                else face(pose, buffer, sprite, o, u, v, n.x(), n.y(), n.z(), color, light)
            }
        }

        /** The rectangle [o] + a[u] + b[v] (a, b in 0..1), front side where u x v points, in one-block tiles. */
        private fun face(
            pose: PoseStack.Pose, buffer: com.mojang.blaze3d.vertex.VertexConsumer, sprite: net.minecraft.client.renderer.texture.TextureAtlasSprite,
            o: Vector3f, u: Vector3f, v: Vector3f, nx: Float, ny: Float, nz: Float, color: Int, light: Int,
        ) {
            val lu = u.length()
            val lv = v.length()
            if (lu <= 0f || lv <= 0f) return
            val cu = kotlin.math.ceil(lu - 1e-4f).toInt()
            val cv = kotlin.math.ceil(lv - 1e-4f).toInt()
            for (i in 0 until cu) for (j in 0 until cv) {
                val a0 = i.toFloat()
                val a1 = minOf(lu, i + 1f)
                val b0 = j.toFloat()
                val b1 = minOf(lv, j + 1f)
                fun corner(a: Float, b: Float) {
                    val p = Vector3f(o).add(Vector3f(u).mul(a / lu)).add(Vector3f(v).mul(b / lv))
                    buffer.addVertex(pose, p.x, p.y, p.z).setColor(color)
                        .setUv(sprite.getU(a - a0), sprite.getV(1f - (b - b0)))
                        .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, nx, ny, nz)
                }
                corner(a0, b0)
                corner(a1, b0)
                corner(a1, b1)
                corner(a0, b1)
            }
        }
    }

    class ReservoirState : BlockEntityRenderState() {
        var formed = false
        var size = 3f
        var shell: net.minecraft.client.renderer.texture.TextureAtlasSprite? = null
        var light = 0

        /** Per cell: its tank's still sprite, tint and how full (0-1). */
        val fluids = ArrayList<Triple<net.minecraft.client.renderer.texture.TextureAtlasSprite, Int, Float>?>()
        val fluidLight = ArrayList<Int>()

        /** Per cell: a bit for each cell of the same tank it touches. */
        val linkedTo = IntArray(com.itszuvalex.femtocraft.logistics.FluidReservoirState.TANKS)
    }

    /**
     * Draws the inside of a formed fluid reservoir, seen through its windows: an opaque inner shell (floor, walls and
     * ceiling, so nothing behind the reservoir shows through) and its four tanks as four columns of fluid, one per
     * quarter of the floor, each filled to its tank's level. Drawn from the home block, lit with the light above the
     * reservoir (the inside is solid blocks, which are dark).
     */
    class ReservoirRenderer : BlockEntityRenderer<com.itszuvalex.femtocraft.logistics.FluidReservoirBlockEntity, ReservoirState> {
        override fun createRenderState() = ReservoirState()

        override fun extractRenderState(
            be: com.itszuvalex.femtocraft.logistics.FluidReservoirBlockEntity, state: ReservoirState, partialTicks: Float, cameraPosition: Vec3,
            breakProgress: ModelFeatureRenderer.CrumblingOverlay?,
        ) {
            super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress)
            state.formed = be.isHome
            state.fluids.clear()
            state.fluidLight.clear()
            if (!state.formed) return
            val level = be.level ?: return
            val mc = net.minecraft.client.Minecraft.getInstance()
            state.size = com.itszuvalex.femtocraft.industry.FrameMultiblocks.FLUID_RESERVOIR.size.first.toFloat()
            state.shell = mc.modelManager.blockStateModelSet.get(be.blockState).particleMaterial().sprite()
            state.light = net.minecraft.client.renderer.LevelRenderer.getLightCoords(level, be.blockPos.offset(1, state.size.toInt(), 1))
            val tanks = be.clientTanks
            for (i in 0 until tanks.cellCount) state.linkedTo[i] = 0
            for (t in tanks.groups.indices) {
                val group = tanks.groups[t]
                val stack = tanks.get(t).toMinecraft()
                // Neighbouring cells of one tank join: no gap and no wall between them.
                for (c in group) {
                    if (c + 1 in group) {
                        state.linkedTo[c] = state.linkedTo[c] or (1 shl c + 1)
                        state.linkedTo[c + 1] = state.linkedTo[c + 1] or (1 shl c)
                    }
                }
                for (c in group) {
                    if (stack.isEmpty) {
                        state.fluids += null
                        state.fluidLight += state.light
                        continue
                    }
                    val model = mc.modelManager.fluidStateModelSet.get(stack.fluid.defaultFluidState())
                    val tint = model.fluidTintSource()?.colorAsStack(stack) ?: -1
                    // Every cell of a tank stands at the tank's level.
                    state.fluids += Triple(model.stillMaterial().sprite(), tint or (0xFF shl 24), stack.amount.toFloat() / tanks.capacity(t))
                    val glow = stack.fluid.fluidType.getLightLevel(stack)
                    state.fluidLight += net.minecraft.util.LightCoordsUtil.withBlock(state.light, maxOf(glow, net.minecraft.util.LightCoordsUtil.block(state.light)))
                }
            }
        }

        override fun submit(state: ReservoirState, poseStack: PoseStack, collector: SubmitNodeCollector, camera: CameraRenderState) {
            if (!state.formed) return
            val s = state.size
            val shell = state.shell ?: return
            // Just inside the walls, facing in.
            collector.submitCustomGeometry(poseStack, net.minecraft.client.renderer.rendertype.RenderTypes.solidMovingBlock()) { pose, buffer ->
                Boxes.box(pose, buffer, shell, SHELL, SHELL, SHELL, s - SHELL, s - SHELL, s - SHELL, -1, state.light, inward = true)
            }
            val half = s / 2
            val height = s - 2 * MARGIN
            collector.submitCustomGeometry(poseStack, net.minecraft.client.renderer.rendertype.RenderTypes.translucentMovingBlock()) { pose, buffer ->
                state.fluids.forEachIndexed { i, fluid ->
                    if (fluid == null || fluid.third <= 0f) return@forEachIndexed
                    // Cell i takes one quarter of the floor, in a ring (north-west, north-east, south-east,
                    // south-west), so linkable neighbours always share a side.
                    val east = i == 1 || i == 2
                    val south = i >= 2
                    val linked = state.linkedTo[i]
                    fun joins(other: Int) = (linked shr other) and 1 != 0
                    val westNeighbour = if (east) (if (south) 3 else 0) else -1
                    val eastNeighbour = if (!east) (if (south) 2 else 1) else -1
                    val northNeighbour = if (south) (if (east) 1 else 0) else -1
                    val southNeighbour = if (!south) (if (east) 2 else 3) else -1
                    val x0 = if (!east) MARGIN else half + if (joins(westNeighbour)) 0f else GAP
                    val x1 = if (!east) half - (if (joins(eastNeighbour)) 0f else GAP) else s - MARGIN
                    val z0 = if (!south) MARGIN else half + if (joins(northNeighbour)) 0f else GAP
                    val z1 = if (!south) half - (if (joins(southNeighbour)) 0f else GAP) else s - MARGIN
                    val top = MARGIN + height * fluid.third.coerceIn(0f, 1f)
                    val faces = net.minecraft.core.Direction.entries.toMutableSet()
                    faces -= net.minecraft.core.Direction.DOWN
                    if (westNeighbour >= 0 && joins(westNeighbour)) faces -= net.minecraft.core.Direction.WEST
                    if (eastNeighbour >= 0 && joins(eastNeighbour)) faces -= net.minecraft.core.Direction.EAST
                    if (northNeighbour >= 0 && joins(northNeighbour)) faces -= net.minecraft.core.Direction.NORTH
                    if (southNeighbour >= 0 && joins(southNeighbour)) faces -= net.minecraft.core.Direction.SOUTH
                    Boxes.box(pose, buffer, fluid.first, x0, MARGIN, z0, x1, top, z1, fluid.second, state.fluidLight[i], faces = faces)
                }
            }
        }

        override fun shouldRenderOffScreen(): Boolean = true

        override fun getRenderBoundingBox(be: com.itszuvalex.femtocraft.logistics.FluidReservoirBlockEntity): AABB =
            AABB(be.blockPos).expandTowards(2.0, 2.0, 2.0)

        companion object {
            /** How far inside the walls the shell sits, and the fluid columns' margin and the gap between them. */
            const val SHELL = 0.01f
            const val MARGIN = 1f / 16f
            const val GAP = 1f / 32f
        }
    }

    class ConduitChipState : BlockEntityRenderState() {
        var layout = 0L
        var arms = 0
        var light = 0
        var sprite: net.minecraft.client.renderer.texture.TextureAtlasSprite? = null
    }

    /**
     * The chips in a logistics conduit, shown on it ([com.itszuvalex.femtocraft.logistics.ChipNodes]): a small cube per
     * chip at a corner of its face's arm, in its kind's colour.
     */
    class ConduitChipRenderer : BlockEntityRenderer<com.itszuvalex.femtocraft.logistics.ConduitBlockEntity, ConduitChipState> {
        override fun createRenderState() = ConduitChipState()

        override fun extractRenderState(
            be: com.itszuvalex.femtocraft.logistics.ConduitBlockEntity, state: ConduitChipState, partialTicks: Float, cameraPosition: Vec3,
            breakProgress: ModelFeatureRenderer.CrumblingOverlay?,
        ) {
            super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress)
            state.layout = be.conduit.chipLayout
            if (state.layout == 0L) return
            state.arms = net.minecraft.core.Direction.entries.fold(0) { m, d -> if (be.hasArm(be.blockState, d)) m or (1 shl d.ordinal) else m }
            state.light = be.level?.let { net.minecraft.client.renderer.LevelRenderer.getLightCoords(it, be.blockPos) } ?: 0
            state.sprite = net.minecraft.client.Minecraft.getInstance().modelManager.blockStateModelSet
                .get(net.minecraft.world.level.block.Blocks.WHITE_CONCRETE.defaultBlockState()).particleMaterial().sprite()
        }

        override fun submit(state: ConduitChipState, poseStack: PoseStack, collector: SubmitNodeCollector, camera: CameraRenderState) {
            if (state.layout == 0L) return
            val sprite = state.sprite ?: return
            val kinds = com.itszuvalex.femtocraft.logistics.Chips.KINDS
            collector.submitCustomGeometry(poseStack, net.minecraft.client.renderer.rendertype.RenderTypes.solidMovingBlock()) { pose, buffer ->
                for (slot in 0 until com.itszuvalex.femtocraft.logistics.ChipNodes.SLOTS) {
                    val kind = com.itszuvalex.femtocraft.logistics.ChipNodes.kindAt(state.layout, slot)
                    if (kind == 0) continue
                    val face = slot / com.itszuvalex.femtocraft.logistics.LogisticsConduit.CHIPS_PER_FACE
                    val armed = state.arms and (1 shl net.minecraft.core.Direction.from3DDataValue(face).ordinal) != 0
                    val b = com.itszuvalex.femtocraft.logistics.ChipNodes.box(slot, armed)
                    val color = kinds.getOrNull(kind - 1)?.color ?: -1
                    Boxes.box(pose, buffer, sprite, b.minX.toFloat(), b.minY.toFloat(), b.minZ.toFloat(), b.maxX.toFloat(), b.maxY.toFloat(), b.maxZ.toFloat(), color, state.light)
                }
            }
        }
    }
}
