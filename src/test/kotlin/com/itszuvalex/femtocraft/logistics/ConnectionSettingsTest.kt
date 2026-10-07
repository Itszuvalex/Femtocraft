package com.itszuvalex.femtocraft.logistics

import com.mojang.serialization.JsonOps
import com.google.gson.JsonObject
import net.minecraft.core.Direction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ConnectionSettingsTest {
    private val base = ConnectionSettings(5000.0, "default", false, ConnectionDirection.INPUT, Direction.NORTH)

    @Test
    fun Cycled_Mode_StepsThroughDirectionsAndWraps() {
        assertEquals(ConnectionDirection.OUTPUT, base.cycled(true, true).direction)
        assertEquals(ConnectionDirection.DISABLED, base.cycled(true, true).cycled(true, true).direction)
        assertEquals(ConnectionDirection.INPUT, base.cycled(true, true).cycled(true, true).cycled(true, true).direction)
        assertEquals(ConnectionDirection.DISABLED, base.cycled(true, false).direction)
    }

    @Test
    fun Cycled_Interface_StepsThroughFacesAndWraps() {
        val seen = generateSequence(base) { it.cycled(false, true) }.take(7).map { it.interfaceDirection }.toList()
        assertEquals(6, seen.take(6).toSet().size)
        assertEquals(seen[0], seen[6])
        assertEquals(base, base.cycled(false, true).cycled(false, false))
    }

    @Test
    fun Cycled_KeepsTheOtherSettings() {
        val cycled = base.copy(channel = "red", paused = true).cycled(true, true)
        assertEquals("red", cycled.channel)
        assertEquals(true, cycled.paused)
        assertEquals(Direction.NORTH, cycled.interfaceDirection)
    }

    @Test
    fun Defaults_EvenFacesInputOddOutputFacingBackIn() {
        assertEquals(ConnectionDirection.INPUT, ConnectionSettings.defaults(Direction.DOWN, 1.0).direction)
        assertEquals(ConnectionDirection.OUTPUT, ConnectionSettings.defaults(Direction.UP, 1.0).direction)
        assertEquals(Direction.DOWN, ConnectionSettings.defaults(Direction.UP, 1.0).interfaceDirection)
        assertEquals(ConnectionDirection.DISABLED, ConnectionSettings.defaults(null, 1.0).direction)
    }

    @Test
    fun Codec_RoundTrips() {
        val settings = ConnectionSettings(12.5, "blue", true, ConnectionDirection.OUTPUT, Direction.WEST)
        val json = ConnectionSettings.MAP_CODEC.codec().encodeStart(JsonOps.INSTANCE, settings).getOrThrow()
        assertEquals(settings, ConnectionSettings.MAP_CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow())
    }

    @Test
    fun Codec_MissingOptionalFields_TakeDefaults() {
        val json = JsonObject().apply { addProperty("flops", 7.0) }
        val settings = ConnectionSettings.MAP_CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow()
        assertEquals(ConnectionSettings(7.0, "default", false, ConnectionDirection.DISABLED, Direction.NORTH), settings)
    }

    @Test
    fun Codec_UnknownDirection_FallsBackToDisabled() {
        val json = JsonObject().apply {
            addProperty("flops", 7.0)
            addProperty("condir", "SIDEWAYS")
        }
        assertEquals(ConnectionDirection.DISABLED, ConnectionSettings.MAP_CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow().direction)
    }
}
