package com.itszuvalex.femtocraft.archive

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.FemtoRegistries
import com.itszuvalex.femtocraft.core.FemtoEntityBlock
import com.itszuvalex.femtocraft.core.FemtoMenu
import com.itszuvalex.femtocraft.industry.FrameMachineBlockEntity
import com.itszuvalex.femtocraft.industry.FrameMultiblock
import com.itszuvalex.femtocraft.industry.FrameMultiblocks
import com.itszuvalex.femtocraft.industry.IndustryContent
import com.itszuvalex.itszulib.ItszuLib
import com.itszuvalex.itszulib.api.adapters.ILevel
import com.itszuvalex.itszulib.api.multiblock.IMultiblockState
import com.itszuvalex.itszulib.api.multiblock.MultiblockInstance
import com.itszuvalex.itszulib.core.frag.FragMultiblockTickable
import com.itszuvalex.itszulib.core.frag.addTickableFragment
import com.itszuvalex.itszulib.menu.MenuSyncs
import com.itszuvalex.itszulib.menu.MenuSync
import com.itszuvalex.itszulib.research.TechnologyResearchedEvent
import com.itszuvalex.itszulib.team.TeamMembershipChangedEvent
import net.minecraft.core.GlobalPos
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.common.NeoForge
import com.itszuvalex.itszulib.research.TechTree
import net.minecraft.core.BlockPos
import net.minecraft.core.RegistryAccess
import net.minecraft.core.UUIDUtil
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.InteractionResult
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import java.util.UUID
import java.util.function.UnaryOperator

/**
 * What the Archive is doing, for its screen and the team's Archive list.
 */
enum class ArchiveStatus {
    /** Unclaimed, or its team has nothing in Femtocraft's tree queued that it can research now. */
    IDLE,

    /** Drawing nanites from a host and making progress on the team's focus. */
    RESEARCHING,

    /** The team has a focus, but no host of the team with Archive nanites is in range. */
    NO_HOST,

    /** The focus has all its points and waits for items to be handed in (the Archive's or Codex's "Offer items"). */
    NEEDS_ITEMS,
}

/**
 * Shared state of the Archive (on its home block): who claimed it (the first player to use it; it researches for that
 * player's team, whichever that is now), and research points bought with nanites but not yet spent.
 *
 * Every Archive of a team works on the team's focus: the first technology of Femtocraft's tree in the team's research
 * queue that it can research now ([TechTree.focus]). Every [STEP_TICKS] ticks: if fewer than [RATE] points are
 * buffered, it draws one Archive nanite from the nearest host of the team within [RADIUS] blocks
 * ([NaniteHost.nearbyHosts]) for [POINTS_PER_NANITE] points, then spends up to [RATE] points on the focus
 * ([TechTree.addProgress], which keeps what is not needed). Points left over go to the next focus. More Archives with
 * more hosts research faster; one host feeds about one Archive.
 *
 * Archive Interfaces add computed points ([addComputed], from FLOPS; DECISIONS D19), spent at up to [COMPUTED_RATE] a
 * step on top of the nanite points, so computation speeds research up without replacing the host.
 */
class ArchiveState(private val onChanged: Runnable) : IMultiblockState {
    var owner: UUID? = null
        private set
    var points: Long = 0L
        private set

    /** Research points computed by Archive Interfaces ([addComputed]), spent alongside the nanite points. */
    var computedPoints: Long = 0L
        private set

    /** Not saved: recomputed each step. */
    var status: ArchiveStatus = ArchiveStatus.IDLE

    /** What was last reported to [ArchiveRegistry], so it is written only on change. Not saved. */
    private var reported: ArchiveRecord? = null

    /**
     * The team this Archive researches for: its owner's current team.
     */
    fun team(): UUID? = owner?.let { ItszuLib.TEAMS.state.teamOf(it)?.id }

    /**
     * Claims an unclaimed Archive for [player]. @return True if it was unclaimed.
     */
    fun claim(player: UUID): Boolean {
        if (owner != null) return false
        owner = player
        onChanged.run()
        return true
    }

    /** How many more computed points this Archive takes now (it keeps at most [COMPUTED_CAP]). */
    fun computedRoom(): Long = (COMPUTED_CAP - computedPoints).coerceAtLeast(0L)

    /**
     * Adds computed research points (up to [computedRoom]). @return How many were taken.
     */
    fun addComputed(amount: Long): Long {
        val taken = amount.coerceIn(0L, computedRoom())
        if (taken > 0L) {
            computedPoints += taken
            onChanged.run()
        }
        return taken
    }

    /**
     * Whether this Archive has something to research now: claimed, and its team has a focus in Femtocraft's tree.
     */
    fun hasFocus(server: net.minecraft.server.MinecraftServer): Boolean = team()?.let { TechTree.focus(server, it, ArchiveContent.TREE) } != null

    /**
     * One research step; [hosts] are the candidate hosts, nearest first, [center] is where drawn nanites flow to, and
     * [home] (the home block) keys this Archive in [ArchiveRegistry].
     *
     * @return The progress made.
     */
    fun step(level: ServerLevel, hosts: List<Player>, center: Vec3 = Vec3.ZERO, home: GlobalPos? = null): Long {
        val used = research(level, hosts, center)
        val owner = owner
        if (home != null && owner != null) {
            val record = ArchiveRecord(home, owner, status)
            if (record != reported) {
                ArchiveRegistry.report(record)
                reported = record
            }
        }
        return used
    }

    private fun research(level: ServerLevel, hosts: List<Player>, center: Vec3): Long {
        val team = team()
        val tech = team?.let { TechTree.focus(level.server, it, ArchiveContent.TREE) }
        if (team == null || tech == null) {
            status = ArchiveStatus.IDLE
            return 0L
        }
        val research = ItszuLib.TEAMS.state.team(team)?.get(com.itszuvalex.itszulib.team.Research.TYPE)
        val remaining = research?.let { TechTree.of(level.registryAccess()).remaining(tech, it) }
        if (remaining != null && remaining.points <= 0L && !remaining.complete) {
            status = ArchiveStatus.NEEDS_ITEMS
            return 0L
        }
        if (points < RATE) {
            val host = hosts.firstOrNull { ItszuLib.TEAMS.state.teamOf(it.uuid)?.id == team }
            if (host != null && NaniteHost.drawTo(host, 1, level, center) == 1) {
                points += POINTS_PER_NANITE
                onChanged.run()
            }
        }
        if (points <= 0L && computedPoints <= 0L) {
            status = ArchiveStatus.NO_HOST
            return 0L
        }
        status = ArchiveStatus.RESEARCHING
        var used = 0L
        if (points > 0L) {
            val spent = TechTree.addProgress(level.server, team, tech, minOf(points, RATE))
            points -= spent
            used += spent
        }
        // Computed points go to the focus as it is now (the nanite points may just have finished the last one).
        val next = if (computedPoints > 0L) TechTree.focus(level.server, team, ArchiveContent.TREE) else null
        if (next != null) {
            val spent = TechTree.addProgress(level.server, team, next, minOf(computedPoints, COMPUTED_RATE))
            computedPoints -= spent
            used += spent
        }
        if (used > 0L) onChanged.run()
        return used
    }

    override fun onBreak(level: ILevel, anchor: BlockPos, brokenAt: BlockPos) {
        val mc = level.toMinecraft() as? ServerLevel ?: return
        ArchiveRegistry.remove(GlobalPos.of(mc.dimension(), anchor))
    }

    override fun serialize(output: ValueOutput) {
        owner?.let { output.store("Owner", UUIDUtil.CODEC, it) }
        output.putLong("Points", points)
        output.putLong("Computed", computedPoints)
    }

    override fun deserialize(input: ValueInput) {
        owner = input.read("Owner", UUIDUtil.CODEC).orElse(null)
        points = input.getLongOr("Points", 0L)
        computedPoints = input.getLongOr("Computed", 0L)
    }

    companion object {
        const val STEP_TICKS = 20
        const val RATE = 5L
        const val POINTS_PER_NANITE = 10L
        const val RADIUS = 8.0

        /** Computed points spent per step, on top of [RATE] from nanites. */
        const val COMPUTED_RATE = 20L

        /** At most this many computed points wait to be spent. */
        const val COMPUTED_CAP = COMPUTED_RATE * 4
    }
}

/**
 * A block of the Archive (3x3x3, built from frames): the first machine a nanite host builds, and where research
 * happens. Its menu shows Femtocraft's tech tree ([ArchiveContent.TREE]).
 */
class ArchiveBlockEntity(pos: BlockPos, state: BlockState) :
    FrameMachineBlockEntity<ArchiveState>(ArchiveContent.ARCHIVE_BE.get(), pos, state, { ArchiveContent.MULTIBLOCK }) {
    init {
        addFrameMachineFragments("block.femtocraft.archive") { id, inv -> ArchiveMenu(id, inv, this) }
        fragList.addTickableFragment(object : FragMultiblockTickable(part) {
            override fun name(): String = "Archive"
            override fun serverStructureTick(level: ILevel, instance: MultiblockInstance) {
                val server = level.toMinecraft() as? ServerLevel ?: return
                if (server.gameTime % ArchiveState.STEP_TICKS != 0L) return
                val state = instance.state as? ArchiveState ?: return
                val center = instance.anchorPos.offset(1, 1, 1)
                state.step(server, NaniteHost.nearbyHosts(server, center, ArchiveState.RADIUS), center.center, GlobalPos.of(server.dimension(), instance.anchorPos))
            }
        })
    }

    /**
     * Whether [player] may use this Archive: it is formed, and unclaimed or claimed by someone in [player]'s team.
     */
    fun canAccess(player: Player): Boolean {
        val state = state() ?: return false
        val team = state.owner?.let { ItszuLib.TEAMS.state.teamOf(it)?.id } ?: return true
        return ItszuLib.TEAMS.state.teamOf(player.uuid)?.id == team
    }
}

/**
 * Using an Archive claims it if nobody has ([ArchiveState.claim]), opens its menu, and is first contact
 * ([NaniteHost.contact]) for a player who has access ([ArchiveBlockEntity.canAccess]) and is not a host yet: a second
 * way in besides touching a crystal cluster, for players joining a team that already built one.
 */
class ArchiveBlock(p: BlockBehaviour.Properties) : FemtoEntityBlock<ArchiveBlockEntity>(p, { ArchiveContent.ARCHIVE_BE.get() }) {
    override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hitResult: BlockHitResult): InteractionResult {
        val be = level.getBlockEntity(pos) as? ArchiveBlockEntity
        if (!level.isClientSide && be != null && be.canAccess(player)) {
            be.state()?.claim(player.uuid)
            NaniteHost.contact(player)
        }
        return super.useWithoutItem(state, level, pos, player, hitResult)
    }
}

/**
 * The Archive's menu: no slots; syncs this Archive's points and status and the team's Archives, and takes the research
 * queue actions ([ArchiveResearch]).
 */
class ArchiveMenu(containerId: Int, inventory: Inventory, be: ArchiveBlockEntity?) :
    FemtoMenu<ArchiveBlockEntity>(ArchiveContent.ARCHIVE_MENU.get(), containerId, inventory, be) {
    /** Client copies. */
    var points = 0L
        private set
    var computedPoints = 0L
        private set
    var status = ArchiveStatus.IDLE
        private set
    var archives: List<ArchiveRecord> = emptyList()
        private set

    init {
        addSync(MenuSyncs.long({ be?.state()?.points ?: 0L }, { points = it }))
        addSync(MenuSyncs.long({ be?.state()?.computedPoints ?: 0L }, { computedPoints = it }))
        addSync(MenuSyncs.int({ (be?.state()?.status ?: ArchiveStatus.IDLE).ordinal }, { status = ArchiveStatus.entries.getOrElse(it) { ArchiveStatus.IDLE } }))
        addSync(ArchiveResearch.archivesSync(inventory.player) { archives = it })
    }

    override fun handleAction(player: Player, action: Int, data: Int): Boolean = ArchiveResearch.handleAction(player, action, data)
}

/**
 * Research actions shared by the Archive and the Codex: queue a technology of Femtocraft's tree (after its missing
 * prerequisites) or take it off the player's team's queue, sent with the technology's synced registry id.
 */
object ArchiveResearch {
    const val ACTION_QUEUE = 0
    const val ACTION_UNQUEUE = 1

    /** Hand in what the team's focus still needs of its items, from the player's inventory. */
    const val ACTION_DELIVER = 2

    fun handleAction(player: Player, action: Int, data: Int): Boolean {
        if (action == ACTION_DELIVER) return deliver(player) > 0
        if (action != ACTION_QUEUE && action != ACTION_UNQUEUE) return false
        val server = player.level().server ?: return false
        val tech = byNetworkId(player.level().registryAccess(), data) ?: return false
        if (TechTree.of(player.level().registryAccess())[tech]?.tree != ArchiveContent.TREE) return false
        val team = ItszuLib.TEAMS.state.teamOf(player.uuid) ?: return false
        if (action == ACTION_QUEUE) TechTree.queue(server, team.id, tech) else TechTree.unqueue(server, team.id, tech)
        return true
    }

    /**
     * Hands in what [player]'s team's focus in Femtocraft's tree still needs of its items, from their inventory.
     *
     * @return How many items were taken.
     */
    fun deliver(player: Player): Int {
        val server = player.level().server ?: return 0
        val team = ItszuLib.TEAMS.state.teamOf(player.uuid)?.id ?: return 0
        val focus = TechTree.focus(server, team, ArchiveContent.TREE) ?: return 0
        val taken = TechTree.deliverFrom(server, team, focus, player.inventory.nonEquipmentItems)
        if (taken > 0) player.inventory.setChanged()
        return taken
    }

    /**
     * Keeps a menu's copy of [player]'s team's Archives ([ArchiveRegistry.forTeam]) in step.
     */
    fun archivesSync(player: Player, setter: (List<ArchiveRecord>) -> Unit) = MenuSync(
        { if (player.level().isClientSide) emptyList() else ArchiveRegistry.forTeam(ItszuLib.TEAMS.state.teamOf(player.uuid)?.id) },
        setter,
        ArchiveRecord.LIST_STREAM_CODEC,
    )

    /** The technology's id in the synced registry (the same on server and client), or -1. */
    fun networkId(access: RegistryAccess, id: Identifier?): Int {
        val registry = access.lookup(TechTree.KEY).orElse(null) ?: return -1
        return id?.let(registry::getValue)?.let(registry::getId) ?: -1
    }

    fun byNetworkId(access: RegistryAccess, id: Int): Identifier? {
        if (id < 0) return null
        val registry = access.lookup(TechTree.KEY).orElse(null) ?: return null
        return registry.byId(id)?.let(registry::getKey)
    }
}

/**
 * The Archive's registrations and frame multiblock.
 */
object ArchiveContent {
    private val R = FemtoRegistries

    /** Femtocraft's tech tree (the JSON files in `data/femtocraft/itszulib/technology`). */
    @JvmField
    val TREE: Identifier = Identifier.fromNamespaceAndPath(Femtocraft.ID, "archive")

    @JvmField val ARCHIVE = R.BLOCKS.registerBlock("archive", ::ArchiveBlock, UnaryOperator { it.strength(2f).sound(SoundType.METAL).requiresCorrectToolForDrops() })
    @JvmField val ARCHIVE_BE = R.blockEntity("archive", ::ArchiveBlockEntity, ARCHIVE::get)
    @JvmField val ARCHIVE_MENU = R.blockMenu<ArchiveBlockEntity, ArchiveMenu>("archive", ::ArchiveMenu)
    @JvmField val CODEX = R.ITEMS.registerItem("codex", ::CodexItem, UnaryOperator { it.stacksTo(1) })
    @JvmField val CODEX_MENU = R.MENUS.register("codex") { -> net.minecraft.world.inventory.MenuType(::CodexMenu, net.minecraft.world.flag.FeatureFlags.VANILLA_SET) }

    /**
     * 27 frames plus a first offering: crackling dust from crystal clusters and glass. Placeholder costs.
     */
    @JvmField
    val MULTIBLOCK: FrameMultiblock = FrameMultiblocks.register(
        FrameMultiblock("archive", setOf(FrameMultiblocks.BASIC), Triple(3, 3, 3),
            { listOf(ItemStack(IndustryContent.CRACKLING_DUST.get(), 8), ItemStack(Items.GLASS, 9)) }, { ARCHIVE.get() }, ::ArchiveState),
    )

    fun init() {
        NaniteHost.ATTACHMENT
        NaniteHost.init()
        ArchiveRegistry.init()
        NeoForge.EVENT_BUS.addListener(::announce)
        NeoForge.EVENT_BUS.addListener(::teamChanged)
    }

    /**
     * When a player joins or leaves a team, their Archives go with them (an Archive researches for its owner's current
     * team). Joining merged their research into the team's, so their Archives drop anything the team already
     * researched and take up the team's focus; leaving gave them a copy of the team's research and queue, so their
     * Archives carry on with the same focus, now for them alone. Each Archive picks this up on its next step; this
     * tells the player.
     */
    private fun teamChanged(event: TeamMembershipChangedEvent) {
        if (event.from == null) return
        val count = ArchiveRegistry.state.values.count { it.owner == event.player }
        if (count == 0) return
        event.server.playerList.getPlayer(event.player)?.sendSystemMessage(Component.translatable("archive.femtocraft.team_changed", count, event.to.name))
    }

    /**
     * Tells a team's online members when it researches a technology of Femtocraft's tree, once however many Archives
     * worked on it.
     */
    private fun announce(event: TechnologyResearchedEvent) {
        val tech = TechTree.of(event.server.registryAccess())[event.technology] ?: return
        if (tech.tree != TREE) return
        val members = ItszuLib.TEAMS.state.team(event.team)?.members?.keys ?: return
        val message = Component.translatable("archive.femtocraft.researched", tech.displayName(event.technology))
        for (player in event.server.playerList.players) if (player.uuid in members) player.sendSystemMessage(message)
    }
}
