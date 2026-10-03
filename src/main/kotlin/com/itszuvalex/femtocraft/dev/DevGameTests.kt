package com.itszuvalex.femtocraft.dev

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.itszulib.ItszuLib
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.gametest.framework.FunctionGameTestInstance
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.gametest.framework.TestData
import net.minecraft.resources.Identifier
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
        IndustryGameTests.register()
        NaniteGameTests.register()
        LogisticsGameTests.register()
        CyberGameTests.register()
        ArchiveGameTests.register()
        ComputationGameTests.register()
        VaultGameTests.register()
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

}
