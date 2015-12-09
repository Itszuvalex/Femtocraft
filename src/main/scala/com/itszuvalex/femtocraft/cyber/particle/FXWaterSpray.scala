package com.itszuvalex.femtocraft.cyber.particle

import net.minecraft.client.particle.EntityFX
import net.minecraft.world.World
import net.minecraftforge.fluids.FluidRegistry

class FXWaterSpray(world: World, x: Double, y: Double, z: Double, xVel: Double, yVel: Double, zVel: Double, gravity: Float, scale: Float, yToStopAt: Double = 0) extends EntityFX(world, x, y, z, xVel, yVel, zVel) {

  particleGravity = gravity
  this.particleMaxAge = 30
  setSize(0.2f, 0.2f)
  this.particleScale = scale
  this.noClip = true

  this.setVelocity(xVel, yVel, zVel)

  setParticleIcon(FluidRegistry.WATER.getStillIcon)

  override def getFXLayer: Int = 1

  override def moveEntity(x: Double, y: Double, z: Double): Unit = {
    super.moveEntity(x, y, z)
    if (posY <= yToStopAt) {
      setPosition(posX, yToStopAt, posZ)
      setVelocity(0, 0, 0)
    }
  }
}
