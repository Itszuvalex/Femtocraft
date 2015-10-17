package com.itszuvalex.femtocraft.cyber.particle

import net.minecraft.client.particle.EntityFX
import net.minecraft.world.World
import net.minecraftforge.fluids.FluidRegistry

class FXWaterSpray(world: World, x: Double, y: Double, z: Double, xVel: Double, yVel: Double, zVel: Double, gravity: Float, scale: Float) extends EntityFX(world, x, y, z, xVel, yVel, zVel) {

    particleGravity = gravity
    this.particleMaxAge = 50
    setSize(0.2f, 0.2f)
    this.particleScale = scale
    this.noClip = false

    setParticleIcon(FluidRegistry.WATER.getStillIcon)

  override def getFXLayer: Int = 1
}
