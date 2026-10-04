package com.itszuvalex.femtocraft.dev

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.archive.ArchiveBlockEntity
import com.itszuvalex.femtocraft.archive.ArchiveContent
import com.itszuvalex.femtocraft.archive.ArchiveMenu
import com.itszuvalex.femtocraft.archive.ArchiveRegistry
import com.itszuvalex.femtocraft.archive.ArchiveResearch
import com.itszuvalex.femtocraft.archive.ArchiveState
import com.itszuvalex.femtocraft.archive.ArchiveStatus
import com.itszuvalex.femtocraft.archive.CodexMenu
import com.itszuvalex.femtocraft.archive.NaniteHost
import com.itszuvalex.femtocraft.worldgen.WorldgenContent
import com.itszuvalex.itszulib.ItszuLib
import com.itszuvalex.itszulib.research.TechTree
import com.itszuvalex.itszulib.research.TechnologyState
import com.itszuvalex.itszulib.team.Research
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.GlobalPos
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.Identifier
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameType
import net.minecraft.world.phys.BlockHitResult
import java.util.UUID

/**
 * Game tests for the Archive, the nanite host and Femtocraft's tech tree.
 */
object ArchiveGameTests {
    private val AT = BlockPos(3, 1, 3)
    private val METALLURGY = tech("metallurgy")
    private val BASIC_CIRCUITS = tech("basic_circuits")
    private val MACHINING = tech("machining")

    private fun tech(path: String) = Identifier.fromNamespaceAndPath(Femtocraft.ID, path)

    fun register() {
        DevGameTests.test("archive_tech_tree_loads", body = ::treeLoads)
        DevGameTests.test("crystal_cluster_touch_makes_a_nanite_host", body = ::contact)
        DevGameTests.test("archive_use_makes_a_nanite_host", body = ::archiveContact)
        DevGameTests.test("archive_use_needs_access_for_first_contact", body = ::archiveContactNeedsAccess)
        DevGameTests.test("nanite_host_regenerates_archive_nanites", body = ::regenerates)
        DevGameTests.test("archive_researches_with_host_nanites", body = ::researches)
        DevGameTests.test("archives_share_the_team_focus", body = ::shareFocus)
        DevGameTests.test("archive_and_codex_edit_the_team_queue", body = ::menuQueue)
        DevGameTests.test("archives_follow_their_owner_between_teams", body = ::followOwner)
        DevGameTests.test("archive_waits_for_items_then_rewards", body = ::items)
        DevGameTests.test("codex_opens_the_tech_tree", body = ::codexOpens)
        DevGameTests.test("host_draw_to_takes_nanites_for_a_consumer", body = ::drawTo)
    }

    private fun player(helper: GameTestHelper): Pair<Player, UUID> {
        val player = helper.makeMockPlayer(GameType.SURVIVAL)
        ItszuLib.TEAMS.change { it.ensurePlayer(player.uuid, "archive_test") }
        return player to ItszuLib.TEAMS.state.teamOf(player.uuid)!!.id
    }

    private fun formArchive(helper: GameTestHelper): ArchiveBlockEntity {
        helper.assertTrue(ArchiveContent.MULTIBLOCK.formAt(helper.level, helper.absolutePos(AT)), "archive formed")
        return helper.getBlockEntity(AT, ArchiveBlockEntity::class.java)
    }

    private fun treeLoads(helper: GameTestHelper) {
        val techs = TechTree.of(helper.level.registryAccess())
        helper.assertValueEqual(techs.inTree(ArchiveContent.TREE).size, 58, "technologies")
        helper.assertValueEqual(techs.problems(), emptyList<String>(), "problems")
        helper.assertTrue(techs.state(tech("macroscopic_structures"), Research.EMPTY) == TechnologyState.RESEARCHED, "root unlocked by default")
        helper.assertTrue(techs.state(METALLURGY, Research.EMPTY) == TechnologyState.AVAILABLE, "first technologies available")
        helper.assertTrue(techs.state(MACHINING, Research.EMPTY) == TechnologyState.LOCKED, "later technologies locked")
        helper.assertValueEqual(techs[METALLURGY]!!.displayName(METALLURGY).string, "Metallurgy", "named")
        helper.succeed()
    }

    private fun contact(helper: GameTestHelper) {
        val (player, _) = player(helper)
        helper.setBlock(AT, WorldgenContent.CRYSTAL_CLUSTER.get())
        val pos = helper.absolutePos(AT)
        fun touch() = helper.level.getBlockState(pos).useWithoutItem(helper.level, player, BlockHitResult(pos.center, Direction.UP, pos, false))
        helper.assertFalse(NaniteHost.isHost(player), "not a host yet")
        touch()
        helper.assertTrue(NaniteHost.isHost(player), "touching made a host")
        helper.assertValueEqual(NaniteHost.archiveNanites(player), NaniteHost.REGEN_CAP, "stocked with Archive nanites")
        NaniteHost.draw(player, 3)
        touch()
        helper.assertValueEqual(NaniteHost.archiveNanites(player), NaniteHost.REGEN_CAP - 3, "a host gets nothing more")
        helper.succeed()
    }

    private fun useArchive(helper: GameTestHelper, player: Player) {
        val pos = helper.absolutePos(AT)
        helper.level.getBlockState(pos).useWithoutItem(helper.level, player, BlockHitResult(pos.center, Direction.UP, pos, false))
    }

    private fun archiveContact(helper: GameTestHelper) {
        formArchive(helper)
        val (player, _) = player(helper)
        useArchive(helper, player)
        helper.assertTrue(NaniteHost.isHost(player), "using the Archive made a host")
        helper.assertTrue(helper.getBlockEntity(AT, ArchiveBlockEntity::class.java).state()!!.owner == player.uuid, "and claimed it")
        helper.assertValueEqual(NaniteHost.archiveNanites(player), NaniteHost.REGEN_CAP, "stocked with Archive nanites")
        NaniteHost.draw(player, 3)
        useArchive(helper, player)
        helper.assertValueEqual(NaniteHost.archiveNanites(player), NaniteHost.REGEN_CAP - 3, "a host gets nothing more")
        helper.succeed()
    }

    private fun archiveContactNeedsAccess(helper: GameTestHelper) {
        val be = formArchive(helper)
        val (owner, _) = player(helper)
        be.state()!!.claim(owner.uuid)
        val (player, _) = player(helper)
        useArchive(helper, player)
        helper.assertFalse(NaniteHost.isHost(player), "another team's Archive gives no first contact")
        helper.assertTrue(be.state()!!.owner == owner.uuid, "still the owner's")
        helper.succeed()
    }

    private fun regenerates(helper: GameTestHelper) {
        val (player, _) = player(helper)
        helper.assertFalse(NaniteHost.regenerate(player), "not a host")
        NaniteHost.contact(player)
        NaniteHost.draw(player, NaniteHost.REGEN_CAP)
        helper.assertTrue(NaniteHost.regenerate(player), "fed host regenerates")
        helper.assertValueEqual(NaniteHost.archiveNanites(player), 1, "one nanite")
        player.foodData.setFoodLevel(NaniteHost.REGEN_MIN_FOOD - 1)
        helper.assertFalse(NaniteHost.regenerate(player), "hungry host does not")
        player.foodData.setFoodLevel(20)
        NaniteHost.give(player, NaniteHost.REGEN_CAP)
        helper.assertFalse(NaniteHost.regenerate(player), "stops at the cap")
        helper.succeed()
    }

    /**
     * A focus that needs items: the Archive stops at its cost and waits (drawing no nanites), offering the items from
     * the player's inventory researches it, and its reward goes to the player.
     */
    private fun items(helper: GameTestHelper) {
        val theory = tech("scientific_theory")
        val be = formArchive(helper)
        val state = be.state()!!
        val player = helper.makeMockServerPlayerInLevel()
        player.setGameMode(GameType.SURVIVAL)
        ItszuLib.TEAMS.change { it.ensurePlayer(player.uuid, "archive_items") }
        val team = ItszuLib.TEAMS.state.teamOf(player.uuid)!!.id
        val server = helper.level.server
        NaniteHost.contact(player)
        state.claim(player.uuid)
        TechTree.queue(server, team, theory)
        val cost = TechTree.of(helper.level.registryAccess())[theory]!!.cost
        TechTree.addProgress(server, team, theory, cost)
        val nanites = NaniteHost.archiveNanites(player)
        helper.assertValueEqual(state.step(helper.level, listOf(player)), 0L, "nothing more to research with points")
        helper.assertValueEqual(state.status, ArchiveStatus.NEEDS_ITEMS, "waits for items")
        helper.assertValueEqual(NaniteHost.archiveNanites(player), nanites, "draws no nanites meanwhile")
        player.inventory.add(ItemStack(net.minecraft.world.item.Items.BOOK))
        player.inventory.add(ItemStack(net.minecraft.world.item.Items.PAPER, 12))
        helper.assertValueEqual(ArchiveResearch.deliver(player), 9, "book and eight paper taken")
        helper.assertValueEqual(player.inventory.countItem(net.minecraft.world.item.Items.PAPER), 4, "the rest kept")
        helper.assertTrue(ItszuLib.TEAMS.state.team(team)!![Research.TYPE].has(theory), "researched")
        helper.assertValueEqual(player.inventory.countItem(ArchiveContent.CODEX.get()), 1, "reward: a Codex")
        helper.succeed()
    }

    private fun researches(helper: GameTestHelper) {
        val be = formArchive(helper)
        val state = be.state()!!
        val (player, team) = player(helper)
        val server = helper.level.server
        NaniteHost.contact(player)
        state.claim(player.uuid)
        helper.assertValueEqual(state.step(helper.level, listOf(player)), 0L, "nothing queued")
        helper.assertValueEqual(state.status, ArchiveStatus.IDLE, "idle")
        TechTree.queue(server, team, METALLURGY)
        val cost = TechTree.of(helper.level.registryAccess())[METALLURGY]!!.cost
        fun research() = ItszuLib.TEAMS.state.team(team)!![Research.TYPE]
        var steps = 0
        while (!research().has(METALLURGY) && steps < 50) {
            state.step(helper.level, listOf(player))
            steps++
        }
        helper.assertTrue(research().has(METALLURGY), "researched the focus after $steps steps")
        helper.assertValueEqual(steps.toLong(), cost / 5L, "5 points a step")
        helper.assertValueEqual(NaniteHost.archiveNanites(player), NaniteHost.REGEN_CAP - (cost / 10L).toInt(), "10 points a nanite")
        state.step(helper.level, listOf(player))
        helper.assertValueEqual(state.status, ArchiveStatus.IDLE, "queue done")

        // No host in range: nothing is drawn and no progress is made.
        TechTree.queue(server, team, BASIC_CIRCUITS)
        helper.assertValueEqual(state.step(helper.level, listOf()), 0L, "no host, no progress")
        helper.assertValueEqual(state.status, ArchiveStatus.NO_HOST, "status")
        // A host from another team does not count.
        val (other, _) = player(helper)
        NaniteHost.contact(other)
        helper.assertValueEqual(state.step(helper.level, listOf(other)), 0L, "other team's host")
        helper.assertValueEqual(NaniteHost.archiveNanites(other), NaniteHost.REGEN_CAP, "other host untouched")
        helper.succeed()
    }

    private fun shareFocus(helper: GameTestHelper) {
        val (player, team) = player(helper)
        val (second, _) = player(helper)
        ItszuLib.TEAMS.change { it.invite(player.uuid, second.uuid).accept(second.uuid, team) }
        NaniteHost.contact(player)
        NaniteHost.contact(second)
        val a = ArchiveState {}
        val b = ArchiveState {}
        a.claim(player.uuid)
        b.claim(second.uuid)
        TechTree.queue(helper.level.server, team, METALLURGY)
        val dimension = helper.level.dimension()
        val homeA = GlobalPos.of(dimension, helper.absolutePos(BlockPos(0, 1, 0)))
        val homeB = GlobalPos.of(dimension, helper.absolutePos(BlockPos(4, 1, 0)))
        helper.assertValueEqual(a.step(helper.level, listOf(player), home = homeA) + b.step(helper.level, listOf(second), home = homeB), 10L, "both work on the focus")
        helper.assertValueEqual(ItszuLib.TEAMS.state.team(team)!![Research.TYPE].progressOf(METALLURGY), 10L, "one shared progress")
        val listed = ArchiveRegistry.forTeam(team)
        helper.assertValueEqual(listed.map { it.pos }.toSet(), setOf(homeA, homeB), "both listed for the team")
        helper.assertTrue(listed.all { it.status == ArchiveStatus.RESEARCHING }, "with their status")
        a.onBreak(com.itszuvalex.itszulib.api.wrappers.WrapperLevel(helper.level), homeA.pos(), homeA.pos())
        helper.assertValueEqual(ArchiveRegistry.forTeam(team).map { it.pos }, listOf(homeB), "a broken Archive leaves the list")
        ArchiveRegistry.remove(homeB)
        helper.succeed()
    }

    private fun followOwner(helper: GameTestHelper) {
        val server = helper.level.server
        val (owner, team) = player(helper)
        val (joiner, solo) = player(helper)
        fun research(id: UUID) = ItszuLib.TEAMS.state.team(id)!![Research.TYPE]
        // The team has researched Metallurgy and moved on; the joiner's Archive is still on Metallurgy.
        TechTree.unlock(server, team, METALLURGY)
        TechTree.queue(server, team, BASIC_CIRCUITS)
        TechTree.queue(server, solo, METALLURGY)
        NaniteHost.contact(joiner)
        val archive = ArchiveState {}
        archive.claim(joiner.uuid)
        val home = GlobalPos.of(helper.level.dimension(), helper.absolutePos(BlockPos(2, 1, 2)))
        archive.step(helper.level, listOf(joiner), home = home)
        helper.assertValueEqual(research(solo).progressOf(METALLURGY), 5L, "working on Metallurgy alone")

        ItszuLib.TEAMS.change { it.invite(owner.uuid, joiner.uuid).accept(joiner.uuid, team) }
        val focus = TechTree.focus(server, team, ArchiveContent.TREE)
        helper.assertTrue(focus != null && focus != METALLURGY, "the team's focus is not the researched technology: $focus")
        val before = research(team).progressOf(focus!!)
        archive.step(helper.level, listOf(joiner), home = home)
        helper.assertTrue(research(team).progressOf(focus) > before, "the joiner's Archive took up the team's focus")
        helper.assertTrue(ArchiveRegistry.forTeam(team).any { it.pos == home }, "listed for the new team")

        ItszuLib.TEAMS.change { it.leave(joiner.uuid) }
        val alone = ItszuLib.TEAMS.state.teamOf(joiner.uuid)!!.id
        helper.assertTrue(TechTree.focus(server, alone, ArchiveContent.TREE) == focus, "the leaver keeps the same focus")
        val kept = research(alone).progressOf(focus)
        archive.step(helper.level, listOf(joiner), home = home)
        helper.assertTrue(research(alone).progressOf(focus) > kept, "and keeps working on it, for themselves")
        helper.assertValueEqual(research(team).progressOf(focus), kept, "no longer for the old team")
        helper.assertFalse(ArchiveRegistry.forTeam(team).any { it.pos == home }, "gone from the old team's list")
        helper.assertTrue(ArchiveRegistry.forTeam(alone).any { it.pos == home }, "on the leaver's list")
        ArchiveRegistry.remove(home)
        helper.succeed()
    }

    private fun menuQueue(helper: GameTestHelper) {
        val be = formArchive(helper)
        val (player, team) = player(helper)
        val access = helper.level.registryAccess()
        fun queue() = ItszuLib.TEAMS.state.team(team)!![Research.TYPE].queue
        val menu = ArchiveMenu(1, player.inventory, be)
        helper.assertFalse(menu.handleAction(player, ArchiveResearch.ACTION_QUEUE, -1), "nothing refused")
        helper.assertTrue(menu.handleAction(player, ArchiveResearch.ACTION_QUEUE, ArchiveResearch.networkId(access, MACHINING)), "queued")
        val path = TechTree.of(access).pathTo(MACHINING, Research.EMPTY)
        helper.assertTrue(path.size > 1 && queue() == path, "queued after its prerequisites: ${queue()}")
        // The Codex edits the same queue, from anywhere.
        val codex = CodexMenu(2, player.inventory)
        helper.assertTrue(codex.handleAction(player, ArchiveResearch.ACTION_UNQUEUE, ArchiveResearch.networkId(access, MACHINING)), "unqueued from the Codex")
        helper.assertTrue(queue() == path.dropLast(1), "only it left the queue")
        helper.assertTrue(ArchiveResearch.byNetworkId(access, ArchiveResearch.networkId(access, METALLURGY)) == METALLURGY, "network id round trip")
        helper.succeed()
    }

    private fun codexOpens(helper: GameTestHelper) {
        val player = helper.makeMockServerPlayerInLevel()
        val stack = ItemStack(ArchiveContent.CODEX.get())
        player.setItemInHand(InteractionHand.MAIN_HAND, stack)
        stack.use(helper.level, player, InteractionHand.MAIN_HAND)
        helper.assertTrue(player.containerMenu is CodexMenu, "codex menu open, got ${player.containerMenu}")
        helper.succeed()
    }

    private fun drawTo(helper: GameTestHelper) {
        val (player, _) = player(helper)
        NaniteHost.contact(player)
        val taken = NaniteHost.drawTo(player, 3, helper.level, helper.absolutePos(AT).center)
        helper.assertValueEqual(taken, 3, "taken")
        helper.assertValueEqual(NaniteHost.archiveNanites(player), NaniteHost.REGEN_CAP - 3, "left")
        helper.assertValueEqual(NaniteHost.drawTo(player, 100, helper.level, helper.absolutePos(AT).center), NaniteHost.REGEN_CAP - 3, "only what is there")
        helper.succeed()
    }
}
