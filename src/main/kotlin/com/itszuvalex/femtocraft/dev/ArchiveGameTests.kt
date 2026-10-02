package com.itszuvalex.femtocraft.dev

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.archive.ArchiveBlockEntity
import com.itszuvalex.femtocraft.archive.ArchiveContent
import com.itszuvalex.femtocraft.archive.ArchiveMenu
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
        DevGameTests.test("archive_menu_chooses_only_available_technologies", body = ::menuChoose)
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
        helper.assertValueEqual(NaniteHost.archiveNanites(player), NaniteHost.REGEN_CAP, "stocked with Archive nanites")
        NaniteHost.draw(player, 3)
        useArchive(helper, player)
        helper.assertValueEqual(NaniteHost.archiveNanites(player), NaniteHost.REGEN_CAP - 3, "a host gets nothing more")
        helper.succeed()
    }

    private fun archiveContactNeedsAccess(helper: GameTestHelper) {
        val be = formArchive(helper)
        be.state()!!.choose(METALLURGY, UUID.randomUUID())
        val (player, _) = player(helper)
        useArchive(helper, player)
        helper.assertFalse(NaniteHost.isHost(player), "another team's Archive gives no first contact")
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

    private fun researches(helper: GameTestHelper) {
        val be = formArchive(helper)
        val state = be.state()!!
        val (player, team) = player(helper)
        NaniteHost.contact(player)
        state.choose(METALLURGY, team)
        val cost = TechTree.of(helper.level.registryAccess())[METALLURGY]!!.cost
        var steps = 0
        while (state.technology != null && steps < 50) {
            state.step(helper.level, listOf(player))
            steps++
        }
        val research = ItszuLib.TEAMS.state.team(team)!![Research.TYPE]
        helper.assertTrue(research.has(METALLURGY), "researched after $steps steps")
        helper.assertValueEqual(steps.toLong(), cost / 5L, "5 points a step")
        helper.assertValueEqual(NaniteHost.archiveNanites(player), NaniteHost.REGEN_CAP - (cost / 10L).toInt(), "10 points a nanite")
        helper.assertValueEqual(state.status, ArchiveStatus.IDLE, "choice cleared")

        // No host in range: nothing is drawn and no progress is made.
        state.choose(BASIC_CIRCUITS, team)
        helper.assertValueEqual(state.step(helper.level, listOf()), 0L, "no host, no progress")
        helper.assertValueEqual(state.status, ArchiveStatus.NO_HOST, "status")
        // A host from another team does not count.
        val (other, _) = player(helper)
        NaniteHost.contact(other)
        helper.assertValueEqual(state.step(helper.level, listOf(other)), 0L, "other team's host")
        helper.assertValueEqual(NaniteHost.archiveNanites(other), NaniteHost.REGEN_CAP, "other host untouched")
        helper.succeed()
    }

    private fun menuChoose(helper: GameTestHelper) {
        val be = formArchive(helper)
        val (player, _) = player(helper)
        val menu = ArchiveMenu(1, player.inventory, be)
        val access = helper.level.registryAccess()
        helper.assertFalse(menu.handleAction(player, ArchiveMenu.ACTION_CHOOSE, ArchiveMenu.networkId(access, MACHINING)), "locked refused")
        helper.assertFalse(menu.handleAction(player, ArchiveMenu.ACTION_CHOOSE, -1), "nothing refused")
        helper.assertTrue(menu.handleAction(player, ArchiveMenu.ACTION_CHOOSE, ArchiveMenu.networkId(access, METALLURGY)), "available chosen")
        helper.assertTrue(be.state()!!.technology == METALLURGY, "chosen")
        helper.assertTrue(ArchiveMenu.byNetworkId(access, ArchiveMenu.networkId(access, METALLURGY)) == METALLURGY, "network id round trip")
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
