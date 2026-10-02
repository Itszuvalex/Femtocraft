package com.itszuvalex.femtocraft.client

import net.minecraft.core.Direction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class SideConfigGeometryTest {
    private val scale = 30

    @Test
    fun Pick_Unrotated_CentreIsSouthFace() {
        // Unrotated, +z (south) faces the viewer.
        assertEquals(Direction.SOUTH, SideConfigGeometry.pick(SideConfigGeometry.rotation(0f, 0f), 0.0, 0.0, scale, emptySet()))
    }

    @Test
    fun Pick_PitchedDown_UpperPointIsUpFace() {
        val rotation = SideConfigGeometry.rotation(0f, 0.6f)
        // Screen y grows downwards, so the top face is above the centre.
        assertEquals(Direction.UP, SideConfigGeometry.pick(rotation, 0.0, -0.55 * scale, scale, emptySet()))
        assertEquals(Direction.SOUTH, SideConfigGeometry.pick(rotation, 0.0, 0.3 * scale, scale, emptySet()))
    }

    @Test
    fun Pick_OutsideEverything_IsNull() {
        assertNull(SideConfigGeometry.pick(SideConfigGeometry.rotation(0f, 0f), 2.0 * scale, 0.0, scale, emptySet()))
    }

    @Test
    fun Pick_Neighbour_SelectsItsSide() {
        val rotation = SideConfigGeometry.rotation(0f, 0f)
        val east = SideConfigGeometry.NEIGHBOUR_DISTANCE * scale
        assertNull(SideConfigGeometry.pick(rotation, east.toDouble(), 0.0, scale, emptySet()))
        assertEquals(Direction.EAST, SideConfigGeometry.pick(rotation, east.toDouble(), 0.0, scale, setOf(Direction.EAST)))
    }

    @Test
    fun Pick_NeighbourInFront_HidesMachineFace() {
        // The south neighbour sits between the viewer and the machine's south face.
        assertEquals(Direction.SOUTH, SideConfigGeometry.pick(SideConfigGeometry.rotation(0f, 0f), 0.0, 0.0, scale, setOf(Direction.SOUTH)))
        assertEquals(Direction.SOUTH, SideConfigGeometry.pick(SideConfigGeometry.rotation(0f, 0f), 0.4 * scale, 0.0, scale, setOf(Direction.SOUTH)))
    }

    @Test
    fun DefaultYaw_TurnsFrontTowardsViewer() {
        for (front in Direction.Plane.HORIZONTAL) {
            val rotation = SideConfigGeometry.rotation(SideConfigGeometry.defaultYaw(front), 0f)
            assertEquals(front, SideConfigGeometry.pick(rotation, 0.0, 0.0, scale, emptySet()), "front $front")
        }
    }

    @Test
    fun FaceCorners_AllFaces_CounterClockwiseFromOutside() {
        for (face in Direction.entries) {
            val c = SideConfigGeometry.faceCorners(face)
            val normal = org.joml.Vector3f(c[1]).sub(c[0]).cross(org.joml.Vector3f(c[2]).sub(c[1]))
            val outward = org.joml.Vector3f(face.stepX.toFloat(), face.stepY.toFloat(), face.stepZ.toFloat())
            org.junit.jupiter.api.Assertions.assertTrue(normal.dot(outward) > 0, "face $face")
        }
    }
}
