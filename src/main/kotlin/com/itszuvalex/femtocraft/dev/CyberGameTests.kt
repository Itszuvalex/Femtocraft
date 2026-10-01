package com.itszuvalex.femtocraft.dev

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.cyber.CyberContent
import com.itszuvalex.femtocraft.dev.DevGameTests.place
import com.itszuvalex.femtocraft.industry.IndustryContent
import com.itszuvalex.femtocraft.nanite.CybermaterialNanites
import com.itszuvalex.femtocraft.nanite.NaniteRegistry
import com.itszuvalex.femtocraft.power.PowerContent
import com.itszuvalex.femtocraft.power.PowerCrystals
import com.itszuvalex.femtocraft.worldgen.CrystalClusterBlockEntity
import com.itszuvalex.femtocraft.worldgen.Rift
import com.itszuvalex.femtocraft.worldgen.WorldgenContent
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.Registries
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.GameType
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.registries.NeoForgeRegistries

/**
 * Game tests for the cyber and worldgen areas.
 */
object CyberGameTests {
    private val CENTER = BlockPos(4, 1, 4)

    fun register() {
        DevGameTests.test("cybermaterial_drops_follow_v3", body = ::drops)
        DevGameTests.test("dumb_dust_converts_blocks", body = ::dumbDust)
        DevGameTests.test("cybermaterials_give_nanites", body = ::nanites)
        DevGameTests.test("substrate_sustains_plants", body = ::substrate)
        DevGameTests.test("rift_converts_cylinder", body = ::riftConverts)
        DevGameTests.test("rift_sizes_include_medium_and_fit_the_feature_region", body = ::riftSizes)
        DevGameTests.test("rift_crystals_rest_on_the_ground", body = ::riftCrystals)
        DevGameTests.test("crystal_cluster_drops_crystals_and_dust", body = ::crystalCluster)
        DevGameTests.test("crystal_cluster_drops_nothing_when_replaced_or_creative_broken", body = ::crystalClusterNoFreeDrops)
        DevGameTests.test("rift_feature_and_recipes_load", body = ::dataLoads)
    }

    private fun dropsOf(helper: GameTestHelper, block: Block): List<ItemStack> {
        val pos = helper.absolutePos(CENTER)
        return Block.getDrops(block.defaultBlockState(), helper.level, pos, null)
    }

    private fun drops(helper: GameTestHelper) {
        val counted = mapOf(
            CyberContent.NANOWEAVE.get() to CyberContent.NANOWEAVE_THREAD.get(),
            CyberContent.CYBERLEAVES.get() to CyberContent.CYBERLEAF.get(),
            CyberContent.REDSTONEREPLACEMENT.get() to IndustryContent.REDSTONEREPLACEMENT_DUST.get(),
            CyberContent.LAPISREPLACEMENT.get() to IndustryContent.LAPISREPLACEMENT_DUST.get(),
        )
        for ((block, item) in counted) {
            repeat(5) {
                val d = dropsOf(helper, block)
                helper.assertTrue(d.size == 1 && d[0].`is`(item) && d[0].count in 3..5, "$block drops 3-5 $item, got $d")
            }
        }
        for (block in listOf(CyberContent.SUBSTRATE.get(), CyberContent.CYBERWOOD.get(), CyberContent.RIFTIRON.get(), CyberContent.PHASEMETAL.get(), CyberContent.DIAMONDREPLACEMENT.get())) {
            val d = dropsOf(helper, block)
            helper.assertTrue(d.size == 1 && d[0].`is`(block.asItem()) && d[0].count == 1, "$block drops itself, got $d")
        }
        helper.succeed()
    }

    private fun useDust(helper: GameTestHelper, rel: BlockPos, stack: ItemStack): InteractionResult {
        val player = helper.makeMockServerPlayerInLevel()
        player.setGameMode(GameType.SURVIVAL)
        player.setItemInHand(InteractionHand.MAIN_HAND, stack)
        val pos = helper.absolutePos(rel)
        return stack.useOn(UseOnContext(player, InteractionHand.MAIN_HAND, BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)))
    }

    private fun dumbDust(helper: GameTestHelper) {
        val stack = ItemStack(CyberContent.DUMB_DUST.get(), 10)
        val cases = listOf(
            Blocks.STONE.defaultBlockState() to CyberContent.SUBSTRATE.get(),
            Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState() to CyberContent.DIAMONDREPLACEMENT.get(),
            Blocks.IRON_ORE.defaultBlockState() to CyberContent.RIFTIRON.get(),
            Blocks.OAK_LEAVES.defaultBlockState().setValue(BlockStateProperties.PERSISTENT, true) to CyberContent.CYBERLEAVES.get(),
        )
        for ((i, case) in cases.withIndex()) {
            val rel = BlockPos(1 + i, 1, 1)
            helper.setBlock(rel, case.first)
            helper.assertTrue(useDust(helper, rel, stack).consumesAction(), "${case.first} converted")
            helper.assertBlockPresent(case.second, rel)
        }
        val log = BlockPos(1, 1, 3)
        helper.setBlock(log, Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X))
        useDust(helper, log, stack)
        helper.assertBlockProperty(log, RotatedPillarBlock.AXIS, Direction.Axis.X)
        helper.assertBlockPresent(CyberContent.CYBERWOOD.get(), log)
        helper.assertValueEqual(stack.count, 5, "one dust per conversion")
        val glass = BlockPos(3, 1, 3)
        helper.setBlock(glass, Blocks.GLASS)
        helper.assertTrue(useDust(helper, glass, stack) == InteractionResult.FAIL, "glass has no cybermaterial")
        helper.assertValueEqual(stack.count, 5, "no dust used on a failed conversion")
        helper.succeed()
    }

    private fun nanites(helper: GameTestHelper) {
        for (block in listOf(CyberContent.SUBSTRATE, CyberContent.CYBERWOOD, CyberContent.CYBERLEAVES, CyberContent.LAPISREPLACEMENT)) {
            helper.assertTrue(CybermaterialNanites.nanitesFor(ItemStack(block.get())) == NaniteRegistry.dumb(1), "${block.id} gives one Dumb nanite")
        }
        helper.assertTrue(CybermaterialNanites.nanitesFor(ItemStack(Blocks.STONE)) == null, "stone gives none")
        helper.succeed()
    }

    private fun substrate(helper: GameTestHelper) {
        helper.setBlock(CENTER, CyberContent.SUBSTRATE.get())
        val above = helper.absolutePos(CENTER.above())
        helper.assertTrue(Blocks.POPPY.defaultBlockState().canSurvive(helper.level, above), "flowers grow on substrate")
        helper.assertTrue(Blocks.OAK_SAPLING.defaultBlockState().canSurvive(helper.level, above), "saplings grow on substrate")
        helper.setBlock(CENTER, CyberContent.RIFTIRON.get())
        helper.assertTrue(!Blocks.POPPY.defaultBlockState().canSurvive(helper.level, above), "but not on riftiron")
        helper.succeed()
    }

    private fun riftConverts(helper: GameTestHelper) {
        for (x in 0..8) for (z in 0..8) {
            helper.setBlock(BlockPos(x, 1, z), Blocks.STONE)
            helper.setBlock(BlockPos(x, 2, z), Blocks.AIR)
        }
        helper.setBlock(BlockPos(4, 2, 4), Blocks.OAK_LOG)
        helper.setBlock(BlockPos(5, 1, 4), Blocks.BEDROCK)
        val center = helper.absolutePos(CENTER)
        val converted = Rift.convert(helper.level, center, 3, center.y, center.y + 1)
        // Strictly inside radius 3: 25 columns of stone, minus the bedrock, plus the log.
        helper.assertValueEqual(converted, 25 - 1 + 1, "blocks converted")
        helper.assertBlockPresent(CyberContent.SUBSTRATE.get(), BlockPos(4 + 2, 1, 4 + 2))
        helper.assertBlockPresent(Blocks.STONE, BlockPos(4 + 3, 1, 4))
        helper.assertBlockPresent(Blocks.BEDROCK, BlockPos(5, 1, 4))
        helper.assertBlockPresent(CyberContent.CYBERWOOD.get(), BlockPos(4, 2, 4))
        helper.succeed()
    }

    private fun riftSizes(helper: GameTestHelper) {
        helper.assertTrue(Rift.pickSize(0) == Rift.SMALL, "low rolls are small")
        helper.assertTrue(Rift.pickSize(Rift.SMALL_WEIGHT) == Rift.MEDIUM, "rolls past the small weight are medium (never in v3)")
        helper.assertTrue(Rift.pickSize(Rift.SMALL_WEIGHT + Rift.MEDIUM_WEIGHT) == Rift.LARGE, "the rest are large")
        val random = RandomSource.create(1)
        repeat(50) {
            val (radius, crystals) = Rift.LARGE.roll(random)
            helper.assertTrue(radius <= Rift.MAX_RADIUS, "radius $radius fits the feature region")
            helper.assertTrue(crystals in 20..24, "large crystal count")
        }
        // A rift centered in its chunk (offset 8) with radius MAX_RADIUS stays within the 3x3 chunks around it.
        helper.assertTrue(8 + (Rift.MAX_RADIUS - 1) <= 31 && 8 - (Rift.MAX_RADIUS - 1) >= -16, "region bound")
        helper.succeed()
    }

    private fun riftCrystals(helper: GameTestHelper) {
        for (x in 2..6) for (z in 2..6) for (y in 1..2) helper.setBlock(BlockPos(x, y, z), Blocks.STONE)
        val base = helper.absolutePos(BlockPos(4, 1, 4))
        val top = base.y + 3
        val fromInside = Rift.landingSpot(helper.level, base.x, base.z, base.y, base.y - 1, top)
        helper.assertTrue(fromInside == base.above(2), "a start inside the ground climbs onto it, got $fromInside")
        val fromAbove = Rift.landingSpot(helper.level, base.x, base.z, top, base.y - 1, top)
        helper.assertTrue(fromAbove == base.above(2), "a start in the air falls onto the ground, got $fromAbove")
        helper.succeed()
    }

    private fun crystalCluster(helper: GameTestHelper) {
        val be = helper.place<CrystalClusterBlockEntity>(CENTER, WorldgenContent.CRYSTAL_CLUSTER.get())
        helper.assertTrue(be.color ushr 24 == 0xFF, "opaque color")
        val drops = CrystalClusterBlockEntity.rollDrops(RandomSource.create(3), be.color)
        val crystals = drops.filter { it.`is`(PowerContent.POWER_CRYSTAL.get()) }
        helper.assertTrue(crystals.size in 2..7, "2-7 crystals, got ${crystals.size}")
        helper.assertTrue(crystals.all { PowerCrystals.data(it)?.color == be.color }, "crystals take the cluster's color")
        helper.assertTrue(drops.single { it.`is`(IndustryContent.CRACKLING_DUST.get()) }.count in 3..5, "3-5 crackling dust")
        // Breaking with drops, as survival breaking and explosions do.
        helper.level.destroyBlock(helper.absolutePos(CENTER), true)
        helper.assertItemEntityPresent(PowerContent.POWER_CRYSTAL.get(), CENTER, 2.0)
        helper.assertItemEntityPresent(IndustryContent.CRACKLING_DUST.get(), CENTER, 2.0)
        helper.succeed()
    }

    /**
     * v3 dropped crystals on any removal; replacing the block (a command) or breaking it in creative drops nothing.
     */
    private fun crystalClusterNoFreeDrops(helper: GameTestHelper) {
        helper.place<CrystalClusterBlockEntity>(CENTER, WorldgenContent.CRYSTAL_CLUSTER.get())
        helper.setBlock(CENTER, Blocks.AIR)
        helper.place<CrystalClusterBlockEntity>(CENTER.west(), WorldgenContent.CRYSTAL_CLUSTER.get())
        helper.destroyBlock(CENTER.west()) // removed without drops
        helper.place<CrystalClusterBlockEntity>(CENTER.east(), WorldgenContent.CRYSTAL_CLUSTER.get())
        val player = helper.makeMockServerPlayerInLevel()
        player.setGameMode(GameType.CREATIVE)
        player.gameMode.destroyBlock(helper.absolutePos(CENTER.east()))
        helper.assertBlockNotPresent(WorldgenContent.CRYSTAL_CLUSTER.get(), CENTER.east())
        helper.assertItemEntityNotPresent(PowerContent.POWER_CRYSTAL.get(), CENTER, 4.0)
        helper.assertItemEntityNotPresent(IndustryContent.CRACKLING_DUST.get(), CENTER, 4.0)
        helper.succeed()
    }

    private fun dataLoads(helper: GameTestHelper) {
        val access = helper.level.registryAccess()
        fun id(path: String) = Identifier.fromNamespaceAndPath(Femtocraft.ID, path)
        helper.assertTrue(access.lookupOrThrow(Registries.CONFIGURED_FEATURE).get(id("rift")).isPresent, "configured feature")
        helper.assertTrue(access.lookupOrThrow(Registries.PLACED_FEATURE).get(id("rift")).isPresent, "placed feature")
        helper.assertTrue(access.lookupOrThrow(NeoForgeRegistries.Keys.BIOME_MODIFIERS).get(id("rift")).isPresent, "biome modifier")
        val recipes = helper.level.server.recipeManager
        for (name in listOf("basic_circuit", "frame", "conduit", "logistics_item_chip_basic_reset", "smelting/riftiron_ingot_from_ore")) {
            helper.assertTrue(recipes.byKey(ResourceKey.create(Registries.RECIPE, id(name))).isPresent, "recipe $name")
        }
        helper.succeed()
    }
}
