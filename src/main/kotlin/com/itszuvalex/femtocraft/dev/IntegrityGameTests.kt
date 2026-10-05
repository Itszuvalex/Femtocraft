package com.itszuvalex.femtocraft.dev

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.industry.DustRecipes
import com.itszuvalex.femtocraft.industry.GerminationRecipes
import com.itszuvalex.femtocraft.industry.LiquifierRecipes
import com.itszuvalex.itszulib.research.TechTree
import com.itszuvalex.itszulib.research.Technology
import com.itszuvalex.itszulib.verify.BlockEntityRoundTrip
import com.itszuvalex.itszulib.verify.ContentIntegrity
import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.GameTestHelper

/**
 * Registry and data integrity: everything Femtocraft registers has the files that describe it, and the data tables
 * (technologies, machine recipes) refer only to things that exist. Generic checks are ItszuLib's [ContentIntegrity].
 */
object IntegrityGameTests {
    fun register() {
        DevGameTests.test("content_has_its_assets_and_data", body = ::content)
        DevGameTests.test("technologies_are_sound", body = ::technologies)
        DevGameTests.test("machine_recipes_are_usable", body = ::machineRecipes)
        DevGameTests.test("block_entities_save_load_and_sync", body = ::roundTrips)
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
