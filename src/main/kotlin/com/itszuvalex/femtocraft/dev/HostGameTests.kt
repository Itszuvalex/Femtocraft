package com.itszuvalex.femtocraft.dev

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.archive.ArchiveContent
import com.itszuvalex.femtocraft.archive.ArchiveState
import com.itszuvalex.femtocraft.archive.NaniteHost
import com.itszuvalex.femtocraft.core.FemtoNetwork
import com.itszuvalex.femtocraft.host.HostMenu
import com.itszuvalex.femtocraft.host.HostStats
import com.itszuvalex.femtocraft.host.HostStrains
import com.itszuvalex.femtocraft.host.NaniteArchetypes
import com.itszuvalex.femtocraft.host.Talents
import com.itszuvalex.femtocraft.nanite.NaniteRegistry
import com.itszuvalex.femtocraft.nanite.PlayerNanites
import com.itszuvalex.itszulib.ItszuLib
import com.itszuvalex.itszulib.research.TechTree
import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.GameType
import java.util.UUID

/**
 * Game tests for the host framework: the talent data, unlocking with research points, strains carried by bred
 * nanites, talents changing the host's body and what machines make of the nanites, and the host screen's menu and
 * screen request.
 */
object HostGameTests {
    private fun talent(path: String) = Identifier.fromNamespaceAndPath(Femtocraft.ID, "archive/$path")
    private val DEEP_RESERVOIR = talent("deep_reservoir")
    private val QUICKENED = talent("quickened_breeding")
    private val DENSE = talent("dense_thought")
    private val LONG_TETHER = talent("long_tether")

    /** A refusal's name, "OK" for none (game test asserts take no nulls). */
    private fun HostStrains.Refusal?.label(): String = this?.name ?: "OK"

    fun register() {
        DevGameTests.test("host_talents_load_from_data", body = ::talentsLoad)
        DevGameTests.test("host_unlocks_talents_with_research_points", body = ::unlocks)
        DevGameTests.test("host_bred_nanites_carry_the_strain", body = ::strainCarried)
        DevGameTests.test("archive_reads_talents_on_the_nanites_it_draws", body = ::archiveReadsTalents)
        DevGameTests.test("host_reach_talent_extends_machine_reach", body = ::reach)
        DevGameTests.test("host_menu_unlocks_and_resets", body = ::menu)
        DevGameTests.test("host_screen_opens_on_request", body = ::screenRequest)
    }

    private fun host(helper: GameTestHelper, name: String): Pair<Player, UUID> {
        val player = helper.makeMockPlayer(GameType.SURVIVAL)
        ItszuLib.TEAMS.change { it.ensurePlayer(player.uuid, name) }
        NaniteHost.contact(player)
        return player to ItszuLib.TEAMS.state.teamOf(player.uuid)!!.id
    }

    /** Researches [n] of Femtocraft's technologies for [team]: [n] talent points. */
    private fun research(helper: GameTestHelper, team: UUID, n: Int) {
        val techs = TechTree.of(helper.level.registryAccess())
        techs.inTree(ArchiveContent.TREE).filterValues { !it.unlockedByDefault }.keys.sorted().take(n)
            .forEach { TechTree.unlock(helper.level.server, team, it) }
    }

    private fun talentsLoad(helper: GameTestHelper) {
        val talents = Talents.of(helper.level.registryAccess())
        val archive = talents.inArchetype(NaniteArchetypes.ARCHIVE.id)
        helper.assertTrue(archive.size >= 7, "the Archive tree loaded, ${archive.size} talents")
        helper.assertTrue(talents.problems().isEmpty(), "no data problems: ${talents.problems()}")
        helper.assertValueEqual(talents.layout(NaniteArchetypes.ARCHIVE.id).positions.size, archive.size, "every talent laid out")
        helper.assertTrue(NaniteArchetypes.ALL.all { NaniteRegistry.isKnown(com.itszuvalex.femtocraft.nanite.NaniteStack(it.id, it.id, com.itszuvalex.femtocraft.nanite.NaniteStrainVersion.ZERO, 1)) },
            "every archetype is a known nanite")
        helper.succeed()
    }

    private fun unlocks(helper: GameTestHelper) {
        val (player, team) = host(helper, "host_unlocks")
        helper.assertValueEqual(HostStrains.earned(player), 0, "no research, no points")
        helper.assertValueEqual(HostStrains.unlock(player, DEEP_RESERVOIR).label(), "NO_POINTS", "needs a point")
        research(helper, team, 2)
        helper.assertValueEqual(HostStrains.available(player), 2, "a point per researched technology")
        helper.assertValueEqual(HostStrains.unlock(player, DENSE).label(), "LOCKED", "needs its prerequisite first")
        helper.assertValueEqual(HostStrains.unlock(player, DEEP_RESERVOIR).label(), "OK", "unlocked")
        helper.assertValueEqual(HostStrains.unlock(player, DEEP_RESERVOIR).label(), "ALREADY_UNLOCKED", "once")
        helper.assertValueEqual(HostStrains.available(player), 1, "it cost its point")
        helper.assertValueEqual(HostStats.of(player).regenCap, NaniteHost.REGEN_CAP + 10, "the body keeps more stocked")
        helper.assertTrue(HostStrains.reset(player, NaniteArchetypes.ARCHIVE.id), "reset")
        helper.assertValueEqual(HostStrains.available(player), 2, "points back")
        helper.assertValueEqual(HostStats.of(player).regenCap, NaniteHost.REGEN_CAP, "and the body as before")

        val stranger = helper.makeMockPlayer(GameType.SURVIVAL)
        helper.assertValueEqual(HostStrains.unlock(stranger, DEEP_RESERVOIR).label(), "NOT_HOST", "only hosts have strains")
        helper.succeed()
    }

    /**
     * Bred nanites carry the strain's talents at version 0.<count>; nanites bred before a change keep theirs, and a
     * draw takes either.
     */
    private fun strainCarried(helper: GameTestHelper) {
        val (player, team) = host(helper, "host_strain")
        val first = PlayerNanites.tank(player).contents()
        helper.assertTrue(first.size == 1 && first[0].talents.isEmpty(), "contact nanites carry no talents: $first")
        research(helper, team, 3)
        HostStrains.unlock(player, QUICKENED)
        HostStrains.unlock(player, DENSE)
        NaniteHost.draw(player, 4)
        NaniteHost.give(player, 2)
        val contents = PlayerNanites.tank(player).contents()
        helper.assertValueEqual(contents.size, 2, "old and new nanites do not stack: $contents")
        val bred = contents.first { it.talents.isNotEmpty() }
        helper.assertValueEqual(bred.talents, listOf(DENSE, QUICKENED).sorted(), "bred with the strain's talents")
        helper.assertValueEqual(bred.version.minor, 2, "version 0.2")
        helper.assertValueEqual(NaniteHost.archiveNanites(player), NaniteHost.REGEN_CAP - 4 + 2, "both count as Archive nanites")
        val drawn = NaniteHost.drawStacks(player, NaniteHost.REGEN_CAP)
        helper.assertValueEqual(drawn.sumOf { it.amount }, NaniteHost.REGEN_CAP - 2, "a draw takes either, oldest first")
        helper.assertTrue(drawn.first().talents.isEmpty(), "oldest first")
        helper.succeed()
    }

    /** An Archive gets the research points the drawn nanite's talents say (10, +5 for Dense Thought). */
    private fun archiveReadsTalents(helper: GameTestHelper) {
        val (player, team) = host(helper, "host_archive")
        research(helper, team, 3)
        val techs = TechTree.of(helper.level.registryAccess())
        val target = techs.inTree(ArchiveContent.TREE).keys.sorted().firstOrNull { techs.state(it, TechTree.research(player)) == com.itszuvalex.itszulib.research.TechnologyState.AVAILABLE && techs[it]!!.cost > 20 }
        helper.assertTrue(target != null, "something to research")
        TechTree.queue(helper.level.server, team, target!!)

        val plain = ArchiveState {}
        plain.claim(player.uuid)
        plain.step(helper.level, listOf(player))
        helper.assertValueEqual(plain.points, ArchiveState.POINTS_PER_NANITE - ArchiveState.RATE, "a talentless nanite: 10 points")

        HostStrains.unlock(player, QUICKENED)
        HostStrains.unlock(player, DENSE)
        NaniteHost.draw(player, NaniteHost.archiveNanites(player))
        NaniteHost.give(player, 1)
        val dense = ArchiveState {}
        dense.claim(player.uuid)
        dense.step(helper.level, listOf(player))
        helper.assertValueEqual(dense.points, ArchiveState.POINTS_PER_NANITE + 5 - ArchiveState.RATE, "a Dense Thought nanite: 15 points")
        helper.succeed()
    }

    private fun reach(helper: GameTestHelper) {
        // In the level, so the host is among the level's players.
        val player = helper.makeMockServerPlayerInLevel()
        ItszuLib.TEAMS.change { it.ensurePlayer(player.uuid, "host_reach") }
        NaniteHost.contact(player)
        val team = ItszuLib.TEAMS.state.teamOf(player.uuid)!!.id
        val center = helper.absolutePos(BlockPos(0, 1, 0))
        player.setPos(center.center.add(4.0, 0.0, 0.0))
        helper.assertTrue(NaniteHost.nearbyHosts(helper.level, center, 2.0).isEmpty(), "4 blocks away, out of a 2 block reach")
        research(helper, team, 2)
        HostStrains.unlock(player, DEEP_RESERVOIR)
        HostStrains.unlock(player, LONG_TETHER)
        helper.assertValueEqual(NaniteHost.nearbyHosts(helper.level, center, 2.0), listOf(player), "Long Tether reaches 4 further")
        helper.succeed()
    }

    private fun menu(helper: GameTestHelper) {
        val player = helper.makeMockServerPlayerInLevel()
        ItszuLib.TEAMS.change { it.ensurePlayer(player.uuid, "host_menu") }
        NaniteHost.contact(player)
        research(helper, ItszuLib.TEAMS.state.teamOf(player.uuid)!!.id, 1)
        val menu = HostMenu(1, player.inventory)
        val id = HostMenu.networkId(helper.level.registryAccess(), DEEP_RESERVOIR)
        helper.assertTrue(id >= 0, "the talent has a network id")
        helper.assertTrue(HostMenu.byNetworkId(helper.level.registryAccess(), id) == DEEP_RESERVOIR, "and back")
        helper.assertTrue(menu.handleAction(player, HostMenu.ACTION_UNLOCK, id), "unlock handled")
        helper.assertValueEqual(HostStrains.of(player).talents(NaniteArchetypes.ARCHIVE.id), listOf(DEEP_RESERVOIR), "unlocked")
        helper.assertFalse(menu.handleAction(player, HostMenu.ACTION_UNLOCK, HostMenu.networkId(helper.level.registryAccess(), LONG_TETHER)), "no points left")
        helper.assertTrue(menu.handleAction(player, HostMenu.ACTION_RESET, NaniteArchetypes.ALL.indexOf(NaniteArchetypes.ARCHIVE)), "reset handled")
        helper.assertTrue(HostStrains.of(player).unlocked.isEmpty(), "reset")
        helper.succeed()
    }

    private fun screenRequest(helper: GameTestHelper) {
        val player = helper.makeMockServerPlayerInLevel()
        helper.assertTrue(FemtoNetwork.openScreen(player, HostMenu.SCREEN), "the host screen is registered")
        helper.assertTrue(player.containerMenu is HostMenu, "and opens its menu, got ${player.containerMenu}")
        helper.assertFalse(FemtoNetwork.openScreen(player, Identifier.fromNamespaceAndPath(Femtocraft.ID, "nothing")), "unknown screens open nothing")
        helper.succeed()
    }
}
