package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.core.FemtoMenu
import com.itszuvalex.femtocraft.industry.ConfiguratorItem
import com.itszuvalex.femtocraft.industry.ConfiguratorMode
import com.itszuvalex.itszulib.api.adapters.IBlockEntity
import com.itszuvalex.itszulib.core.EnumAutomaticIO
import com.itszuvalex.itszulib.core.SidedStorageConfiguration
import com.itszuvalex.itszulib.menu.MenuActionPayload
import com.mojang.blaze3d.platform.Lighting
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.QuadInstance
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.util.RandomSource
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent
import net.neoforged.neoforge.client.network.ClientPacketDistributor
import org.joml.Quaternionf
import org.joml.Vector3f

/**
 * The 3D side configuration panel beside a machine screen (REVIEW O8, after Ender IO's IO configuration view): the
 * machine in the middle and its six neighbours pulled out around it, each face shaded by its automatic IO setting.
 * Drag to rotate; click a face, or the neighbour on that side, to cycle it as the configurator does (shift cycles
 * backwards); the mode button switches between the sided configurations the machine has. The configuration is read
 * from the client block entity (sided configurations sync with the block entity's description) and changed through
 * [FemtoMenu.ACTION_CONFIGURE].
 */
class SideConfigPanel(private val menu: FemtoMenu<*>, private val modes: List<ConfiguratorMode>) {
    var x = 0
    var y = 0
    var mode: ConfiguratorMode = modes.first()
        private set
    private var yaw = 0f
    private var pitch = DEFAULT_PITCH
    private var pressX = Double.NaN
    private var pressY = Double.NaN
    private var dragged = false

    init {
        configuration()?.let { yaw = SideConfigGeometry.defaultYaw(it.front()) }
    }

    fun cycleMode() {
        mode = modes[(modes.indexOf(mode) + 1) % modes.size]
    }

    fun configuration(): SidedStorageConfiguration<*>? =
        ConfiguratorItem.configModule(mode)?.let { (menu.blockEntity as? IBlockEntity)?.getModule(it, null) }

    private fun rotation(): Quaternionf = SideConfigGeometry.rotation(yaw, pitch)

    private fun centreX() = x + SIZE / 2.0

    private fun centreY() = y + SIZE / 2.0

    fun contains(mx: Double, my: Double) = mx >= x && mx < x + SIZE && my >= y && my < y + SIZE

    private fun neighbours(be: BlockEntity): Set<Direction> {
        val level = be.level ?: return emptySet()
        return Direction.entries.filterTo(mutableSetOf()) { !level.getBlockState(be.blockPos.relative(it)).isAir }
    }

    private fun faceAt(mx: Double, my: Double): Direction? {
        val be = menu.blockEntity ?: return null
        if (!contains(mx, my)) return null
        return SideConfigGeometry.pick(rotation(), mx - centreX(), my - centreY(), SCALE, neighbours(be))
    }

    fun extract(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, font: net.minecraft.client.gui.Font) {
        graphics.fill(x - 1, y - 1, x + SIZE + 1, y + SIZE + 1, FemtoScreen.SLOT)
        graphics.fill(x, y, x + SIZE, y + SIZE, BACKGROUND)
        val be = menu.blockEntity ?: return
        val level = be.level as? ClientLevel ?: return
        val config = configuration()
        val hovered = if (dragged) null else faceAt(mouseX.toDouble(), mouseY.toDouble())
        val blocks = mutableListOf(SideConfigRenderState.Placed.of(level, be.blockPos, be.blockState, Vector3f(), 1f))
        for (dir in neighbours(be)) {
            val pos = be.blockPos.relative(dir)
            val offset = Vector3f(dir.stepX.toFloat(), dir.stepY.toFloat(), dir.stepZ.toFloat()).mul(SideConfigGeometry.NEIGHBOUR_DISTANCE)
            blocks += SideConfigRenderState.Placed.of(level, pos, level.getBlockState(pos), offset, SideConfigGeometry.NEIGHBOUR_SIZE)
        }
        val colors = IntArray(Direction.entries.size) { i ->
            val face = Direction.entries[i]
            val base = config?.let { colorFor(it.getIOForAbsoluteFacing(face)) } ?: 0
            if (face == hovered) blend(base) else base
        }
        graphics.submitPictureInPictureRenderState(
            SideConfigRenderState(blocks, colors, rotation(), x, y, x + SIZE, y + SIZE, SCALE.toFloat(), graphics.peekScissorStack()),
        )
        if (hovered != null && config != null) {
            graphics.setTooltipForNextFrame(font, listOf(
                Component.translatable("gui.femtocraft.side_config.face", Component.translatable("gui.femtocraft.side_config.dir.${hovered.serializedName}")).visualOrderText,
                Component.translatable("gui.femtocraft.side_config.io.${config.getIOForAbsoluteFacing(hovered).name.lowercase()}").visualOrderText,
                Component.translatable("gui.femtocraft.side_config.storage", config.getStorageNameForAbsoluteFacing(hovered)).visualOrderText,
            ), net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner.INSTANCE, mouseX, mouseY, false)
        }
    }

    fun mouseClicked(mx: Double, my: Double): Boolean {
        if (!contains(mx, my)) return false
        pressX = mx
        pressY = my
        dragged = false
        return true
    }

    fun mouseDragged(mx: Double, my: Double, dx: Double, dy: Double): Boolean {
        if (pressX.isNaN()) return false
        if (!dragged && (mx - pressX) * (mx - pressX) + (my - pressY) * (my - pressY) < DRAG_THRESHOLD * DRAG_THRESHOLD) return true
        dragged = true
        yaw += (dx * DRAG_RADIANS).toFloat()
        pitch = (pitch + (dy * DRAG_RADIANS).toFloat()).coerceIn(-MAX_PITCH, MAX_PITCH)
        return true
    }

    fun mouseReleased(mx: Double, my: Double, backward: Boolean): Boolean {
        if (pressX.isNaN()) return false
        val wasDrag = dragged
        pressX = Double.NaN
        pressY = Double.NaN
        dragged = false
        if (wasDrag) return true
        val face = faceAt(mx, my) ?: return true
        if (configuration() == null) return true
        ClientPacketDistributor.sendToServer(MenuActionPayload(menu.containerId, FemtoMenu.ACTION_CONFIGURE, FemtoMenu.configureData(face, mode, backward)))
        return true
    }

    companion object {
        const val SIZE = 112
        const val SCALE = 30
        const val BACKGROUND = 0xFF2B2B2B.toInt()
        const val DEFAULT_PITCH = 0.5f
        const val MAX_PITCH = 1.5f
        const val DRAG_RADIANS = 0.02
        const val DRAG_THRESHOLD = 3.0
        const val INPUT = 0x803399FF.toInt()
        const val OUTPUT = 0x80FF9933.toInt()

        fun colorFor(io: EnumAutomaticIO): Int = when (io) {
            EnumAutomaticIO.INPUT -> INPUT
            EnumAutomaticIO.OUTPUT -> OUTPUT
            EnumAutomaticIO.NONE -> 0
        }

        /**
         * Lightens an overlay colour for the hovered face (a faint white overlay for a face without a setting).
         */
        fun blend(color: Int): Int {
            if (color == 0) return 0x50FFFFFF
            val r = ((color shr 16 and 255) + 255) / 2
            val g = ((color shr 8 and 255) + 255) / 2
            val b = ((color and 255) + 255) / 2
            return (0xA0 shl 24) or (r shl 16) or (g shl 8) or b
        }
    }
}

/**
 * The panel's layout and face picking, without a game. Points are in blocks relative to the machine's centre; the view
 * is rotated by [rotation] and projected orthographically, x right, y up and z towards the viewer.
 */
object SideConfigGeometry {
    /**
     * Distance from the machine's centre to a neighbour's centre, in blocks.
     */
    const val NEIGHBOUR_DISTANCE = 1.35f

    /**
     * A neighbour's edge length, in blocks.
     */
    const val NEIGHBOUR_SIZE = 0.5f

    fun rotation(yaw: Float, pitch: Float): Quaternionf = Quaternionf().rotateX(pitch).rotateY(yaw)

    /**
     * A yaw that turns [front] towards the viewer, a little to the left so a side shows too.
     */
    fun defaultYaw(front: Direction): Float {
        val angle = kotlin.math.atan2(front.stepX.toFloat(), front.stepZ.toFloat())
        return -(Math.PI.toFloat() / 6) - angle
    }

    /**
     * The machine face (or the neighbour on that side) under a point [dx], [dy] GUI pixels from the panel's centre,
     * or null. [scale] is pixels per block; [neighbours] are the sides with a neighbour drawn.
     */
    fun pick(rotation: Quaternionf, dx: Double, dy: Double, scale: Int, neighbours: Set<Direction>): Direction? {
        val px = (dx / scale).toFloat()
        val py = (-dy / scale).toFloat()
        var best: Direction? = null
        var bestDepth = Float.NEGATIVE_INFINITY
        fun test(side: Direction, centre: Vector3f, size: Float) {
            for (face in Direction.entries) {
                val depth = faceDepthAt(rotation, centre, size, face, px, py) ?: continue
                if (depth > bestDepth) {
                    bestDepth = depth
                    best = side
                }
            }
        }
        for (face in Direction.entries) {
            val depth = faceDepthAt(rotation, Vector3f(), 1f, face, px, py) ?: continue
            if (depth > bestDepth) {
                bestDepth = depth
                best = face
            }
        }
        for (side in neighbours) {
            test(side, Vector3f(side.stepX.toFloat(), side.stepY.toFloat(), side.stepZ.toFloat()).mul(NEIGHBOUR_DISTANCE), NEIGHBOUR_SIZE)
        }
        return best
    }

    /**
     * The depth (larger is nearer) at which the projected point ([px], [py]) hits [face] of the cube of edge [size]
     * centred on [centre], or null if it misses or the face points away.
     */
    fun faceDepthAt(rotation: Quaternionf, centre: Vector3f, size: Float, face: Direction, px: Float, py: Float): Float? {
        val normal = rotation.transform(Vector3f(face.stepX.toFloat(), face.stepY.toFloat(), face.stepZ.toFloat()))
        if (normal.z <= 1e-4f) return null
        val corners = faceCorners(face).map { rotation.transform(Vector3f(it).mul(size).add(centre)) }
        if (!insideConvex(corners, px, py)) return null
        // Depth of the face's plane at the point.
        val p0 = corners[0]
        return p0.z - (normal.x * (px - p0.x) + normal.y * (py - p0.y)) / normal.z
    }

    /**
     * The corners of a unit cube's [face], centred on the origin, counter-clockwise seen from outside the cube.
     */
    fun faceCorners(face: Direction): List<Vector3f> {
        val n = Vector3f(face.stepX.toFloat(), face.stepY.toFloat(), face.stepZ.toFloat()).mul(0.5f)
        val (u, v) = when (face.axis) {
            Direction.Axis.X -> Vector3f(0f, 0.5f, 0f) to Vector3f(0f, 0f, 0.5f)
            Direction.Axis.Y -> Vector3f(0.5f, 0f, 0f) to Vector3f(0f, 0f, 0.5f)
            Direction.Axis.Z -> Vector3f(0.5f, 0f, 0f) to Vector3f(0f, 0.5f, 0f)
        }
        val corners = listOf(
            Vector3f(n).sub(u).sub(v), Vector3f(n).add(u).sub(v), Vector3f(n).add(u).add(v), Vector3f(n).sub(u).add(v),
        )
        // Counter-clockwise seen from outside, so culling render types keep the face.
        val winding = Vector3f(u).cross(v).dot(n)
        return if (winding > 0) corners else corners.reversed()
    }

    private fun insideConvex(corners: List<Vector3f>, px: Float, py: Float): Boolean {
        var sign = 0
        for (i in corners.indices) {
            val a = corners[i]
            val b = corners[(i + 1) % corners.size]
            val cross = (b.x - a.x) * (py - a.y) - (b.y - a.y) * (px - a.x)
            val s = if (cross > 0) 1 else if (cross < 0) -1 else 0
            if (s == 0) continue
            if (sign == 0) sign = s else if (s != sign) return false
        }
        return true
    }
}

/**
 * What [SideConfigRenderer] draws: blocks (their model parts collected on the render thread) and the machine's face
 * overlays, one ARGB colour per [Direction] (0 for none).
 */
class SideConfigRenderState(
    val blocks: List<Placed>,
    val faceColors: IntArray,
    val rotation: Quaternionf,
    private val x0: Int,
    private val y0: Int,
    private val x1: Int,
    private val y1: Int,
    private val scale: Float,
    private val scissorArea: ScreenRectangle?,
) : PictureInPictureRenderState {
    private val bounds = PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea)

    override fun x0() = x0

    override fun y0() = y0

    override fun x1() = x1

    override fun y1() = y1

    override fun scale() = scale

    override fun scissorArea() = scissorArea

    override fun bounds() = bounds

    /**
     * A block drawn at [offset] from the machine's centre with edge [size], and the tint colour per tint index.
     */
    class Placed(val parts: List<BlockStateModelPart>, val tints: IntArray, val offset: Vector3f, val size: Float) {
        /**
         * True if the block model has no quads (the block is drawn by its block entity renderer, or not at all).
         */
        val empty: Boolean = parts.all { part -> (Direction.entries + listOf(null)).all { part.getQuads(it).isEmpty() } }

        companion object {
            fun of(level: ClientLevel, pos: BlockPos, state: BlockState, offset: Vector3f, size: Float): Placed {
                val minecraft = Minecraft.getInstance()
                val parts = mutableListOf<BlockStateModelPart>()
                minecraft.modelManager.blockStateModelSet.get(state).collectParts(level, pos, state, RandomSource.create(pos.asLong()), parts)
                val tints = minecraft.blockColors.getTintSources(state).map { it.colorInWorld(state, level, pos) or 0xFF000000.toInt() }.toIntArray()
                return Placed(parts, tints, offset, size)
            }
        }
    }
}

class SideConfigRenderer(bufferSource: MultiBufferSource.BufferSource) : PictureInPictureRenderer<SideConfigRenderState>(bufferSource) {
    private val quad = QuadInstance()

    override fun getRenderStateClass(): Class<SideConfigRenderState> = SideConfigRenderState::class.java

    override fun getTextureLabel(): String = "femtocraft_side_config"

    override fun getTranslateY(height: Int, guiScale: Int): Float = height / 2f

    override fun renderToTexture(renderState: SideConfigRenderState, poseStack: PoseStack) {
        Minecraft.getInstance().gameRenderer.lighting.setupFor(Lighting.Entry.ITEMS_3D)
        // The texture's pose has y down and z away from the viewer (a half turn about x from the panel's view space).
        poseStack.mulPose(Quaternionf().rotateX(Math.PI.toFloat()))
        poseStack.mulPose(renderState.rotation)
        quad.setLightCoords(FULL_BRIGHT)
        quad.setOverlayCoords(OverlayTexture.NO_OVERLAY)
        for (block in renderState.blocks) {
            poseStack.pushPose()
            poseStack.translate(block.offset.x, block.offset.y, block.offset.z)
            poseStack.scale(block.size, block.size, block.size)
            poseStack.translate(-0.5f, -0.5f, -0.5f)
            val pose = poseStack.last()
            for (part in block.parts) {
                for (dir in DIRECTIONS_AND_NULL) {
                    for (baked in part.getQuads(dir)) {
                        val tint = baked.materialInfo().tintIndex()
                        quad.setColor(if (tint in block.tints.indices) block.tints[tint] else -1)
                        bufferSource.getBuffer(baked.materialInfo().itemRenderType()).putBakedQuad(pose, baked, quad)
                    }
                }
            }
            poseStack.popPose()
        }
        // Draw the blocks first: their render types are batched until the end, and the overlays go on top of them.
        bufferSource.endBatch()
        // Blocks drawn only by a block entity renderer (a chest) get a faint box so the side still shows a neighbour.
        for (block in renderState.blocks) {
            if (!block.empty) continue
            for (face in Direction.entries) box(poseStack, block.offset, block.size, face, EMPTY_BOX)
        }
        for (face in Direction.entries) {
            val color = renderState.faceColors[face.ordinal]
            if (color != 0) box(poseStack, Vector3f(), OVERLAY_SCALE, face, color)
        }
    }

    private fun box(poseStack: PoseStack, centre: Vector3f, size: Float, face: Direction, color: Int) {
        val buffer = bufferSource.getBuffer(RenderTypes.textBackground())
        val pose = poseStack.last()
        for (corner in SideConfigGeometry.faceCorners(face)) {
            corner.mul(size).add(centre)
            buffer.addVertex(pose, corner.x, corner.y, corner.z).setColor(color).setLight(FULL_BRIGHT)
        }
    }

    companion object {
        private const val FULL_BRIGHT = 0xF000F0
        private const val OVERLAY_SCALE = 1.04f
        private const val EMPTY_BOX = 0x40C0C0C0
        private val DIRECTIONS_AND_NULL: List<Direction?> = Direction.entries + listOf(null)

        fun register(event: RegisterPictureInPictureRenderersEvent) {
            event.register(SideConfigRenderState::class.java, ::SideConfigRenderer)
        }
    }
}
