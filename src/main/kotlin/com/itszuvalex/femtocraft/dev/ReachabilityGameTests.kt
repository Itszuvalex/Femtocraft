package com.itszuvalex.femtocraft.dev

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.cyber.Cybermaterials
import com.itszuvalex.femtocraft.industry.DustRecipes
import com.itszuvalex.femtocraft.industry.FrameMultiblocks
import com.itszuvalex.femtocraft.industry.GerminationRecipes
import com.itszuvalex.femtocraft.nanite.NaniteInfusionRecipes
import com.itszuvalex.femtocraft.worldgen.CrystalClusterBlockEntity
import net.minecraft.util.RandomSource
import com.itszuvalex.femtocraft.worldgen.WorldgenContent
import com.itszuvalex.itszulib.research.TechTree
import com.itszuvalex.itszulib.verify.Reachability
import com.itszuvalex.itszulib.verify.Reachability.Producer
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.Identifier
import net.minecraft.world.item.Item
import java.nio.file.Files
import java.nio.file.Path

/**
 * Which of Femtocraft's items can be obtained at all, starting from everything outside Femtocraft (vanilla and other
 * mods are assumed obtainable) and the blocks the world generates (crystal clusters, the cybermaterials a rift makes).
 * Producers are the crafting and smelting recipes, the machine tables, technology rewards, and what mining each
 * Femtocraft block drops.
 *
 * An item is unobtainable when nothing leads to it, or only things that need it first. [KNOWN_UNOBTAINABLE] lists the
 * ones that are meant to be, until their blocks are designed; the test fails for an item that is not listed, and for a
 * listed one that has become obtainable, so the list only ever shrinks. The full report goes to
 * `build/reports/femtocraft-reachability.txt`.
 */
object ReachabilityGameTests {
    /**
     * Femtocraft items nothing makes yet, as of the first run of this test. Remove an item when it gains a source (the
     * test insists); do not add one without deciding it is meant to be unobtainable.
     *
     * - No crafting recipe, so only breaking a placed one gives it: the crystal crusher, furnace and liquifier, the
     *   fluid, item and nanite repositories, the nanite extractor and infuser, the crystal power conduit, refined
     *   substrate and the glow stick.
     * - Nothing at all: dumb dust, the configurator, nano lash, nano pack, solar panel, and `shift_test`.
     */
    private val KNOWN_UNOBTAINABLE: Set<String> = setOf(
        "femtocraft:crystal_crusher", "femtocraft:crystal_furnace", "femtocraft:crystal_liquifier",
        "femtocraft:fluid_repository", "femtocraft:item_repository", "femtocraft:nanite_repository",
        "femtocraft:nanite_extractor", "femtocraft:nanite_infuser", "femtocraft:power_conduit_crystal",
        "femtocraft:refined_substrate", "femtocraft:glow_stick",
        "femtocraft:dumb_dust", "femtocraft:configurator", "femtocraft:nano_lash", "femtocraft:nano_pack",
        "femtocraft:solar_panel", "femtocraft:shift_test",
    )

    fun register() {
        DevGameTests.test("femtocraft_items_are_obtainable", body = ::obtainable)
    }

    private fun item(id: String): Item? = BuiltInRegistries.ITEM.getOptional(Identifier.parse(id)).orElse(null)

    /** Every `"name"` of an item entry in a loot table file. */
    private fun lootItems(path: String): Set<Item> {
        val stream = Femtocraft::class.java.classLoader.getResourceAsStream(path) ?: return emptySet()
        val found = LinkedHashSet<Item>()
        fun walk(e: JsonElement) {
            when {
                e.isJsonObject -> {
                    val o = e.asJsonObject
                    if (o.get("type")?.asString == "minecraft:item") o.get("name")?.asString?.let(::item)?.let(found::add)
                    o.entrySet().forEach { walk(it.value) }
                }
                e.isJsonArray -> e.asJsonArray.forEach(::walk)
            }
        }
        stream.use { walk(JsonParser.parseReader(it.reader())) }
        return found
    }

    private fun producers(helper: GameTestHelper): List<Producer<Item>> {
        val producers = ArrayList<Producer<Item>>(Reachability.recipeProducers(helper.level))
        for ((input, output) in DustRecipes.all()) producers += Producer("dust of ${input.item}", listOf(setOf(input.item)), setOf(output.item))
        for (r in GerminationRecipes.all()) producers += Producer("germinating ${r.input}", listOf(setOf(r.input)), r.results.map { it.first }.toSet())
        for (r in NaniteInfusionRecipes.all()) producers += Producer("infusing ${r.input()}", listOf(setOf(r.input())), setOf(r.output()))
        for (mb in FrameMultiblocks.all()) {
            producers += Producer("building ${mb.id}", mb.required().map { setOf(it.item) }, setOf(mb.packedItem?.invoke() ?: mb.block.asItem()))
        }
        // A crystal cluster's drops are rolled in code (CrystalClusterBlock.getDrops), so sample them.
        val clusterDrops = (1L..200L).flatMap { CrystalClusterBlockEntity.rollDrops(RandomSource.create(it), 0xFFFFFF) }.map { it.item }.toSet()
        producers += Producer("mining a crystal cluster", listOf(setOf(WorldgenContent.CRYSTAL_CLUSTER.get().asItem())), clusterDrops)
        val techs = TechTree.of(helper.level.registryAccess())
        for ((id, tech) in techs.all.filterKeys { it.namespace == Femtocraft.ID }) {
            val rewards = tech.rewards.map { it.item().value() }.toSet()
            if (rewards.isNotEmpty()) producers += Producer("reward of $id", emptyList(), rewards)
        }
        for ((key, block) in BuiltInRegistries.BLOCK.entrySet().filter { it.key.identifier().namespace == Femtocraft.ID }) {
            val drops = lootItems("data/${Femtocraft.ID}/loot_table/blocks/${key.identifier().path}.json")
            if (drops.isNotEmpty()) producers += Producer("mining ${key.identifier()}", listOf(setOf(block.asItem())), drops)
        }
        return producers
    }

    private fun obtainable(helper: GameTestHelper) {
        val own = Reachability.itemsOf(Femtocraft.ID)
        val base = BuiltInRegistries.ITEM.filter { it !in own }.toMutableSet()
        base += WorldgenContent.CRYSTAL_CLUSTER.get().asItem()
        Cybermaterials.producedBlocks().forEach { base += it.asItem() }

        val producers = producers(helper)
        val reached = Reachability.reachable(base, producers)
        val unobtainable = own.filter { it !in reached }.map { BuiltInRegistries.ITEM.getKey(it).toString() }.sorted()

        val report = buildString {
            appendLine("${own.size - unobtainable.size} of ${own.size} Femtocraft items are obtainable.")
            for (name in unobtainable) {
                appendLine("$name: ${Reachability.blockedBy(item(name)!!, reached, producers).joinToString("; ")}")
            }
        }
        val path = Path.of("build", "reports", "femtocraft-reachability.txt")
        Files.createDirectories(path.parent)
        Files.writeString(path, report)

        helper.assertValueEqual(unobtainable.filter { it !in KNOWN_UNOBTAINABLE }, emptyList<String>(), "unobtainable items (see $path)")
        helper.assertValueEqual(KNOWN_UNOBTAINABLE.filter { it !in unobtainable }, emptyList<String>(), "listed as unobtainable but obtainable (remove from the list)")
        helper.succeed()
    }
}
