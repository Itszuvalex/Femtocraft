package com.itszuvalex.femtocraft.dev

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.industry.FrameItem
import com.itszuvalex.femtocraft.industry.FrameMultiblocks
import com.itszuvalex.femtocraft.logistics.LogisticsContent
import com.itszuvalex.femtocraft.power.CrystalMountBlockEntity
import com.itszuvalex.femtocraft.power.PowerContent
import com.itszuvalex.femtocraft.power.PowerCrystalData
import com.itszuvalex.femtocraft.power.PowerCrystals
import com.itszuvalex.itszulib.api.adapters.IItemStack
import com.itszuvalex.femtocraft.worldgen.WorldgenContent
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameType
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * A stage for checking models without playing: with `-Dfemtocraft.showcase=true` (`./gradlew runClient -Pshowcase`),
 * the first player to join is made a spectator, the Femtocraft blocks with OBJ models are built on a platform on the
 * ground at the world's centre, and the camera moves through [VIEWS], logging `SHOWCASE view <i>` at each and
 * `SHOWCASE done` at the end (for screenshots taken from outside the game). The client half hides the HUD (see
 * [DevShowcaseClient]). Dev only.
 */
object DevShowcase {
    @JvmStatic
    val enabled: Boolean get() = System.getProperty("femtocraft.showcase") == "true"

    /**
     * The stage's origin, on the ground at the world's centre (found when the stage is built). Sections high in open sky
     * were never lit, so a stage there renders black.
     */
    private var BASE = BlockPos(0, 120, 0)
    private const val BUILD_AT = 40
    private const val VIEW_TICKS = 400

    /**
     * Camera position and the point it looks at, relative to [BASE].
     */
    private val VIEWS = listOf(
        Vec3(9.0, 4.5, -9.0) to Vec3(9.0, 1.0, 2.0),
        Vec3(2.5, 2.2, -2.5) to Vec3(2.5, 0.5, 2.5),
        Vec3(2.0, 1.6, 0.2) to Vec3(2.5, 0.7, 2.5),
        Vec3(1.0, 3.5, -3.5) to Vec3(1.5, 0.5, 1.5),
        Vec3(7.5, 2.5, -2.0) to Vec3(7.5, 0.5, 2.5),
        Vec3(15.5, 4.5, -6.0) to Vec3(16.0, 1.5, 1.0),
        Vec3(18.0, 4.0, -3.0) to Vec3(18.0, 2.2, 1.0),
        Vec3(8.0, 2.5, 2.0) to Vec3(9.0, 0.8, 6.0),
        Vec3(12.0, 3.0, 1.0) to Vec3(13.0, -0.5, 3.5),
        Vec3(2.5, 2.0, 9.0) to Vec3(2.5, 0.5, 5.5),
    )

    private var player: ServerPlayer? = null
    private var ticks = 0

    fun register() {
        if (!enabled) return
        NeoForge.EVENT_BUS.addListener { e: PlayerEvent.PlayerLoggedInEvent -> if (player == null) start(e.entity as ServerPlayer) }
        NeoForge.EVENT_BUS.addListener { _: ServerTickEvent.Post -> tick() }
    }

    private fun start(p: ServerPlayer) {
        player = p
        ticks = 0
        p.setGameMode(GameType.SPECTATOR)
        val server = p.level().server
        listOf("time set noon", "weather clear").forEach { server.commands.performPrefixedCommand(server.createCommandSourceStack(), it) }
        look(p, 0)
    }

    private fun tick() {
        val p = player ?: return
        ticks++
        val level = p.level() as ServerLevel
        if (ticks == BUILD_AT - 20) {
            BASE = BlockPos(0, level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, 0, 0) + 1, 0)
            look(p, 0)
        }
        if (ticks == BUILD_AT) {
            build(level)
            Femtocraft.LOGGER.info("SHOWCASE built")
        }
        if (ticks < BUILD_AT) return
        building()?.progress = 0
        val view = (ticks - BUILD_AT) / VIEW_TICKS
        if ((ticks - BUILD_AT) % VIEW_TICKS != 0) return
        if (view >= VIEWS.size) {
            if (view == VIEWS.size) Femtocraft.LOGGER.info("SHOWCASE done")
            return
        }
        // The last view also fills the player's nanites, so the HUD shows the nanite gauge sliding in, and hands the
        // player frames for a germination chamber, so the frame preview outlines it where the camera looks.
        if (view == VIEWS.size - 1) {
            p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                FrameItem.withSelection(net.minecraft.world.item.ItemStack(com.itszuvalex.femtocraft.industry.IndustryContent.FRAME_ITEM.get(), 64), FrameMultiblocks.GERMINATION_CHAMBER.id))
            val tank = com.itszuvalex.femtocraft.nanite.PlayerNanites.tank(p)
            tank.fill(com.itszuvalex.femtocraft.nanite.NaniteRegistry.dumb(60), true)
            com.itszuvalex.femtocraft.nanite.PlayerNanites.save(p, tank)
        }
        look(p, view)
        Femtocraft.LOGGER.info("SHOWCASE view {}", view)
    }

    private fun look(p: ServerPlayer, view: Int) {
        val (eye, target) = VIEWS[view]
        val from = Vec3.atLowerCornerOf(BASE).add(eye)
        val to = Vec3.atLowerCornerOf(BASE).add(target)
        val d = to.subtract(from)
        val yaw = Math.toDegrees(atan2(-d.x, d.z)).toFloat()
        val pitch = (-Math.toDegrees(atan2(d.y, sqrt(d.x * d.x + d.z * d.z)))).toFloat()
        p.teleportTo(p.level() as ServerLevel, from.x, from.y - p.eyeHeight, from.z, setOf(), yaw, pitch, true)
    }

    private fun building(): com.itszuvalex.femtocraft.industry.FrameState? =
        (player?.level()?.getBlockEntity(BASE.offset(10, 0, 5)) as? com.itszuvalex.femtocraft.industry.FrameBlockEntity)?.frameState()

    private fun mountCrystal(level: ServerLevel, pos: BlockPos, color: Int) {
        val stack = net.minecraft.world.item.ItemStack(PowerContent.POWER_CRYSTAL.get())
        PowerCrystals.set(stack, PowerCrystalData("Showcase", PowerCrystals.TYPE_SMALL, color, 0f, 100.0, 0.0, 0.0, 1000.0))
        (level.getBlockEntity(pos) as? CrystalMountBlockEntity)?.storage?.setSlot(0, IItemStack.of(stack))
    }

    private fun build(level: ServerLevel) {
        fun set(x: Int, y: Int, z: Int, block: Block) = level.setBlockAndUpdate(BASE.offset(x, y, z), block.defaultBlockState())
        for (x in -3..20) for (z in -3..8) {
            set(x, -1, z, Blocks.SMOOTH_STONE)
            for (y in 0..8) set(x, y, z, Blocks.AIR)
        }
        // Crystal mounts: bottom only, then with a solid block above (top and bottom).
        set(0, 0, 2, PowerContent.CRYSTAL_MOUNT.get())
        set(2, 0, 2, PowerContent.CRYSTAL_MOUNT.get())
        set(2, 1, 2, Blocks.SMOOTH_STONE)
        set(4, 1, 2, PowerContent.CRYSTAL_MOUNT.get())
        // Crystals in two of them (the renderer turns them with the grips, in the crystal's color).
        mountCrystal(level, BASE.offset(0, 0, 2), 0x00FFFF)
        mountCrystal(level, BASE.offset(2, 0, 2), 0xFF40C0)
        // The two mounts with crystals form a wireless network (a power beam between them); a charging array nearby
        // is its leaf (a diffusion beam).
        set(1, 0, 0, PowerContent.CRYSTAL_CHARGING_ARRAY.get())
        // Power conduits: a corner, so arms show along x and z.
        for (x in 6..8) set(x, 0, 2, PowerContent.POWER_CONDUIT.get())
        set(8, 0, 3, PowerContent.POWER_CONDUIT.get())
        set(8, 1, 2, PowerContent.POWER_CONDUIT.get())
        // Logistics conduits ending at a chest (an inventory arm).
        for (x in 10..11) set(x, 0, 2, LogisticsContent.CONDUIT.get())
        set(12, 0, 2, Blocks.CHEST)
        // A frame structure and a finished germination chamber, both 2x3x2.
        FrameItem.place(level, BASE.offset(14, 0, 0), FrameMultiblocks.GERMINATION_CHAMBER)
        FrameMultiblocks.GERMINATION_CHAMBER.formAt(level, BASE.offset(17, 0, 0))
        // Crystal clusters, each with its own random color.
        for (x in listOf(0, 2, 4)) set(x, 0, 5, WorldgenContent.CRYSTAL_CLUSTER.get())
        // Glow sticks, each with its own random pale color.
        for (x in listOf(6, 7, 8)) set(x, 0, 6, PowerContent.GLOW_STICK.get())
        // A frame structure that keeps building (nanite particles; [tick] holds its progress at 0).
        FrameItem.place(level, BASE.offset(10, 0, 5), FrameMultiblocks.GERMINATION_CHAMBER)
        building()?.building = true
    }
}
