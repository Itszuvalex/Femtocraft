package com.itszuvalex.femtocraft.dev

import com.itszuvalex.femtocraft.dev.DevGameTests.place
import com.itszuvalex.femtocraft.dev.PowerGameTests.crystal
import com.itszuvalex.femtocraft.industry.ConfiguratorItem
import com.itszuvalex.femtocraft.industry.CrystalFocusingChamberBlockEntity
import com.itszuvalex.femtocraft.industry.CrystalFurnaceBlockEntity
import com.itszuvalex.femtocraft.industry.CrystalItemMachineBlockEntity
import com.itszuvalex.femtocraft.industry.CrystalLiquifierBlockEntity
import com.itszuvalex.femtocraft.industry.DemolisherBlockEntity
import com.itszuvalex.femtocraft.industry.DustRecipes
import com.itszuvalex.femtocraft.industry.FemtoFluids
import com.itszuvalex.femtocraft.industry.FrameBlockEntity
import com.itszuvalex.femtocraft.industry.FrameItem
import com.itszuvalex.femtocraft.industry.FrameMultiblocks
import com.itszuvalex.femtocraft.industry.GerminationChamberBlockEntity
import com.itszuvalex.femtocraft.industry.GerminationRecipes
import com.itszuvalex.femtocraft.industry.IndustryContent
import com.itszuvalex.femtocraft.industry.NanoFurnaceBlockEntity
import com.itszuvalex.femtocraft.industry.ProcessingMachineBlockEntity
import com.itszuvalex.femtocraft.industry.ShiftItem
import com.itszuvalex.femtocraft.power.CrystalChargingArrayBlockEntity
import com.itszuvalex.femtocraft.power.PowerConduitBlockEntity
import com.itszuvalex.femtocraft.power.PowerContent
import com.itszuvalex.femtocraft.power.PowerCrystals
import com.itszuvalex.itszulib.api.Modules
import com.itszuvalex.itszulib.api.adapters.IFluidStack
import com.itszuvalex.itszulib.api.adapters.IItemStack
import com.itszuvalex.itszulib.core.EnumAutomaticIO
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.GameType
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.material.Fluids
import net.neoforged.neoforge.fluids.FluidStack
import kotlin.random.Random

/**
 * Game tests for the industry area: machines, frames and frame-built multiblocks, tools.
 */
object IndustryGameTests {
    private val CENTER = BlockPos(4, 1, 4)

    fun register() {
        DevGameTests.test("nano_furnace_smelts", 400, ::nanoFurnaceSmelts)
        DevGameTests.test("demolisher_grinds_cobblestone", 400, ::demolisherGrinds)
        DevGameTests.test("dust_recipes_use_ore_and_dust_tags", body = ::dustRecipeTags)
        DevGameTests.test("crystal_furnace_runs_on_its_crystal", 400, ::crystalFurnace)
        DevGameTests.test("crystal_liquifier_makes_gritty_slurry", 400, ::liquifier)
        DevGameTests.test("wired_conduit_charges_crystal_machine", body = ::wiredPower)
        DevGameTests.test("machine_output_full_keeps_result", 400, ::outputFull)
        DevGameTests.test("configurator_cycles_face_io_then_storage", body = ::configurator)
        DevGameTests.test("frame_builds_germination_chamber", 400, ::frameBuilds)
        DevGameTests.test("frame_ignores_wrong_items", 200, ::frameWrongItems)
        DevGameTests.test("frame_teardown_drops_frames", body = ::frameTeardown)
        DevGameTests.test("germination_chamber_grows_seeds", 200, ::germinationGrows)
        DevGameTests.test("germination_chamber_output_full_keeps_harvest", 200, ::germinationOutputFull)
        DevGameTests.test("germination_results_reach_range_top", body = ::germinationRanges)
        DevGameTests.test("germination_chamber_teardown_drops_contents", body = ::germinationTeardown)
        DevGameTests.test("focusing_chamber_charges_large_crystal", body = ::focusingChamber)
        DevGameTests.test("shift_device_finds_destination", body = ::shiftDestination)
    }

    private fun stack(be: ProcessingMachineBlockEntity, slot: Int) = be.inventory.get(slot).toMinecraft()

    private fun nanoFurnaceSmelts(helper: GameTestHelper) {
        val be = helper.place<NanoFurnaceBlockEntity>(CENTER, IndustryContent.NANO_FURNACE.get())
        be.battery.setStorage(5000.0)
        be.inventory.setSlot(0, IItemStack.of(ItemStack(Items.RAW_IRON, 2)))
        helper.succeedWhen {
            helper.assertTrue(stack(be, 1).`is`(Items.IRON_INGOT), "iron smelted")
            helper.assertValueEqual(stack(be, 1).count, 1, "one so far")
        }
    }

    private fun demolisherGrinds(helper: GameTestHelper) {
        val be = helper.place<DemolisherBlockEntity>(CENTER, IndustryContent.DEMOLISHER.get())
        be.battery.setStorage(5000.0)
        be.inventory.setSlot(0, IItemStack.of(ItemStack(Items.COBBLESTONE)))
        helper.succeedWhen { helper.assertTrue(stack(be, 1).`is`(Items.GRAVEL), "cobblestone ground to gravel") }
    }

    private fun dustRecipeTags(helper: GameTestHelper) {
        val dust = DustRecipes.result(ItemStack(Items.IRON_ORE))
        helper.assertTrue(dust.`is`(IndustryContent.IRON_DUST.get()), "iron ore (c:ores/iron) grinds into c:dusts/iron, got $dust")
        helper.assertValueEqual(dust.count, DustRecipes.DEFAULT_DUST, "two dust per ore")
        val redstone = DustRecipes.result(ItemStack(Items.REDSTONE_ORE))
        helper.assertTrue(redstone.`is`(Items.REDSTONE) && redstone.count == 6, "redstone ore gives six redstone (v3 override), got $redstone")
        helper.assertValueEqual(DustRecipes.result(crystal(type = PowerCrystals.TYPE_LARGE)).count, 3, "large crystal -> 3 crackling dust")
        helper.succeed()
    }

    private fun crystalFurnace(helper: GameTestHelper) {
        val be = helper.place<CrystalFurnaceBlockEntity>(CENTER, IndustryContent.CRYSTAL_FURNACE.get())
        be.inventory.setSlot(CrystalItemMachineBlockEntity.CRYSTAL_SLOT, IItemStack.of(crystal(storage = 3000.0, max = 3000.0)))
        be.inventory.setSlot(0, IItemStack.of(ItemStack(Items.RAW_GOLD)))
        helper.succeedWhen {
            helper.assertTrue(stack(be, 1).`is`(Items.GOLD_INGOT), "gold smelted")
            helper.assertValueEqual(PowerCrystals.data(stack(be, CrystalItemMachineBlockEntity.CRYSTAL_SLOT))!!.storage, 3000.0 - ProcessingMachineBlockEntity.POWER_REQ, "the crystal paid for it")
        }
    }

    private fun liquifier(helper: GameTestHelper) {
        val be = helper.place<CrystalLiquifierBlockEntity>(CENTER, IndustryContent.CRYSTAL_LIQUIFIER.get())
        be.inventory.setSlot(CrystalLiquifierBlockEntity.CRYSTAL_SLOT, IItemStack.of(crystal(storage = 3000.0, max = 3000.0)))
        be.inventory.setSlot(0, IItemStack.of(ItemStack(Items.COBBLESTONE)))
        helper.succeedWhen {
            val fluid = be.tank.get(0).toMinecraft()
            helper.assertTrue(fluid.fluid == FemtoFluids.GRITTY_SLURRY.get(), "gritty slurry in the tank")
            helper.assertValueEqual(fluid.amount, 500, "500 mB per cobblestone")
        }
    }

    private fun wiredPower(helper: GameTestHelper) {
        val array = helper.place<CrystalChargingArrayBlockEntity>(BlockPos(2, 1, 2), PowerContent.CRYSTAL_CHARGING_ARRAY.get())
        array.battery.setStorage(5000.0)
        helper.place<PowerConduitBlockEntity>(BlockPos(3, 1, 2), PowerContent.POWER_CONDUIT.get())
        val furnace = helper.place<CrystalFurnaceBlockEntity>(BlockPos(4, 1, 2), IndustryContent.CRYSTAL_FURNACE.get())
        furnace.inventory.setSlot(CrystalItemMachineBlockEntity.CRYSTAL_SLOT, IItemStack.of(crystal(storage = 0.0, max = 1000.0)))
        helper.succeedWhen {
            val charge = PowerCrystals.data(stack(furnace, CrystalItemMachineBlockEntity.CRYSTAL_SLOT))!!.storage
            helper.assertTrue(charge >= 100.0, "the furnace's crystal is charged through the conduit (has $charge)")
            helper.assertValueEqual(charge + array.battery.storage(), 5000.0, "power is conserved")
        }
    }

    private fun outputFull(helper: GameTestHelper) {
        val be = helper.place<DemolisherBlockEntity>(CENTER, IndustryContent.DEMOLISHER.get())
        be.battery.setStorage(5000.0)
        be.inventory.setSlot(1, IItemStack.of(ItemStack(Items.DIRT, 64)))
        be.inventory.setSlot(0, IItemStack.of(ItemStack(Items.COBBLESTONE)))
        helper.runAfterDelay(ProcessingMachineBlockEntity.TICKS_REQ + 20L) {
            helper.assertTrue(be.processing.`is`(Items.COBBLESTONE), "still holding the input while the output is blocked")
            be.inventory.setSlot(1, IItemStack.Empty)
            helper.succeedWhen { helper.assertTrue(stack(be, 1).`is`(Items.GRAVEL), "result delivered once there is room") }
        }
    }

    private fun configurator(helper: GameTestHelper) {
        val be = helper.place<NanoFurnaceBlockEntity>(CENTER, IndustryContent.NANO_FURNACE.get())
        val config = be.getModule(Modules.ITEM_STORAGE_CONFIGURABLE, null)!!
        val face = Direction.UP
        helper.assertValueEqual(config.getStorageNameForAbsoluteFacing(face), ProcessingMachineBlockEntity.INPUT, "top starts as input")
        ConfiguratorItem.cycle(config, face, backward = false)
        helper.assertValueEqual(config.getIOForAbsoluteFacing(face), EnumAutomaticIO.INPUT, "IO cycles first")
        ConfiguratorItem.cycle(config, face, backward = false)
        ConfiguratorItem.cycle(config, face, backward = false)
        helper.assertValueEqual(config.getIOForAbsoluteFacing(face), EnumAutomaticIO.NONE, "IO wrapped")
        helper.assertValueEqual(config.getStorageNameForAbsoluteFacing(face), ProcessingMachineBlockEntity.NONE, "storage cycled on wrap")
        helper.succeed()
    }

    private val FRAME_AT = BlockPos(3, 1, 3)

    private fun placeFrame(helper: GameTestHelper, multi: com.itszuvalex.femtocraft.industry.FrameMultiblock): FrameBlockEntity {
        FrameItem.place(helper.level, helper.absolutePos(FRAME_AT), multi)
        return helper.getBlockEntity(FRAME_AT, FrameBlockEntity::class.java)
    }

    private fun frameBuilds(helper: GameTestHelper) {
        val controller = placeFrame(helper, FrameMultiblocks.GERMINATION_CHAMBER)
        helper.assertTrue(controller.info.info.isController, "frame controller formed")
        controller.storage.setSlot(0, IItemStack.of(ItemStack(IndustryContent.RIFTIRON_INGOT_ACTIVATED.get(), 10)))
        helper.succeedWhen {
            for (loc in FrameMultiblocks.GERMINATION_CHAMBER.takenLocations(FRAME_AT)) {
                helper.assertBlockPresent(IndustryContent.GERMINATION_CHAMBER.get(), loc)
            }
            val be = helper.getBlockEntity(FRAME_AT, GerminationChamberBlockEntity::class.java)
            helper.assertTrue(be.isController, "chamber controller formed")
            helper.assertTrue(helper.getBlockEntity(FRAME_AT.offset(1, 2, 1), GerminationChamberBlockEntity::class.java).info.info.controller == helper.absolutePos(FRAME_AT), "parts point at the controller")
        }
    }

    /**
     * v3's StorageUtils never compared items, so any ten items started the build.
     */
    private fun frameWrongItems(helper: GameTestHelper) {
        val controller = placeFrame(helper, FrameMultiblocks.GERMINATION_CHAMBER)
        controller.storage.setSlot(0, IItemStack.of(ItemStack(Items.IRON_INGOT, 10)))
        helper.runAfterDelay(100) {
            helper.assertFalse(controller.frameState.get()!!.building, "iron ingots do not start the build")
            helper.assertValueEqual(controller.storage.get(0).stackSize(), 10, "and are not consumed")
            helper.succeed()
        }
    }

    private fun frameTeardown(helper: GameTestHelper) {
        placeFrame(helper, FrameMultiblocks.CRYSTAL_FOCUSING_CHAMBER)
        helper.setBlock(FRAME_AT.offset(1, 1, 1), Blocks.AIR)
        helper.succeedWhen {
            for (loc in FrameMultiblocks.CRYSTAL_FOCUSING_CHAMBER.takenLocations(FRAME_AT)) helper.assertBlockPresent(Blocks.AIR, loc)
            val frames = helper.getEntities(EntityType.ITEM).map { it.item }.filter { it.`is`(IndustryContent.FRAME_ITEM.get()) }
            helper.assertValueEqual(frames.sumOf { it.count }, 8, "one frame per block")
            helper.assertTrue(frames.all { FrameItem.selection(it) == FrameMultiblocks.CRYSTAL_FOCUSING_CHAMBER.id }, "frames keep their selection")
        }
    }

    private fun formChamber(helper: GameTestHelper): GerminationChamberBlockEntity {
        FrameMultiblocks.GERMINATION_CHAMBER.formAt(helper.level, helper.absolutePos(FRAME_AT))
        val be = helper.getBlockEntity(FRAME_AT, GerminationChamberBlockEntity::class.java)
        val s = be.state()!!
        s.battery.setStorage(s.battery.maxStorage())
        s.tank.set(0, IFluidStack.of(FluidStack(Fluids.WATER, 10000)))
        s.input.setSlot(0, IItemStack.of(ItemStack(Items.WHEAT_SEEDS)))
        return be
    }

    /**
     * Shortens the running task so the test does not wait the recipe's minute.
     */
    private fun hurry(be: GerminationChamberBlockEntity) {
        val task = be.state()!!.task
        task.minTicks = 1
        task.baseGoal = 20.0
    }

    private fun germinationGrows(helper: GameTestHelper) {
        val be = formChamber(helper)
        helper.runAfterDelay(2) {
            hurry(be)
            helper.succeedWhen {
                val out = (1..3).map { be.storage.get(it).toMinecraft() }
                helper.assertTrue(out.any { it.`is`(Items.WHEAT) }, "wheat harvested: $out")
                helper.assertTrue(be.tank.get(0).amount() < 10000, "water used")
            }
        }
    }

    /**
     * v3 dropped the rolled results when the output was full and the next seed started.
     */
    private fun germinationOutputFull(helper: GameTestHelper) {
        val be = formChamber(helper)
        val s = be.state()!!
        for (i in 0 until 3) s.output.setSlot(i, IItemStack.of(ItemStack(Items.DIRT, 64)))
        s.input.setSlot(0, IItemStack.of(ItemStack(Items.WHEAT_SEEDS, 2)))
        helper.runAfterDelay(2) {
            hurry(be)
            helper.runAfterDelay(30) {
                helper.assertTrue(s.pending.isNotEmpty(), "harvest held while the output is full")
                helper.assertValueEqual(s.input.get(0).stackSize(), 1, "next seed waits")
                for (i in 0 until 3) s.output.setSlot(i, IItemStack.Empty)
                helper.succeedWhen { helper.assertTrue((0 until 3).any { s.output.get(it).toMinecraft().`is`(Items.WHEAT) }, "harvest delivered") }
            }
        }
    }

    /**
     * v3 rolled `nextInt(max - min) + min` and never reached the top of a range.
     */
    private fun germinationRanges(helper: GameTestHelper) {
        val cactus = GerminationRecipes.find(ItemStack(Items.CACTUS))!!
        val random = Random(1234)
        val counts = (0 until 200).map { cactus.roll(random).single().count }.toSet()
        helper.assertTrue(counts == setOf(2, 3), "cactus gives 2 or 3, got $counts")
        helper.succeed()
    }

    private fun germinationTeardown(helper: GameTestHelper) {
        val be = formChamber(helper)
        be.state()!!.output.setSlot(0, IItemStack.of(ItemStack(Items.WHEAT, 5)))
        helper.setBlock(FRAME_AT.offset(0, 1, 0), Blocks.AIR)
        helper.succeedWhen {
            for (loc in FrameMultiblocks.GERMINATION_CHAMBER.takenLocations(FRAME_AT)) helper.assertBlockPresent(Blocks.AIR, loc)
            helper.assertTrue(helper.getEntities(EntityType.ITEM).any { it.item.`is`(Items.WHEAT) }, "contents dropped")
            helper.assertTrue(helper.getEntities(EntityType.ITEM).any { it.item.`is`(Items.WHEAT_SEEDS) }, "input dropped")
        }
    }

    private fun focusingChamber(helper: GameTestHelper) {
        FrameMultiblocks.CRYSTAL_FOCUSING_CHAMBER.formAt(helper.level, helper.absolutePos(FRAME_AT))
        val be = helper.getBlockEntity(FRAME_AT.offset(1, 0, 0), CrystalFocusingChamberBlockEntity::class.java)
        be.small.setSlot(0, IItemStack.of(crystal(storage = 0.0, max = 1000.0, passive = 1f)))
        be.large.setSlot(0, IItemStack.of(crystal(storage = 0.0, max = 5000.0, type = PowerCrystals.TYPE_LARGE)))
        helper.succeedWhen {
            val big = PowerCrystals.data(be.large.get(0).toMinecraft())!!.storage
            helper.assertTrue(big >= 20.0, "large crystal charged from the small one (has $big)")
        }
    }

    private fun shiftDestination(helper: GameTestHelper) {
        val player = helper.makeMockPlayer(GameType.SURVIVAL)
        val start = helper.absolutePos(BlockPos(0, 1, 4))
        player.snapTo(start.x + 0.5, start.y.toDouble(), start.z + 0.5, -90f, 0f)
        val dest = ShiftItem.destination(helper.level, player)
        helper.assertTrue(dest != null && dest.x - start.x >= 7, "teleports about 8 blocks east, got $dest from $start")
        helper.succeed()
    }
}
