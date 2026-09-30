package com.itszuvalex.femtocraft.dev

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.itszulib.ItszuLib
import net.minecraft.core.registries.Registries
import net.minecraft.gametest.framework.FunctionGameTestInstance
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.gametest.framework.TestData
import net.minecraft.resources.Identifier
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModList
import net.neoforged.neoforge.event.RegisterGameTestsEvent
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Consumer

/**
 * Game tests, run with `./gradlew runGameTestServer`. Each test uses vanilla's 1x1x1 `minecraft:empty` structure.
 */
object DevGameTests {
    private val TEST_FUNCTIONS: DeferredRegister<Consumer<GameTestHelper>> =
        DeferredRegister.create(Registries.TEST_FUNCTION, Femtocraft.ID)

    private val TESTS = mutableListOf<DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>>>()

    private fun test(name: String, body: (GameTestHelper) -> Unit) {
        TESTS += TEST_FUNCTIONS.register(name) { -> Consumer<GameTestHelper> { body(it) } }
    }

    init {
        test("itszulib_loaded") { helper ->
            helper.assertTrue(ModList.get().isLoaded(ItszuLib.ID), "ItszuLib dependency is not loaded")
            helper.succeed()
        }
    }

    fun register(modBus: IEventBus) {
        TEST_FUNCTIONS.register(modBus)
        modBus.addListener(::registerTests)
    }

    private fun registerTests(event: RegisterGameTestsEvent) {
        val environment = event.registerEnvironment(Identifier.fromNamespaceAndPath(Femtocraft.ID, "default"))
        TESTS.forEach { test ->
            event.registerTest(
                test.id,
                FunctionGameTestInstance(test.key, TestData(environment, Identifier.withDefaultNamespace("empty"), 100, 0, true)),
            )
        }
    }
}
