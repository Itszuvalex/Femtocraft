package com.itszuvalex.femtocraft.computation

import com.itszuvalex.femtocraft.archive.ArchiveState
import com.itszuvalex.itszulib.core.Distributable
import com.itszuvalex.itszulib.core.DistributionAlgorithm
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** A job that takes up to [room] FLOPS. */
private class TestJob(var room0: Double, private val perTick: Double = Double.MAX_VALUE) : Distributable {
    var received = 0.0
    override val max: Double get() = room0
    override val amount: Double get() = received
    override val transferMax: Double get() = perTick
    override fun add(amount: Double): Double = minOf(amount, room0 - received).also { received += it }
    override fun remove(amount: Double): Double = 0.0
}

private class Rig(vararg tiers: ProcessorTier, var power: Double = 10_000.0, temperature: Double = 20.0) {
    val heat = MainframeHeat(temperature)
    val computer = MainframeComputer({ tiers.toList() }, heat, { power }, { power -= it })
}

class MainframeHeatTest {
    @Test
    fun Speed_FullBelowThreshold_LinearToZeroAtMax() {
        assertEquals(1.0, MainframeHeat(20.0).speed())
        assertEquals(1.0, MainframeHeat(MainframeHeat.THROTTLE_START).speed())
        assertEquals(0.5, MainframeHeat((MainframeHeat.THROTTLE_START + MainframeHeat.MAX) / 2).speed(), 1e-9)
        assertEquals(0.0, MainframeHeat(MainframeHeat.MAX + 10).speed())
    }

    @Test
    fun Cool_MovesTowardsAmbientByRate() {
        val heat = MainframeHeat(100.0)
        heat.cool(20.0, 0.25)
        assertEquals(80.0, heat.temperature, 1e-9)
    }

    @Test
    fun Ambient_PlainsIsTwentyColderBiomesColder() {
        assertEquals(20.0, MainframeHeat.ambient(0.8f), 1e-6)
        assertTrue(MainframeHeat.ambient(0.0f) < 5.0)
        assertTrue(MainframeHeat.ambient(2.0f) > 45.0)
    }

    @Test
    fun Uncooled_SettlesThrottled_CooledRunsFull() {
        fun settle(rate: Double): Double {
            val rig = Rig(*Array(4) { ProcessorTier.MICRO_LOGIC_CORE })
            var flops = 0.0
            repeat(5000) {
                rig.computer.newTick()
                flops = rig.computer.remove(Double.MAX_VALUE)
                rig.heat.cool(20.0, rate)
                rig.power = 10_000.0
            }
            return flops
        }
        val uncooled = settle(MainframeHeat.BASE_COOLING)
        val iced = settle(MainframeHeat.BASE_COOLING + 2 * 0.02)
        assertTrue(uncooled < 80.0 && uncooled > 40.0, "uncooled $uncooled")
        assertEquals(80.0, iced, 1e-6)
    }
}

class MainframeComputerTest {
    @Test
    fun Remove_SpendsPowerOnlyForWhatIsTaken_AndHeats() {
        val rig = Rig(ProcessorTier.MICRO_LOGIC_CORE)
        assertEquals(20.0, rig.computer.remove(5.0) + rig.computer.remove(15.0) + rig.computer.remove(5.0), 1e-9)
        assertEquals(10_000.0 - 20.0 * ProcessorTier.MICRO_LOGIC_CORE.powerPerFlop, rig.power, 1e-9)
        assertEquals(20.0 + 20.0 * ProcessorTier.MICRO_LOGIC_CORE.heatPerFlop, rig.heat.temperature, 1e-9)
    }

    @Test
    fun Available_LimitedByPower() {
        val rig = Rig(ProcessorTier.MICRO_LOGIC_CORE, power = 5.0)
        assertEquals(10.0, rig.computer.available(), 1e-9)
        assertEquals(10.0, rig.computer.remove(100.0), 1e-9)
        assertEquals(0.0, rig.power, 1e-9)
    }

    @Test
    fun NewTick_RestoresCapacity() {
        val rig = Rig(ProcessorTier.ORPHEUS)
        rig.computer.remove(1000.0)
        assertEquals(0.0, rig.computer.available(), 1e-9)
        rig.computer.newTick()
        assertEquals(ProcessorTier.ORPHEUS.flopsPerTick, rig.computer.available(), 1e-9)
    }

    @Test
    fun Distribution_EfficientComputerGivesFirst_NoWasteWithoutJobs() {
        val micro = Rig(ProcessorTier.MICRO_LOGIC_CORE)
        val orpheus = Rig(ProcessorTier.ORPHEUS)
        val job = TestJob(50.0)
        DistributionAlgorithm(listOf(micro.computer, orpheus.computer), listOf(), listOf(job)).distribute()
        assertEquals(50.0, job.received, 1e-9)
        assertEquals(50.0, orpheus.computer.computed, 1e-9)
        assertEquals(0.0, micro.computer.computed, 1e-9)
        assertEquals(10_000.0, micro.power, 1e-9)

        DistributionAlgorithm(listOf(micro.computer), listOf(), listOf()).distribute()
        assertEquals(10_000.0, micro.power, 1e-9)
    }
}

class ArchiveJobTest {
    @Test
    fun Add_TurnsFlopsIntoPoints_KeepsTheRemainder() {
        val state = ArchiveState {}
        val job = ArchiveJob { state }
        assertEquals(450.0, job.add(450.0), 1e-9)
        assertEquals(2L, state.computedPoints)
        assertEquals(50.0, job.remainder, 1e-9)
    }

    @Test
    fun Add_StopsAtTheArchivesRoom() {
        val state = ArchiveState {}
        val job = ArchiveJob { state }
        val all = ArchiveState.COMPUTED_CAP * ArchiveJob.FLOPS_PER_POINT
        assertEquals(all, job.add(all + 1000.0), 1e-9)
        assertEquals(ArchiveState.COMPUTED_CAP, state.computedPoints)
        assertEquals(0.0, job.max - job.amount, 1e-9)
        assertEquals(0.0, job.add(10.0), 1e-9)
    }

    @Test
    fun Add_NoArchive_TakesNothing() {
        assertEquals(0.0, ArchiveJob { null }.add(1000.0), 1e-9)
    }
}
