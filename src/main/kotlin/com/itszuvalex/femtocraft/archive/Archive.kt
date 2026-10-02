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
import com.itszuvalex.itszulib.research.TechTree
import com.itszuvalex.itszulib.research.TechnologyState
import com.itszuvalex.itszulib.team.Research
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
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import java.util.UUID
import java.util.function.UnaryOperator

/**
 * What the Archive is doing, for its screen.
 */
enum class ArchiveStatus {
    /** No technology chosen. */
    IDLE,

    /** Drawing nanites from a host and making progress. */
    RESEARCHING,

    /** Chosen, but no host of the researching team with Archive nanites is in range. */
    NO_HOST,

    /** The chosen technology cannot be researched now (its prerequisites changed, or it vanished). */
    UNAVAILABLE,
}

/**
 * Shared state of the Archive (on its home block): the technology it researches, the team it researches for (whoever
 * chose the technology), and research points bought with nanites but not yet spent.
 *
 * Every [STEP_TICKS] ticks: if fewer than [RATE] points are buffered, it draws one Archive nanite from the nearest host
 * of that team within [RADIUS] blocks ([NaniteHost.nearbyHosts]) for [POINTS_PER_NANITE] points, then spends up to
 * [RATE] points on the technology ([TechTree.addProgress], which keeps what is not needed). When the technology is
 * researched the choice clears; points left over stay for the next one.
 */
class ArchiveState(private val onChanged: Runnable) : IMultiblockState {
    var technology: Identifier? = null
        private set
    var team: UUID? = null
        private set
    var points: Long = 0L
        private set

    /** Not saved: recomputed each step. */
    var status: ArchiveStatus = ArchiveStatus.IDLE

    /**
     * Researches [technology] for [team] from now on.
     */
    fun choose(technology: Identifier, team: UUID) {
        this.technology = technology
        this.team = team
        status = ArchiveStatus.RESEARCHING
        onChanged.run()
    }

    fun clear() {
        technology = null
        status = ArchiveStatus.IDLE
        onChanged.run()
    }

    /**
     * One research step; [hosts] are the candidate hosts, nearest first, and [center] is where drawn nanites flow to.
     *
     * @return The progress made.
     */
    fun step(level: ServerLevel, hosts: List<Player>, center: net.minecraft.world.phys.Vec3 = net.minecraft.world.phys.Vec3.ZERO): Long {
        val tech = technology
        val team = team
        if (tech == null || team == null) {
            status = ArchiveStatus.IDLE
            return 0L
        }
        val research = ItszuLib.TEAMS.state.team(team)?.get(Research.TYPE) ?: Research.EMPTY
        when (TechTree.of(level.registryAccess()).state(tech, research)) {
            TechnologyState.AVAILABLE -> {}
            TechnologyState.RESEARCHED -> {
                clear()
                return 0L
            }
            else -> {
                status = ArchiveStatus.UNAVAILABLE
                return 0L
            }
        }
        if (points < RATE) {
            val host = hosts.firstOrNull { ItszuLib.TEAMS.state.teamOf(it.uuid)?.id == team }
            if (host != null && NaniteHost.drawTo(host, 1, level, center) == 1) {
                points += POINTS_PER_NANITE
                onChanged.run()
            }
        }
        if (points <= 0L) {
            status = ArchiveStatus.NO_HOST
            return 0L
        }
        status = ArchiveStatus.RESEARCHING
        val used = TechTree.addProgress(level.server, team, tech, minOf(points, RATE))
        if (used > 0L) {
            points -= used
            onChanged.run()
        }
        val now = ItszuLib.TEAMS.state.team(team)?.get(Research.TYPE)
        if (now?.has(tech) == true) {
            announce(level, team, tech)
            clear()
        }
        return used
    }

    private fun announce(level: ServerLevel, team: UUID, tech: Identifier) {
        val name = TechTree.of(level.registryAccess())[tech]?.displayName(tech) ?: Component.literal(tech.toString())
        val members = ItszuLib.TEAMS.state.team(team)?.members?.keys ?: return
        for (player in level.server.playerList.players) {
            if (player.uuid in members) player.sendSystemMessage(Component.translatable("archive.femtocraft.researched", name))
        }
    }

    override fun serialize(output: ValueOutput) {
        technology?.let { output.store("Technology", Identifier.CODEC, it) }
        team?.let { output.store("Team", UUIDUtil.CODEC, it) }
        output.putLong("Points", points)
    }

    override fun deserialize(input: ValueInput) {
        technology = input.read("Technology", Identifier.CODEC).orElse(null)
        team = input.read("Team", UUIDUtil.CODEC).orElse(null)
        points = input.getLongOr("Points", 0L)
    }

    companion object {
        const val STEP_TICKS = 20
        const val RATE = 5L
        const val POINTS_PER_NANITE = 10L
        const val RADIUS = 8.0
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
                state.step(server, NaniteHost.nearbyHosts(server, center, ArchiveState.RADIUS), center.center)
            }
        })
    }
}

class ArchiveBlock(p: BlockBehaviour.Properties) : FemtoEntityBlock<ArchiveBlockEntity>(p, { ArchiveContent.ARCHIVE_BE.get() })

/**
 * The Archive's menu: no slots; syncs the chosen technology (by its synced registry id), points and status, and takes
 * [ACTION_CHOOSE] from the tech tree view.
 */
class ArchiveMenu(containerId: Int, inventory: Inventory, be: ArchiveBlockEntity?) :
    FemtoMenu<ArchiveBlockEntity>(ArchiveContent.ARCHIVE_MENU.get(), containerId, inventory, be) {
    private val access: RegistryAccess = inventory.player.level().registryAccess()

    /** Client copies. */
    var technology: Identifier? = null
        private set
    var points = 0L
        private set
    var status = ArchiveStatus.IDLE
        private set

    init {
        addSync(MenuSyncs.int({ networkId(access, be?.state()?.technology) }, { technology = byNetworkId(access, it) }))
        addSync(MenuSyncs.long({ be?.state()?.points ?: 0L }, { points = it }))
        addSync(MenuSyncs.int({ (be?.state()?.status ?: ArchiveStatus.IDLE).ordinal }, { status = ArchiveStatus.entries.getOrElse(it) { ArchiveStatus.IDLE } }))
    }

    override fun handleAction(player: Player, action: Int, data: Int): Boolean {
        if (action != ACTION_CHOOSE) return false
        val state = blockEntity?.state() ?: return false
        val tech = byNetworkId(access, data) ?: return false
        val team = ItszuLib.TEAMS.state.teamOf(player.uuid) ?: return false
        if (TechTree.of(access).state(tech, team[Research.TYPE]) != TechnologyState.AVAILABLE) return false
        state.choose(tech, team.id)
        return true
    }

    companion object {
        const val ACTION_CHOOSE = 0

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
    }
}
