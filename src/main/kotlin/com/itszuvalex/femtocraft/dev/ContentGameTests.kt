package com.itszuvalex.femtocraft.dev

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.FemtoComponents
import com.itszuvalex.femtocraft.FemtoItems
import com.itszuvalex.femtocraft.cyber.BaseSeedItem
import com.itszuvalex.femtocraft.cyber.CyberBaseBlockEntity
import com.itszuvalex.femtocraft.cyber.CyberMachineRegistry
import com.itszuvalex.femtocraft.cyber.CybermaterialRegistry
import com.itszuvalex.femtocraft.cyber.GrowthChamberBlockEntity
import com.itszuvalex.femtocraft.industry.DustRecipes
import com.itszuvalex.femtocraft.industry.FrameBlockEntity
import com.itszuvalex.femtocraft.industry.FrameItem
import com.itszuvalex.femtocraft.industry.MaterialProcessorBlockEntity
import com.itszuvalex.femtocraft.industry.MultiblockMaterialProcessor
import com.itszuvalex.femtocraft.logistics.ItemRepositoryBlockEntity
import com.itszuvalex.femtocraft.nanite.NaniteHiveSmallBlockEntity
import com.itszuvalex.femtocraft.worldgen.CrystalClusterBlockEntity
import com.itszuvalex.itszulib.api.adapters.IItemStack
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks

private fun at(x: Int, y: Int, z: Int) = BlockPos(x, y, z)

object CyberGameTests {
    fun register() {
        DevGameTests.test("cyber_materials_convert_by_tag") { helper ->
            helper.assertTrue(CybermaterialRegistry.getReplacement(Blocks.STONE.defaultBlockState())?.block == FemtoBlocks.CYBERWEAVE.get(), "stone")
            helper.assertTrue(CybermaterialRegistry.getReplacement(Blocks.OAK_LOG.defaultBlockState())?.block == FemtoBlocks.CYBERWOOD.get(), "log")
            helper.assertTrue(CybermaterialRegistry.getReplacement(Blocks.OAK_LEAVES.defaultBlockState())?.block == FemtoBlocks.CYBERLEAF.get(), "leaves")
            helper.assertTrue(CybermaterialRegistry.getReplacement(FemtoBlocks.CYBERWOOD.get().defaultBlockState()) == null, "cyberwood converts to itself")
            helper.assertTrue(CybermaterialRegistry.getReplacement(Blocks.GLASS.defaultBlockState()) == null, "glass")
            helper.succeed()
        }

        DevGameTests.test("cyber_base_builds_growth_chamber", maxTicks = 40) { helper ->
            val origin = helper.absolutePos(at(2, 1, 2))
            helper.assertTrue(CyberBaseBlockEntity.place(helper.level, 2, origin), "base placement failed")
            val base = helper.level.getBlockEntity(origin) as CyberBaseBlockEntity
            helper.assertTrue(base.multiblock.isController, "origin is controller")
            helper.assertTrue((helper.level.getBlockEntity(origin.offset(1, 0, 1)) as CyberBaseBlockEntity).multiblock.controllerPos == origin, "part")
            helper.assertValueEqual(6, base.remainingSlots(), "slots before")
            helper.assertTrue(base.buildMachine(CyberMachineRegistry.GROWTH_CHAMBER), "build refused")
            helper.assertValueEqual(4, base.remainingSlots(), "slots after")
            helper.assertBlockPresent(FemtoBlocks.CYBER_MACHINE_IN_PROGRESS.get(), at(3, 2, 3))
            helper.succeedWhen {
                helper.assertBlockPresent(FemtoBlocks.GROWTH_CHAMBER.get(), at(2, 2, 2))
                helper.assertBlockPresent(FemtoBlocks.GROWTH_CHAMBER.get(), at(3, 3, 3))
                val gc = helper.getBlockEntity(at(3, 3, 3), GrowthChamberBlockEntity::class.java)
                helper.assertTrue(gc.multiblock.controllerPos == origin.above(), "chamber controller")
            }
        }

        DevGameTests.test("cyber_base_break_removes_machines_and_drops_seed", maxTicks = 40) { helper ->
            val origin = helper.absolutePos(at(2, 1, 2))
            CyberBaseBlockEntity.place(helper.level, 1, origin)
            val base = helper.level.getBlockEntity(origin) as CyberBaseBlockEntity
            base.buildMachine(CyberMachineRegistry.BIO_BEACON)
            helper.runAfterDelay(3) {
                helper.assertBlockPresent(FemtoBlocks.BIO_BEACON.get(), at(2, 2, 2))
                helper.destroyBlock(at(2, 1, 2))
                helper.succeedWhen {
                    helper.assertBlockNotPresent(FemtoBlocks.BIO_BEACON.get(), at(2, 2, 2))
                    helper.assertBlockNotPresent(FemtoBlocks.BIO_BEACON.get(), at(2, 3, 2))
                    helper.assertItemEntityPresent(FemtoItems.BASE_SEED.get(), at(2, 1, 2), 2.0)
                }
            }
        }

        DevGameTests.test("growth_chamber_grows_wheat", maxTicks = 700) { helper ->
            val origin = helper.absolutePos(at(2, 1, 2))
            CyberBaseBlockEntity.place(helper.level, 2, origin)
            (helper.level.getBlockEntity(origin) as CyberBaseBlockEntity).buildMachine(CyberMachineRegistry.GROWTH_CHAMBER)
            var inserted = false
            helper.succeedWhen {
                val gc = helper.level.getBlockEntity(origin.above()) as? GrowthChamberBlockEntity
                helper.assertTrue(gc != null && gc.multiblock.isController, "chamber not built yet")
                if (!inserted) {
                    gc!!.inventory.setSlot(0, IItemStack.of(ItemStack(Items.WHEAT_SEEDS, 2)))
                    inserted = true
                }
                val outputs = (1..9).map { gc!!.inventory.get(it).toMinecraft() }
                helper.assertTrue(outputs.any { it.`is`(Items.WHEAT) }, "no wheat yet (progress ${gc!!.progress})")
            }
        }

        DevGameTests.test("base_seed_size_component") { helper ->
            val stack = BaseSeedItem.createStack(1, 3)
            helper.assertTrue(stack.get(FemtoComponents.BASE_SIZE.get()) == 3, "size")
            helper.succeed()
        }
    }
}

object IndustryGameTests {
    fun register() {
        DevGameTests.test("frame_collects_resources_and_builds", maxTicks = 300) { helper ->
            val controller = helper.absolutePos(at(2, 1, 2))
            FrameItem.placeFrames(helper.level, MultiblockMaterialProcessor, controller)
            val frame = helper.level.getBlockEntity(controller) as FrameBlockEntity
            helper.assertTrue(frame.multiblock.isController, "frame controller")
            frame.inventory.setSlot(0, IItemStack.of(ItemStack(Items.COBBLESTONE, 24)))
            helper.assertTrue(frame.isBuilding, "frame did not start building")
            helper.succeedWhen {
                helper.assertBlockPresent(FemtoBlocks.MATERIAL_PROCESSOR.get(), at(2, 1, 2))
                helper.assertBlockPresent(FemtoBlocks.MATERIAL_PROCESSOR.get(), at(3, 3, 3))
                val mp = helper.getBlockEntity(at(3, 3, 3), MaterialProcessorBlockEntity::class.java)
                helper.assertTrue(mp.multiblock.controllerPos == controller, "processor controller")
            }
        }

        DevGameTests.test("frame_break_drops_frames") { helper ->
            val controller = helper.absolutePos(at(2, 1, 2))
            FrameItem.placeFrames(helper.level, MultiblockMaterialProcessor, controller)
            helper.destroyBlock(at(3, 2, 3))
            helper.succeedWhen {
                helper.assertBlockNotPresent(FemtoBlocks.FRAME.get(), at(2, 1, 2))
                helper.assertItemEntityCountIs(FemtoItems.FRAME.get(), at(2, 2, 2), 4.0, MultiblockMaterialProcessor.numFrames)
            }
        }

        DevGameTests.test("material_processor_smelts_with_furnace_assembly", maxTicks = 400) { helper ->
            val controller = helper.absolutePos(at(2, 1, 2))
            MultiblockMaterialProcessor.formAtLocation(helper.level, controller)
            val mp = helper.level.getBlockEntity(controller) as MaterialProcessorBlockEntity
            mp.inventory.setSlot(MaterialProcessorBlockEntity.ASSEMBLY_START, IItemStack.of(ItemStack(FemtoItems.FURNACE_ASSEMBLY.get())))
            mp.inventory.setSlot(MaterialProcessorBlockEntity.POWER_SLOT, IItemStack.of(DevGameTests.crystal(100000.0, 100000.0)))
            mp.inventory.setSlot(MaterialProcessorBlockEntity.INPUT_START, IItemStack.of(ItemStack(Items.RAW_IRON, 1)))
            helper.succeedWhen {
                helper.assertTrue((0 until MaterialProcessorBlockEntity.NUM_OUTPUT).any { mp.getOutputItem(it).`is`(Items.IRON_INGOT) }, "no iron ingot yet")
                helper.assertTrue(mp.getCurrentPower() < 100000.0, "no power used")
            }
        }

        DevGameTests.test("material_processor_keeps_inventory_as_item") { helper ->
            val controller = helper.absolutePos(at(2, 1, 2))
            MultiblockMaterialProcessor.formAtLocation(helper.level, controller)
            val mp = helper.level.getBlockEntity(controller) as MaterialProcessorBlockEntity
            mp.inventory.setSlot(0, IItemStack.of(ItemStack(Items.DIAMOND, 5)))
            helper.destroyBlock(at(3, 2, 3))
            helper.succeedWhen {
                helper.assertBlockNotPresent(FemtoBlocks.MATERIAL_PROCESSOR.get(), at(2, 1, 2))
                val item = helper.getEntities(net.minecraft.world.entity.EntityType.ITEM).map { it.item }.firstOrNull { it.`is`(FemtoItems.MULTIBLOCK.get()) }
                helper.assertTrue(item != null, "no multiblock item dropped")
                helper.assertTrue(item!!.get(FemtoComponents.MULTIBLOCK.get()) == MultiblockMaterialProcessor.name, "multiblock name")
                MultiblockMaterialProcessor.formAtLocationFromItem(helper.level, controller, item)
                val copy = helper.level.getBlockEntity(controller) as MaterialProcessorBlockEntity
                helper.assertTrue(copy.inventory.get(0).toMinecraft().`is`(Items.DIAMOND), "inventory not restored")
                helper.assertValueEqual(5, copy.inventory.get(0).stackSize(), "count restored")
            }
        }

        DevGameTests.test("grinder_dust_from_ore_tags") { helper ->
            val dust = DustRecipes.getDust(ItemStack(Items.REDSTONE_ORE))
            helper.assertTrue(dust.`is`(Items.REDSTONE), "redstone ore -> redstone, got $dust")
            helper.assertValueEqual(6, dust.count, "redstone override count")
            helper.assertTrue(DustRecipes.getDust(ItemStack(Items.STONE)).isEmpty, "stone is not an ore")
            helper.succeed()
        }
    }
}

object LogisticsGameTests {
    fun register() {
        DevGameTests.test("item_repository_joins_nanite_hive") { helper ->
            val hive: NaniteHiveSmallBlockEntity = with(DevGameTests) { helper.place(at(1, 1, 1), FemtoBlocks.NANITE_HIVE_SMALL.get()) }
            val repo: ItemRepositoryBlockEntity = with(DevGameTests) { helper.place(at(6, 1, 6), FemtoBlocks.ITEM_REPOSITORY.get()) }
            helper.succeedWhen {
                helper.assertTrue(repo.naniteNode.getHiveLoc() == hive.hive.getHiveLoc(), "repository hive: ${repo.naniteNode.getHiveLoc()}")
                helper.assertTrue(repo.naniteNode.getNodeLoc() in hive.hive.getNodeLocs(), "hive nodes")
            }
        }

        DevGameTests.test("indexed_storage_indexes_items_and_tags") { helper ->
            val repo: ItemRepositoryBlockEntity = with(DevGameTests) { helper.place(at(2, 1, 2), FemtoBlocks.ITEM_REPOSITORY.get()) }
            repo.inventory.setSlot(3, IItemStack.of(ItemStack(Items.OAK_LOG, 4)))
            repo.inventory.setSlot(7, IItemStack.of(ItemStack(Items.BIRCH_LOG, 4)))
            helper.assertValueEqual(setOf(3), repo.inventory.getSlotsByItem(Items.OAK_LOG), "oak slots")
            helper.assertValueEqual(setOf(3, 7), repo.inventory.getSlotsByTag(net.minecraft.tags.ItemTags.LOGS), "log tag slots")
            repo.inventory.setSlot(3, IItemStack.Empty)
            helper.assertTrue(!repo.inventory.containsItem(Items.OAK_LOG), "index not updated after removal")
            helper.succeed()
        }

        DevGameTests.test("crystal_cluster_drops_crystals") { helper ->
            val cluster: CrystalClusterBlockEntity = with(DevGameTests) { helper.place(at(2, 1, 2), FemtoBlocks.CRYSTAL_CLUSTER.get()) }
            cluster.color = 0x12345678
            helper.destroyBlock(at(2, 1, 2))
            helper.succeedWhen {
                val crystals = helper.getEntities(net.minecraft.world.entity.EntityType.ITEM).map { it.item }.filter { it.`is`(FemtoItems.POWER_CRYSTAL.get()) }
                helper.assertTrue(crystals.size >= CrystalClusterBlockEntity.DROP_CRYSTALS_MIN, "dropped ${crystals.size}")
                helper.assertTrue(crystals.all { FemtoItems.POWER_CRYSTAL.get().getColor(it) == 0x12345678 }, "crystal color")
            }
        }
    }
}
