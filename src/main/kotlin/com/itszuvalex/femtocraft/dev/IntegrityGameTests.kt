package com.itszuvalex.femtocraft.dev

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.industry.DustRecipes
import com.itszuvalex.femtocraft.industry.GerminationRecipes
import com.itszuvalex.femtocraft.industry.LiquifierRecipes
import com.itszuvalex.itszulib.research.TechTree
import com.itszuvalex.itszulib.research.Technology
import com.itszuvalex.femtocraft.nanite.FragNaniteTank
import com.itszuvalex.femtocraft.nanite.NaniteRegistry
import com.itszuvalex.itszulib.core.BlockEntityCore
import com.itszuvalex.itszulib.core.BreakBehavior
import com.itszuvalex.itszulib.verify.BlockEntityContents
import com.itszuvalex.itszulib.verify.BlockEntityRoundTrip
import com.itszuvalex.itszulib.verify.BreakChecks
import com.itszuvalex.itszulib.verify.CapabilityChecks
import com.itszuvalex.itszulib.verify.MenuChecks
import com.itszuvalex.itszulib.verify.TickChecks
import com.itszuvalex.itszulib.verify.ContentIntegrity
import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.GameTestHelper

/**
 * Registry and data integrity: everything Femtocraft registers has the files that describe it, and the data tables
 * (technologies, machine recipes) refer only to things that exist. Generic checks are ItszuLib's [ContentIntegrity].
 */
object IntegrityGameTests {
    fun register() {
        BlockEntityContents.register(NaniteContentsProbe)
        DevGameTests.test("content_has_its_assets_and_data", body = ::content)
        DevGameTests.test("technologies_are_sound", body = ::technologies)
        DevGameTests.test("machine_recipes_are_usable", body = ::machineRecipes)
        DevGameTests.test("block_entities_save_load_and_sync", body = ::roundTrips)
        DevGameTests.test("block_entities_break_as_they_declare", body = ::breaks)
        DevGameTests.test("block_entities_run_without_failing", 600, ::ticks)
        DevGameTests.test("capabilities_match_modules", body = ::capabilities)
        DevGameTests.test("menus_neither_lose_nor_make_items", 600, ::menus)
    }

    private fun content(helper: GameTestHelper) {
        helper.assertValueEqual(ContentIntegrity.problems(Femtocraft.ID, Femtocraft::class.java.classLoader), emptyList<String>(), "content problems")
        helper.succeed()
    }

    private fun technologies(helper: GameTestHelper) {
        val techs = TechTree.of(helper.level.registryAccess())
        helper.assertValueEqual(techs.problems(), emptyList<String>(), "tree problems")
        val problems = ArrayList<String>()
        for ((id, tech) in techs.all.toSortedMap().filterKeys { it.namespace == Femtocraft.ID }) {
            for (item in tech.items) if (item.ingredient().items().findAny().isEmpty) problems += "$id needs an item that matches nothing"
            if (tech.name.isEmpty && tech.description.isEmpty && tech.displayName(id).string == Technology.translationKey(id)) problems += "$id has no name"
        }
        helper.assertValueEqual(problems, emptyList<String>(), "technology problems")
        helper.succeed()
    }

    private fun roundTrips(helper: GameTestHelper) {
        val problems = BlockEntityRoundTrip.problems(helper.level, helper.absolutePos(BlockPos(4, 1, 4)), Femtocraft.ID)
        helper.assertValueEqual(problems, emptyList<String>(), "block entity problems")
        helper.succeed()
    }

    private fun menus(helper: GameTestHelper) {
        val player = helper.makeMockServerPlayerInLevel()
        helper.assertValueEqual(MenuChecks.problems(helper.level, helper.absolutePos(BlockPos(4, 2, 4)), Femtocraft.ID, player), emptyList<String>(), "menu problems")
        helper.succeed()
    }

    private fun capabilities(helper: GameTestHelper) {
        helper.assertValueEqual(CapabilityChecks.problems(helper.level, helper.absolutePos(BlockPos(4, 2, 4)), Femtocraft.ID), emptyList<String>(), "capability problems")
        helper.succeed()
    }

    private fun ticks(helper: GameTestHelper) {
        helper.assertValueEqual(TickChecks.problems(helper.level, helper.absolutePos(BlockPos(4, 2, 4)), Femtocraft.ID), emptyList<String>(), "tick problems")
        helper.succeed()
    }

    private fun breaks(helper: GameTestHelper) {
        helper.assertValueEqual(BreakChecks.problems(helper.level, helper.absolutePos(BlockPos(4, 1, 4)), Femtocraft.ID), emptyList<String>(), "break problems")
        helper.succeed()
    }

    private fun machineRecipes(helper: GameTestHelper) {
        val problems = ArrayList<String>()
        for ((input, output) in DustRecipes.all()) if (output.isEmpty) problems += "dust recipe for ${input.item} makes nothing"
        for (recipe in LiquifierRecipes.all()) {
            if (recipe.output().isEmpty) problems += "liquifier recipe for ${recipe.input} makes nothing"
        }
        for (recipe in GerminationRecipes.all()) {
            val name = recipe.input.toString()
            if (recipe.results.isEmpty()) problems += "germination of $name makes nothing"
            if (recipe.results.any { it.second.isEmpty() }) problems += "germination of $name has an empty result range"
            if (recipe.ticks <= 0 || recipe.fluidRequired <= 0 || recipe.fluidPerTick <= 0) problems += "germination of $name has a non-positive cost"
        }
        helper.assertValueEqual(problems, emptyList<String>(), "recipe problems")
        helper.succeed()
    }
}

/**
 * Nanites in a block entity's tanks, for ItszuLib's content checks: filled with a distinctive amount, measured per strain.
 */
object NaniteContentsProbe : BlockEntityContents.Probe {
    private fun tanks(be: BlockEntityCore, behaviors: Set<BreakBehavior>) =
        be.contentFragments().filter { it.breakBehavior in behaviors }.filterIsInstance<FragNaniteTank>().filter { it.persist }

    override fun fill(be: BlockEntityCore, behaviors: Set<BreakBehavior>): Boolean {
        var filled = false
        for (frag in tanks(be, behaviors)) {
            val amount = minOf(frag.tank.capacity, 37)
            if (amount > 0 && frag.tank.fill(NaniteRegistry.dumb(amount), true).isEmpty) filled = true
        }
        return filled
    }

    override fun tally(be: BlockEntityCore, behaviors: Set<BreakBehavior>): BlockEntityContents.Tally {
        val counts = java.util.TreeMap<String, Long>()
        for (frag in tanks(be, behaviors)) for (stack in frag.tank.contents()) {
            counts.merge("nanites:${stack.archetype}/${stack.strain}/${stack.version}", stack.amount.toLong(), Long::plus)
        }
        return BlockEntityContents.Tally(counts)
    }
}
