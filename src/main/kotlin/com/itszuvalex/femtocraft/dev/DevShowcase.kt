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
 * A stage for checking models without playing: with `-Dfemtocraft.showcase` set (`./gradlew runClient -Pshowcase`),
 * the first player to join is made a spectator, the Femtocraft blocks with OBJ models are built on a platform on the
 * ground at the world's centre, and the camera moves through [VIEWS], logging `SHOWCASE view <i>` at each and
 * `SHOWCASE done` at the end (for screenshots taken from outside the game). The client half hides the HUD (see
 * [DevShowcaseClient]). Dev only.
 */
object DevShowcase {
    @JvmStatic
    val enabled: Boolean get() = System.getProperty("femtocraft.showcase") != null

    /**
     * The first view to show (`-Pshowcase=<n>`), 0 by default.
     */
    private val firstView: Int get() = System.getProperty("femtocraft.showcase")?.toIntOrNull() ?: 0

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
        Vec3(16.5, 2.5, 3.0) to Vec3(16.5, 0.5, 6.5),
        Vec3(9.0, 4.5, -9.0) to Vec3(9.0, 1.0, 2.0),
        Vec3(9.0, 4.5, -9.0) to Vec3(9.0, 1.0, 2.0),
        Vec3(17.5, 3.0, -2.5) to Vec3(17.5, 1.5, 1.0),
        Vec3(12.5, 2.5, 4.0) to Vec3(12.5, 0.5, 7.0),
        Vec3(13.0, 3.0, 2.5) to Vec3(14.5, 1.0, 6.5),
        Vec3(16.0, 3.5, 1.0) to Vec3(19.5, 3.0, 7.5),
        Vec3(23.5, 3.0, -3.5) to Vec3(23.5, 1.5, 1.5),
        Vec3(27.0, 5.0, -7.0) to Vec3(27.0, 1.5, 1.5),
        Vec3(25.0, 3.2, -2.2) to Vec3(27.5, 1.2, 1.5),
        Vec3(25.0, 3.2, -2.2) to Vec3(27.5, 1.2, 1.5),
        Vec3(10.5, 2.5, -1.0) to Vec3(10.5, 0.5, 2.0),
        // 22: the storage multiblock items in item frames; 23: one in hand; 24: dropped on the ground
        Vec3(30.0, 1.6, 4.2) to Vec3(30.0, 1.4, 7.0),
        Vec3(30.0, 1.6, 4.2) to Vec3(30.0, 1.6, 7.0),
        Vec3(30.0, 1.9, 4.0) to Vec3(30.0, 0.2, 6.0),
        // 25-28: the channel panel: joined, waiting for a deleted channel, the team's channels, asking before a delete
        Vec3(2.5, 2.0, 9.0) to Vec3(2.5, 0.5, 5.5),
        Vec3(2.5, 2.0, 9.0) to Vec3(2.5, 0.5, 5.5),
        Vec3(2.5, 2.0, 9.0) to Vec3(2.5, 0.5, 5.5),
        Vec3(2.5, 2.0, 9.0) to Vec3(2.5, 0.5, 5.5),
        // 29-30: a long list of long names: the scrollbar, then scrolled down
        Vec3(2.5, 2.0, 9.0) to Vec3(2.5, 0.5, 5.5),
        Vec3(2.5, 2.0, 9.0) to Vec3(2.5, 0.5, 5.5),
    )

    private const val ITEMS_VIEW = 22
    private const val HAND_VIEW = 23
    private const val DROPPED_VIEW = 24
    private const val CHANNELS_VIEW = 25
    private val CHANNEL_BLOCK = BlockPos(2, 0, 6)
    private val CHANNEL_BLOCK_2 = BlockPos(4, 0, 6)

    /** The view being shown, for the client half (screenshots). */
    @Volatile
    @JvmStatic
    var currentView = -1

    /** Ticks into the current view. */
    @Volatile
    @JvmStatic
    var viewTicks = 0

    private const val FRAMES_VIEW = 9
    private const val SIDE_CONFIG_VIEW = 10
    private const val CODEX_VIEW = 11
    private const val MACHINE_VIEW = 12
    private const val MULTIBLOCK_VIEW = 13
    private const val COMPUTATION_VIEW = 14
    private val MAINFRAME = BlockPos(11, 0, 7)
    private const val LIGHTNING_VIEW = 16
    private const val VAULT_VIEW = 17
    private const val CONDUIT_VIEW = 21
    private val ITEM_VAULT = BlockPos(22, 0, 0)
    private val POLE = BlockPos(19, 0, 7)
    private val CHAMBER = BlockPos(17, 0, 0)
    private val FURNACE = BlockPos(9, 2, -7)
    private const val MENU_DELAY = 40
    private val LIQUIFIER = BlockPos(16, 0, 6)

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
        val view = firstView + (ticks - BUILD_AT) / VIEW_TICKS
        // The side configuration view opens the crystal liquifier's screen, with its 3D side configuration panel, a
        // little after the view starts (once the client has the block entity).
        if (view == SIDE_CONFIG_VIEW && (ticks - BUILD_AT) % VIEW_TICKS == MENU_DELAY) {
            p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY)
            (level.getBlockEntity(BASE.offset(LIQUIFIER)) as? com.itszuvalex.itszulib.api.adapters.IBlockEntity)?.getModule(com.itszuvalex.itszulib.api.Modules.MENU, null)
                ?.let { p.openMenu(it, it.menuPos()) }
        }
        // The Codex view queues research for the player's team, records two Archives and opens the Codex.
        if (view == CODEX_VIEW && (ticks - BUILD_AT) % VIEW_TICKS == MENU_DELAY) {
            p.closeContainer()
            codex(p, level)
        }
        // The machine view opens a nano furnace's screen: an input slot, an output slot and a power gauge.
        // (Placed first, so the client has its block entity when the menu opens.)
        if (view == MACHINE_VIEW && (ticks - BUILD_AT) % VIEW_TICKS == 1) {
            level.setBlockAndUpdate(BASE.offset(FURNACE), com.itszuvalex.femtocraft.industry.IndustryContent.NANO_FURNACE.get().defaultBlockState())
        }
        if (view == MACHINE_VIEW && (ticks - BUILD_AT) % VIEW_TICKS == MENU_DELAY) {
            p.closeContainer()
            (level.getBlockEntity(BASE.offset(FURNACE)) as? com.itszuvalex.itszulib.api.adapters.IBlockEntity)?.getModule(com.itszuvalex.itszulib.api.Modules.MENU, null)
                ?.let { p.openMenu(it, it.menuPos()) }
        }
        // ...and closes it again, so the furnace itself shows in the world (its colour layer behind the base texture).
        if (view == MACHINE_VIEW && (ticks - BUILD_AT) % VIEW_TICKS == MENU_DELAY + 120) p.closeContainer()
        // The multiblock view opens the germination chamber's screen: its side configuration panel shows the whole
        // structure, with two outer faces of different members configured.
        if (view == MULTIBLOCK_VIEW && (ticks - BUILD_AT) % VIEW_TICKS == MENU_DELAY) {
            p.closeContainer()
            for ((offset, face) in listOf(BlockPos.ZERO to net.minecraft.core.Direction.NORTH, BlockPos(1, 2, 1) to net.minecraft.core.Direction.UP)) {
                val member = level.getBlockEntity(BASE.offset(CHAMBER).offset(offset)) as? com.itszuvalex.itszulib.api.adapters.IBlockEntity
                member?.getModule(com.itszuvalex.itszulib.api.Modules.ITEM_STORAGE_CONFIGURABLE, null)
                    ?.let { com.itszuvalex.femtocraft.industry.ConfiguratorItem.cycle(it, face, false) }
                (member as? com.itszuvalex.itszulib.core.BlockEntityCore)?.markDirtyAndSync()
            }
            (level.getBlockEntity(BASE.offset(CHAMBER)) as? com.itszuvalex.itszulib.api.adapters.IBlockEntity)?.getModule(com.itszuvalex.itszulib.api.Modules.MENU, null)
                ?.let { p.openMenu(it, it.menuPos()) }
        }
        // The computation view opens the mainframe's screen (it computes for the logistics conduit beside it).
        // The next view closes it again to show the blocks (and the cryo-endothermal stack beside them).
        if (view == COMPUTATION_VIEW + 1 && (ticks - BUILD_AT) % VIEW_TICKS == 1) p.closeContainer()
        if (view == COMPUTATION_VIEW && (ticks - BUILD_AT) % VIEW_TICKS == MENU_DELAY) {
            p.closeContainer()
            (level.getBlockEntity(BASE.offset(MAINFRAME)) as? com.itszuvalex.itszulib.api.adapters.IBlockEntity)?.getModule(com.itszuvalex.itszulib.api.Modules.MENU, null)
                ?.let { p.openMenu(it, it.menuPos()) }
        }
        // The vault view opens the item vault's terminal; the next shows the three vaults.
        if (view == VAULT_VIEW && (ticks - BUILD_AT) % VIEW_TICKS == MENU_DELAY) {
            p.closeContainer()
            (level.getBlockEntity(BASE.offset(ITEM_VAULT)) as? com.itszuvalex.itszulib.api.adapters.IBlockEntity)?.getModule(com.itszuvalex.itszulib.api.Modules.MENU, null)
                ?.let { p.openMenu(it, it.menuPos()) }
        }
        if (view == VAULT_VIEW + 1 && (ticks - BUILD_AT) % VIEW_TICKS == 1) p.closeContainer()
        // ...then the fluid reservoir's screen.
        if (view == VAULT_VIEW + 3 && (ticks - BUILD_AT) % VIEW_TICKS == MENU_DELAY) {
            (level.getBlockEntity(BASE.offset(ITEM_VAULT).offset(4, 0, 0)) as? com.itszuvalex.itszulib.api.adapters.IBlockEntity)?.getModule(com.itszuvalex.itszulib.api.Modules.MENU, null)
                ?.let { p.openMenu(it, it.menuPos()) }
        }
        // The conduit view opens a logistics conduit holding filtered chips.
        if (view == CONDUIT_VIEW && (ticks - BUILD_AT) % VIEW_TICKS == 1) p.closeContainer()
        if (view == CONDUIT_VIEW && (ticks - BUILD_AT) % VIEW_TICKS == MENU_DELAY) {
            (level.getBlockEntity(BASE.offset(10, 0, 2)) as? com.itszuvalex.itszulib.api.adapters.IBlockEntity)?.getModule(com.itszuvalex.itszulib.api.Modules.MENU, null)
                ?.let { p.openMenu(it, it.menuPos()) }
        }
        // The lightning view strikes the atmospheric pole every half second (harmless bolts on the capacitor's top).
        if (view == LIGHTNING_VIEW && (ticks - BUILD_AT) % 10 == 5) {
            (level.getBlockEntity(BASE.offset(POLE)) as? com.itszuvalex.femtocraft.power.AtmosphericChargingBaseBlockEntity)?.let { pole ->
                pole.capacitorPos()?.let { pole.strike(level, it) }
            }
        }
        currentView = view
        viewTicks = (ticks - BUILD_AT) % VIEW_TICKS
        if (view >= ITEMS_VIEW) extraViews(p, level, view, viewTicks)
        if ((ticks - BUILD_AT) % VIEW_TICKS != 0) return
        if (view >= VIEWS.size) {
            if (view == VIEWS.size) Femtocraft.LOGGER.info("SHOWCASE done")
            return
        }
        // The last view also fills the player's nanites, so the HUD shows the nanite gauge sliding in, and hands the
        // player frames for a germination chamber, so the frame preview outlines it where the camera looks.
        if (view == FRAMES_VIEW) {
            p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                FrameItem.withSelection(net.minecraft.world.item.ItemStack(com.itszuvalex.femtocraft.industry.IndustryContent.FRAME_ITEM.get(), 64), FrameMultiblocks.GERMINATION_CHAMBER.id))
            val tank = com.itszuvalex.femtocraft.nanite.PlayerNanites.tank(p)
            tank.fill(com.itszuvalex.femtocraft.nanite.NaniteRegistry.dumb(60), true)
            com.itszuvalex.femtocraft.nanite.PlayerNanites.save(p, tank)
        }
        look(p, view)
        Femtocraft.LOGGER.info("SHOWCASE view {}", view)
    }

    /**
     * The views for the storage multiblock items and the channel panel. Item frames hold the three machine items (and a
     * full item vault), the next view holds one in hand, the next drops them on the ground; then the dev channel
     * block's menu opens on the channel tab, a channel is deleted under it, and the team's channels and a delete
     * confirmation show (the last by the client half).
     */
    private fun extraViews(p: ServerPlayer, level: ServerLevel, view: Int, t: Int) {
        val server = level.server
        fun run(command: String) = server.commands.performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), command)
        fun at(x: Int, y: Int, z: Int) = BASE.offset(x, y, z)
        fun stackOf(multi: com.itszuvalex.femtocraft.industry.FrameMultiblock, full: Boolean): net.minecraft.world.item.ItemStack {
            val state = multi.newState() as com.itszuvalex.femtocraft.industry.PackedState
            if (full && state is com.itszuvalex.femtocraft.logistics.ItemVaultState) {
                state.storage.setSlot(0, IItemStack.of(net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND, 40)))
                state.storage.setSlot(1, IItemStack.of(net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_INGOT, 12)))
            }
            return com.itszuvalex.femtocraft.industry.PackedMultiblocks.pack(multi, state, level.registryAccess())
        }
        val multis = listOf(FrameMultiblocks.ITEM_VAULT, FrameMultiblocks.FLUID_RESERVOIR, FrameMultiblocks.NANITE_VAULT)
        if (view != HAND_VIEW && t == 1) p.setGameMode(GameType.SPECTATOR)
        if (view == ITEMS_VIEW && t == 1) {
            for (x in 26..34) for (y in 0..2) level.setBlockAndUpdate(at(x, y, 8), Blocks.SMOOTH_STONE.defaultBlockState())
            val frame = net.minecraft.world.entity.EntityType.ITEM_FRAME
            listOf(multis[0] to false, multis[1] to false, multis[2] to false, multis[0] to true).forEachIndexed { i, (multi, full) ->
                val item = net.minecraft.world.entity.decoration.ItemFrame(level, at(27 + i * 2, 1, 7), net.minecraft.core.Direction.NORTH)
                item.setItem(stackOf(multi, full), false)
                level.addFreshEntity(item)
            }
        }
        if (view == HAND_VIEW && t == 1) {
            p.setGameMode(GameType.CREATIVE)
            p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, stackOf(multis[0], true))
        }
        if (view == DROPPED_VIEW && t == 1) {
            p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY)
            multis.forEachIndexed { i, multi ->
                val e = net.minecraft.world.entity.item.ItemEntity(level, BASE.x + 28.5 + i * 1.5, BASE.y + 0.3, BASE.z + 6.0, stackOf(multi, i == 0))
                e.setNoPickUpDelay()
                e.setDeltaMovement(0.0, 0.0, 0.0)
                level.addFreshEntity(e)
            }
        }
        // The channel views: two dev channel blocks, a few channels, the first block on "Alpha".
        if (view == CHANNELS_VIEW && t == 1) {
            p.closeContainer()
            level.getEntitiesOfClass(net.minecraft.world.entity.Entity::class.java, net.minecraft.world.phys.AABB(Vec3.atLowerCornerOf(BASE).add(20.0, -2.0, -6.0), Vec3.atLowerCornerOf(BASE).add(40.0, 6.0, 8.0)))
                .filter { it !is ServerPlayer }.forEach { it.discard() }
            val channels = com.itszuvalex.itszulib.ItszuLib.CHANNELS
            for (resource in listOf(com.itszuvalex.itszulib.dev.DevChannelBlockEntity.POWER, com.itszuvalex.itszulib.dev.DevChannelBlockEntity.ITEMS)) {
                com.itszuvalex.itszulib.channel.ChannelResources.register(resource, net.minecraft.network.chat.Component.literal(resource.path.removePrefix("dev_").replaceFirstChar { it.uppercase() }))
            }
            level.setBlockAndUpdate(at(CHANNEL_BLOCK.x, 0, CHANNEL_BLOCK.z), com.itszuvalex.itszulib.dev.DevContent.DEV_CHANNEL_BLOCK.get().defaultBlockState())
            level.setBlockAndUpdate(at(CHANNEL_BLOCK_2.x, 0, CHANNEL_BLOCK_2.z), com.itszuvalex.itszulib.dev.DevContent.DEV_CHANNEL_BLOCK.get().defaultBlockState())
            val power = com.itszuvalex.itszulib.dev.DevChannelBlockEntity.POWER
            val existing = channels.visible(p.uuid, power, com.itszuvalex.itszulib.channel.ChannelScope.PLAYER).map { it.name }
            for (name in listOf("Alpha", "Beta", "Workshop north", "Far field")) if (name !in existing) channels.create(p.uuid, com.itszuvalex.itszulib.channel.ChannelScope.PLAYER, power, name)
            val alpha = channels.visible(p.uuid, power, com.itszuvalex.itszulib.channel.ChannelScope.PLAYER).first { it.name == "Alpha" }
            for (pos in listOf(CHANNEL_BLOCK, CHANNEL_BLOCK_2)) {
                (level.getBlockEntity(at(pos.x, 0, pos.z)) as? com.itszuvalex.itszulib.dev.DevChannelBlockEntity)?.channels?.bind(power, p.uuid, alpha)
            }
            if (channels.visible(p.uuid, power, com.itszuvalex.itszulib.channel.ChannelScope.TEAM).none { it.name == "Shared base" }) {
                channels.create(p.uuid, com.itszuvalex.itszulib.channel.ChannelScope.TEAM, power, "Shared base")
            }
        }
        if (view == CHANNELS_VIEW && t == MENU_DELAY) {
            (level.getBlockEntity(at(CHANNEL_BLOCK.x, 0, CHANNEL_BLOCK.z)) as? com.itszuvalex.itszulib.api.adapters.IBlockEntity)
                ?.getModule(com.itszuvalex.itszulib.api.Modules.MENU, null)?.let { p.openMenu(it, it.menuPos()) }
        }
        // Delete "Alpha" under the open menu: both blocks are waiting for it.
        if (view == CHANNELS_VIEW + 1 && t == 1) {
            val power = com.itszuvalex.itszulib.dev.DevChannelBlockEntity.POWER
            com.itszuvalex.itszulib.ItszuLib.CHANNELS.visible(p.uuid, power, com.itszuvalex.itszulib.channel.ChannelScope.PLAYER).firstOrNull { it.name == "Alpha" }
                ?.let { com.itszuvalex.itszulib.ItszuLib.CHANNELS.delete(p.uuid, it.id) }
        }
        // The team's channels.
        if (view == CHANNELS_VIEW + 2 && t == 1) {
            (p.containerMenu as? com.itszuvalex.itszulib.menu.MenuCore)?.channels?.handle(com.itszuvalex.itszulib.menu.MenuChannels.ACTION_SCOPE, 1)
        }
        // More channels than fit, with names too long for a row.
        if (view == CHANNELS_VIEW + 4 && t == 1) {
            val power = com.itszuvalex.itszulib.dev.DevChannelBlockEntity.POWER
            val existing = com.itszuvalex.itszulib.ItszuLib.CHANNELS.visible(p.uuid, power, com.itszuvalex.itszulib.channel.ChannelScope.PLAYER).map { it.name }
            for (i in 1..14) {
                val name = "Workshop north-east line %02d".format(i)
                if (name !in existing) com.itszuvalex.itszulib.ItszuLib.CHANNELS.create(p.uuid, com.itszuvalex.itszulib.channel.ChannelScope.PLAYER, power, name)
            }
        }
        // Back to the player's own, with a block on "Beta" so the confirmation says it is in use; the client half then asks
        // before deleting the first channel in the list.
        if (view == CHANNELS_VIEW + 3 && t == 1) {
            val power = com.itszuvalex.itszulib.dev.DevChannelBlockEntity.POWER
            com.itszuvalex.itszulib.ItszuLib.CHANNELS.visible(p.uuid, power, com.itszuvalex.itszulib.channel.ChannelScope.PLAYER).firstOrNull { it.name == "Beta" }?.let { beta ->
                (level.getBlockEntity(at(CHANNEL_BLOCK.x, 0, CHANNEL_BLOCK.z)) as? com.itszuvalex.itszulib.dev.DevChannelBlockEntity)?.channels?.bind(power, p.uuid, beta)
            }
            (p.containerMenu as? com.itszuvalex.itszulib.menu.MenuCore)?.channels?.handle(com.itszuvalex.itszulib.menu.MenuChannels.ACTION_SCOPE, 0)
        }
    }

    private fun codex(p: ServerPlayer, level: ServerLevel) {
        val team = com.itszuvalex.itszulib.ItszuLib.TEAMS.state.teamOf(p.uuid)?.id ?: return
        val tech = { path: String -> net.minecraft.resources.Identifier.fromNamespaceAndPath(Femtocraft.ID, path) }
        com.itszuvalex.itszulib.research.TechTree.queue(level.server, team, tech("machining"))
        com.itszuvalex.itszulib.research.TechTree.queue(level.server, team, tech("basic_circuits"))
        com.itszuvalex.itszulib.research.TechTree.focus(level.server, team, com.itszuvalex.femtocraft.archive.ArchiveContent.TREE)
            ?.let { com.itszuvalex.itszulib.research.TechTree.addProgress(level.server, team, it, 12) }
        val archive = { x: Int, status: com.itszuvalex.femtocraft.archive.ArchiveStatus ->
            com.itszuvalex.femtocraft.archive.ArchiveRegistry.report(
                com.itszuvalex.femtocraft.archive.ArchiveRecord(net.minecraft.core.GlobalPos.of(level.dimension(), BASE.offset(x, 0, 12)), p.uuid, status))
        }
        archive(0, com.itszuvalex.femtocraft.archive.ArchiveStatus.RESEARCHING)
        archive(6, com.itszuvalex.femtocraft.archive.ArchiveStatus.NO_HOST)
        com.itszuvalex.femtocraft.archive.CodexItem.open(p)
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
        for (x in -3..34) for (z in -3..8) {
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
        (level.getBlockEntity(BASE.offset(10, 0, 5)) as? com.itszuvalex.femtocraft.industry.FrameBlockEntity)?.markDirtyAndSync()
        // A crystal liquifier between a logistics conduit and a chest, under a block, with its top set to pull items
        // in and its east face to push fluid out.
        level.setBlockAndUpdate(BASE.offset(LIQUIFIER), com.itszuvalex.femtocraft.industry.IndustryContent.CRYSTAL_LIQUIFIER.get().defaultBlockState()
            .setValue(com.itszuvalex.itszulib.core.HorizontalFacing.FACING, net.minecraft.core.Direction.NORTH))
        set(LIQUIFIER.x - 1, 0, LIQUIFIER.z, LogisticsContent.CONDUIT.get())
        set(LIQUIFIER.x + 1, 0, LIQUIFIER.z, Blocks.CHEST)
        set(LIQUIFIER.x, 1, LIQUIFIER.z, Blocks.SMOOTH_STONE)
        val liquifier = level.getBlockEntity(BASE.offset(LIQUIFIER)) as? com.itszuvalex.itszulib.api.adapters.IBlockEntity
        liquifier?.getModule(com.itszuvalex.itszulib.api.Modules.ITEM_STORAGE_CONFIGURABLE, null)?.let {
            com.itszuvalex.femtocraft.industry.ConfiguratorItem.cycle(it, net.minecraft.core.Direction.UP, false)
            com.itszuvalex.femtocraft.industry.ConfiguratorItem.cycle(it, net.minecraft.core.Direction.WEST, false)
            com.itszuvalex.femtocraft.industry.ConfiguratorItem.cycle(it, net.minecraft.core.Direction.WEST, false)
        }
        (liquifier as? com.itszuvalex.itszulib.core.BlockEntityCore)?.markDirtyAndSync()
        // Computation: a mainframe with two Micro Logic Cores and ice beside it, a computation conduit to an Archive
        // Interface and a logistics conduit.
        val computation = com.itszuvalex.femtocraft.computation.ComputationContent
        level.setBlockAndUpdate(BASE.offset(MAINFRAME), computation.MAINFRAME.get().defaultBlockState()
            .setValue(com.itszuvalex.itszulib.core.HorizontalFacing.FACING, net.minecraft.core.Direction.NORTH))
        (level.getBlockEntity(BASE.offset(MAINFRAME)) as? com.itszuvalex.femtocraft.computation.MainframeBlockEntity)?.let { mf ->
            repeat(2) { mf.processors.setSlot(it, IItemStack.of(net.minecraft.world.item.ItemStack(computation.MICRO_LOGIC_CORE.get()))) }
            mf.battery.setStorage(com.itszuvalex.femtocraft.computation.MainframeBlockEntity.BATTERY_SIZE)
        }
        set(MAINFRAME.x - 1, 0, MAINFRAME.z, Blocks.PACKED_ICE)
        for (x in MAINFRAME.x + 1..MAINFRAME.x + 2) set(x, 0, MAINFRAME.z, computation.COMPUTATION_CONDUIT.get())
        set(MAINFRAME.x + 2, 1, MAINFRAME.z, computation.COMPUTATION_CONDUIT.get())
        set(MAINFRAME.x + 3, 0, MAINFRAME.z, computation.ARCHIVE_INTERFACE.get())
        set(MAINFRAME.x + 2, 0, MAINFRAME.z + 1, LogisticsContent.CONDUIT.get())
        // The alpha's cryo-endothermal charging base on two coils, ice beside the lower one, water in reach.
        set(15, 0, 5, PowerContent.CRYO_COIL.get())
        set(15, 1, 5, PowerContent.CRYO_COIL.get())
        set(15, 2, 5, PowerContent.CRYO_BASE.get())
        set(16, 0, 5, Blocks.ICE)
        set(15, 0, 4, Blocks.PACKED_ICE)
        set(16, 0, 4, Blocks.WATER)
        // The alpha's atmospheric charging pole: a base, three coils and a capacitor.
        set(POLE.x, 0, POLE.z, PowerContent.ATMOSPHERIC_BASE.get())
        for (y in 1..3) set(POLE.x, y, POLE.z, PowerContent.ATMOSPHERIC_COIL.get())
        set(POLE.x, 4, POLE.z, PowerContent.ATMOSPHERIC_CAPACITOR.get())
        // Filtered chips in the first logistics conduit: items (diamonds, iron, redstone) and fluids (water, lava).
        (level.getBlockEntity(BASE.offset(10, 0, 2)) as? com.itszuvalex.femtocraft.logistics.ConduitBlockEntity)?.let { conduit ->
            val up = net.minecraft.core.Direction.UP
            val item = com.itszuvalex.femtocraft.logistics.ItemChipKind
            val fluid = com.itszuvalex.femtocraft.logistics.FluidChipKind
            val itemChip = net.minecraft.world.item.ItemStack(LogisticsContent.ITEM_CHIP.get()).also { st ->
                st.set(item.component, item.defaults(up).with(filter = com.itszuvalex.itszulib.api.filter.ResourceFilter(item.filterKind, com.itszuvalex.femtocraft.logistics.ChipKind.FILTER_SLOTS,
                    listOf(net.minecraft.world.item.Items.DIAMOND, net.minecraft.world.item.Items.IRON_INGOT, net.minecraft.world.item.Items.REDSTONE).map { net.minecraft.world.item.ItemStack(it) })))
            }
            val fluidChip = net.minecraft.world.item.ItemStack(LogisticsContent.FLUID_CHIP.get()).also { st ->
                st.set(fluid.component, fluid.defaults(up).with(filter = com.itszuvalex.itszulib.api.filter.ResourceFilter(fluid.filterKind, com.itszuvalex.femtocraft.logistics.ChipKind.FILTER_SLOTS,
                    listOf(net.minecraft.world.level.material.Fluids.WATER, net.minecraft.world.level.material.Fluids.LAVA).map { net.neoforged.neoforge.fluids.FluidStack(it, 1) }, com.itszuvalex.itszulib.api.filter.FilterMode.DENY)))
            }
            conduit.conduit.chips[up.get3DDataValue()].setSlot(0, IItemStack.of(itemChip))
            conduit.conduit.chips[up.get3DDataValue()].setSlot(1, IItemStack.of(fluidChip))
        }
        // The storage multiblocks; the item vault holds a little of many items (more than a terminal page).
        FrameMultiblocks.ITEM_VAULT.formAt(level, BASE.offset(ITEM_VAULT))
        FrameMultiblocks.FLUID_RESERVOIR.formAt(level, BASE.offset(ITEM_VAULT).offset(4, 0, 0))
        FrameMultiblocks.NANITE_VAULT.formAt(level, BASE.offset(ITEM_VAULT).offset(8, 0, 0))
        // The reservoir: tank 2 locked to lava, cells 3 and 4 linked into one tank, then water, lava and slurry poured in.
        (level.getBlockEntity(BASE.offset(ITEM_VAULT).offset(4, 0, 0)) as? com.itszuvalex.femtocraft.logistics.FluidReservoirBlockEntity)?.state()?.let { res ->
            res.toggleLock(1, net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(net.minecraft.world.level.material.Fluids.LAVA))
            res.toggleLink(2)
            val cap = com.itszuvalex.femtocraft.logistics.FluidReservoirState.CAPACITY
            listOf(net.minecraft.world.level.material.Fluids.WATER to cap * 3 / 4, net.minecraft.world.level.material.Fluids.LAVA to cap / 3,
                com.itszuvalex.femtocraft.industry.FemtoFluids.GRITTY_SLURRY.get() to cap * 3 / 2)
                .forEach { (fluid, amount) -> res.tanks.fill(com.itszuvalex.itszulib.api.adapters.IFluidStack.of(net.neoforged.neoforge.fluids.FluidStack(fluid, amount)), true) }
        }
        (level.getBlockEntity(BASE.offset(ITEM_VAULT)) as? com.itszuvalex.femtocraft.logistics.ItemVaultBlockEntity)?.state()?.storage?.let { vault ->
            net.minecraft.core.registries.BuiltInRegistries.ITEM.stream().filter { it != net.minecraft.world.item.Items.AIR }.limit(80).toList().forEachIndexed { i, item ->
                vault.insert(IItemStack.of(net.minecraft.world.item.ItemStack(item, minOf(item.defaultMaxStackSize, 1 + (i * 37) % 64))))
                if (i % 7 == 0) repeat(4) { vault.insert(IItemStack.of(net.minecraft.world.item.ItemStack(item, item.defaultMaxStackSize))) }
            }
        }
    }
}
