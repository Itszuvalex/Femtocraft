package com.itszuvalex.femtocraft

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SmokeTest {
    @Test
    fun modId_IsLowercase() {
        assertEquals(Femtocraft.ID.lowercase(), Femtocraft.ID)
    }
}
