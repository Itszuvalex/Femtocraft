package com.itszuvalex.femtocraft.industry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CraftJobTest {
    @Test
    fun Add_TakesUpToOneCraftWhileWanted() {
        var wants = true
        val job = CraftJob(100.0) { wants }
        assertEquals(60.0, job.add(60.0))
        assertFalse(job.ready())
        assertEquals(40.0, job.add(60.0))
        assertTrue(job.ready())
        assertEquals(0.0, job.room)
        job.spend()
        assertEquals(0.0, job.stored)
        wants = false
        assertEquals(0.0, job.add(50.0))
        assertEquals(0.0, job.room)
    }
}
