package com.itszuvalex.femtocraft.dev

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.FemtoItems
import com.itszuvalex.femtocraft.FemtoModules
import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.PowerManager
import com.itszuvalex.femtocraft.power.item.CrystalData
import com.itszuvalex.femtocraft.power.item.PowerCrystalItem
import com.itszuvalex.femtocraft.power.tile.CrystalMountBlockEntity
import com.itszuvalex.femtocraft.power.tile.PowerGeneratorBlockEntity
import com.itszuvalex.femtocraft.power.tile.PowerPedestalBlockEntity
import com.itszuvalex.itszulib.ItszuLib
import com.itszuvalex.itszulib.api.adapters.IItemStack
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.gametest.framework.FunctionGameTestInstance
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.gametest.framework.TestData
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.entity.BlockEntity
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModList
import net.neoforged.neoforge.event.RegisterGameTestsEvent
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Consumer

/**
 * Femtocraft game tests, run with `./gradlew runGameTestServer`. Tests run in `femtocraft:test_area`, an empty
 * 9x5x9 structure, so multi-block setups fit.
 */
object DevGameTests {
    private val TEST_FUNCTIONS: DeferredRegister<Consumer<GameTestHelper>> =
        DeferredRegister.create(Registries.TEST_FUNCTION, Femtocraft.ID)

    private val TESTS = mutableListOf<Pair<DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>>, Int>>()

    private val AREA = Identifier.fromNamespaceAndPath(Femtocraft.ID, "test_area")

    /**
     * @param maxTicks Timeout for tests that wait (`succeedWhen`).
     */
    fun test(name: String, maxTicks: Int = 100, body: (GameTestHelper) -> Unit) {
        TESTS += TEST_FUNCTIONS.register(name) { -> Consumer<GameTestHelper> { body(it) } } to maxTicks
    }

    init {
        test("itszulib_loaded") { helper ->
            helper.assertTrue(ModList.get().isLoaded(ItszuLib.ID), "ItszuLib dependency is not loaded")
            helper.succeed()
        }
        PowerGameTests.register()
    }

    fun register(modBus: IEventBus) {
        TEST_FUNCTIONS.register(modBus)
        modBus.addListener(::registerTests)
    }

    private fun registerTests(event: RegisterGameTestsEvent) {
        val environment = event.registerEnvironment(Identifier.fromNamespaceAndPath(Femtocraft.ID, "default"))
        TESTS.forEach { (test, maxTicks) ->
            event.registerTest(
                test.id,
                FunctionGameTestInstance(test.key, TestData(environment, AREA, maxTicks, 0, true, Rotation.NONE, false, 1, 1, false, 2)),
            )
        }
    }

    // Shared helpers

    inline fun <reified T : BlockEntity> GameTestHelper.place(pos: BlockPos, block: net.minecraft.world.level.block.Block): T {
        setBlock(pos, block)
        return getBlockEntity(pos, T::class.java)
    }

    fun crystal(storage: Double, current: Double = storage / 2, transfer: Int = 100): ItemStack {
        val stack = ItemStack(FemtoItems.POWER_CRYSTAL.get())
        PowerCrystalItem.initialize(stack, "Test", CrystalData.TYPE_LARGE, -1, storage, 0f, transfer)
        (stack.item as PowerCrystalItem).setStorageCurrent(stack, current)
        return stack
    }
}

/**
 * Crystal power network: parent/child linking, power distribution, the distributed generator -> sink path.
 */
object PowerGameTests {
    private fun GameTestHelper.mountWithCrystal(pos: BlockPos, storage: Double, current: Double): CrystalMountBlockEntity {
        val mount: CrystalMountBlockEntity = with(DevGameTests) { place(pos, FemtoBlocks.CRYSTAL_MOUNT.get()) }
        mount.inventory.setSlot(0, IItemStack.of(DevGameTests.crystal(storage, current)))
        return mount
    }

    fun register() {
        DevGameTests.test("power_mount_adopts_node_in_range") { helper ->
            val mount = helper.mountWithCrystal(BlockPos(2, 1, 4), 1000.0, 500.0)
            val nodeBe: DevPowerNodeBlockEntity = with(DevGameTests) { helper.place(BlockPos(5, 1, 4), DevContent.TRANSFER_NODE.block.get()) }
            val node = nodeBe.node
            // Block entities placed mid-tick get onLoad (and so join the power tree) on the next tick.
            helper.succeedWhen {
                helper.assertTrue(mount.node.getNodeLoc() == node.getParentLoc(), "node parent: ${node.getParentLoc()}")
                helper.assertTrue(node.getNodeLoc() in mount.node.getChildrenLocs()!!, "mount children should contain the node")
                helper.assertTrue(PowerManager.nodeTracker.isLocationTracked(mount.node.getNodeLoc()), "mount tracked")
            }
        }

        DevGameTests.test("power_mount_without_crystal_is_not_a_node") { helper ->
            val mount: CrystalMountBlockEntity = with(DevGameTests) { helper.place(BlockPos(2, 1, 4), FemtoBlocks.CRYSTAL_MOUNT.get()) }
            helper.assertTrue(!PowerManager.nodeTracker.isLocationTracked(mount.node.getNodeLoc()), "empty mount must not be tracked")
            mount.inventory.setSlot(0, IItemStack.of(DevGameTests.crystal(1000.0)))
            helper.assertTrue(PowerManager.nodeTracker.isLocationTracked(mount.node.getNodeLoc()), "mount tracked after crystal inserted")
            mount.inventory.setSlot(0, IItemStack.Empty)
            helper.assertTrue(!PowerManager.nodeTracker.isLocationTracked(mount.node.getNodeLoc()), "mount untracked after crystal removed")
            helper.succeed()
        }

        DevGameTests.test("power_mount_distributes_to_emptier_child") { helper ->
            val mount = helper.mountWithCrystal(BlockPos(2, 1, 4), 1000.0, 900.0)
            val nodeBe: DevPowerNodeBlockEntity = with(DevGameTests) { helper.place(BlockPos(5, 1, 4), DevContent.TRANSFER_NODE.block.get()) }
            nodeBe.node.setPower(100.0)
            helper.succeedWhen {
                helper.assertTrue(nodeBe.node.getPowerCurrent() > 100.0, "child did not receive power: ${nodeBe.node.getPowerCurrent()}")
                helper.assertTrue(mount.node.getPowerCurrent() < 900.0, "mount did not lose power")
            }
        }

        DevGameTests.test("power_generator_dumps_into_sink", maxTicks = 200) { helper ->
            // sink (4,1,4) <- pedestal (4,2,4) <- mount (4,3,4)
            with(DevGameTests) { helper.place<BlockEntity>(BlockPos(4, 1, 4), FemtoBlocks.POWER_SINK.get()) }
            val pedestal: PowerPedestalBlockEntity = with(DevGameTests) { helper.place(BlockPos(4, 2, 4), FemtoBlocks.POWER_PEDESTAL.get()) }
            val mount = helper.mountWithCrystal(BlockPos(4, 3, 4), 1000.0, 0.0)
            pedestal.onPlaced(null, ItemStack.EMPTY)
            helper.assertTrue(mount.loc == pedestal.mountLoc, "pedestal not linked to mount")
            val generator: PowerGeneratorBlockEntity = with(DevGameTests) { helper.place(BlockPos(1, 1, 1), FemtoBlocks.POWER_GENERATOR.get()) }
            generator.charge(PowerGeneratorBlockEntity.POWER_MAXIMUM.toDouble(), true)
            helper.succeedWhen {
                helper.assertTrue(mount.node.getPowerCurrent() > 0.0, "sink's crystal received no power")
                helper.assertTrue(generator.powerCurrent < PowerGeneratorBlockEntity.POWER_MAXIMUM, "generator did not drain")
            }
        }

        DevGameTests.test("power_node_save_load_keeps_links") { helper ->
            val mount = helper.mountWithCrystal(BlockPos(2, 1, 4), 1000.0, 0.0) // empty, so it does not push power into the node
            val nodeBe: DevPowerNodeBlockEntity = with(DevGameTests) { helper.place(BlockPos(5, 1, 4), DevContent.TRANSFER_NODE.block.get()) }
            nodeBe.node.setPower(42.0)
            helper.runAfterDelay(2) {
            val registries = helper.level.registryAccess()
            val copy = BlockEntity.loadStatic(nodeBe.blockPos, nodeBe.blockState, nodeBe.saveWithFullMetadata(registries), registries)
                as DevPowerNodeBlockEntity
            helper.assertTrue(mount.node.getNodeLoc() == copy.node.getParentLoc(), "parent after load: ${copy.node.getParentLoc()}")
            helper.assertValueEqual(42.0, copy.node.getPowerCurrent(), "power after load")
            helper.assertValueEqual(nodeBe.node.getColor(), copy.node.getColor(), "color after load")
            val mountCopy = BlockEntity.loadStatic(mount.blockPos, mount.blockState, mount.saveWithFullMetadata(registries), registries)
                as CrystalMountBlockEntity
            helper.assertTrue(nodeBe.node.getNodeLoc() in mountCopy.node.getChildrenLocs()!!, "mount children after load")
            helper.assertValueEqual(1000.0, mountCopy.node.getPowerMax(), "crystal capacity after load")
            helper.succeed()
            }
        }

        DevGameTests.test("power_crystal_mount_drops_crystal_on_break") { helper ->
            helper.mountWithCrystal(BlockPos(2, 1, 4), 1000.0, 500.0)
            helper.destroyBlock(BlockPos(2, 1, 4))
            helper.succeedWhen { helper.assertItemEntityPresent(FemtoItems.POWER_CRYSTAL.get(), BlockPos(2, 1, 4), 2.0) }
        }

        DevGameTests.test("power_node_module_is_exposed") { helper ->
            val mount = helper.mountWithCrystal(BlockPos(2, 1, 4), 1000.0, 500.0)
            helper.assertTrue(mount.getModule(FemtoModules.POWER_NODE, null) === mount.node, "POWER_NODE module")
            helper.succeed()
        }
    }
}
