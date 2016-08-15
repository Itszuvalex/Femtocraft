/*
 * ******************************************************************************
 *  * Copyright (C) 2013  Christopher Harris (Itszuvalex)
 *  * Itszuvalex@gmail.com
 *  *
 *  * This program is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU General Public License
 *  * as published by the Free Software Foundation; either version 2
 *  * of the License, or (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *  *****************************************************************************
 */
package com.itszuvalex.femtocraft.particles

import com.itszuvalex.femtocraft.Femtocraft
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.Particle
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.{Tessellator, VertexBuffer}
import net.minecraft.entity.Entity
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

/**
  * Created by Christopher Harris (Itszuvalex) on 9/25/14.
  */

@SideOnly(Side.CLIENT)
object EntityFxPower {
  val particleLocation = new ResourceLocation(Femtocraft.ID.toLowerCase, "textures/particles/particles.png")
}

@SideOnly(Side.CLIENT)
class EntityFxPower(par1World: World, x: Double, y: Double, z: Double, scale: Float, red: Float, green: Float, blue: Float) extends Particle(par1World, x, y, z, 0.0D, 0.0D, 0.0D) {
  private var powerParticleScale = 0f

  def this(par1World: World, x: Double, y: Double, z: Double, red: Float, green: Float, blue: Float) =
    this(par1World, x, y, z, 1.0F, red, green, blue)

  {
    this.motionX *= 0.10000000149011612D
    this.motionY *= 0.10000000149011612D
    this.motionZ *= 0.10000000149011612D
    this.particleTextureIndexX = 0
    this.particleTextureIndexY = 0
    val f4 = Math.random.toFloat * 0.4F + 0.6F
    this.particleRed = ((Math.random * 0.20000000298023224D).toFloat + 0.8F) * red * f4
    this.particleGreen = ((Math.random * 0.20000000298023224D).toFloat + 0.8F) * green * f4
    this.particleBlue = ((Math.random * 0.20000000298023224D).toFloat + 0.8F) * blue * f4
    this.particleScale *= 0.75F
    this.particleScale *= scale
    this.powerParticleScale = this.particleScale
    this.particleMaxAge = (8.0D / (Math.random * 0.8D + 0.2D)).toInt
    this.particleMaxAge = (this.particleMaxAge.toFloat * scale).toInt
  }

  override def getFXLayer = 3


  override def renderParticle(worldRendererIn: VertexBuffer, entityIn: Entity, partialTicks: Float, rotationX: Float, rotationZ: Float, rotationYZ: Float, rotationXY: Float, rotationXZ: Float): Unit = {
    val tessellator = Tessellator.getInstance()
    val vertexbuffer = tessellator.getBuffer
    vertexbuffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP)
    Minecraft.getMinecraft.getTextureManager.bindTexture(EntityFxPower.particleLocation)
    super.renderParticle(worldRendererIn, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ)
    tessellator.draw()
  }

  /**
    * Called to update the entity's position/logic.
    */
  override def onUpdate() {
    this.prevPosX = this.posX
    this.prevPosY = this.posY
    this.prevPosZ = this.posZ
    if ( {
      this.particleAge += 1
      this.particleAge - 1
    } >= this.particleMaxAge) {
      setExpired()
    }
    this.setParticleTextureIndex(this.particleAge * 8 / this.particleMaxAge)
    this.moveEntity(this.motionX, this.motionY, this.motionZ)
    if (this.posY == this.prevPosY) {
      this.motionX *= 1.1D
      this.motionZ *= 1.1D
    }
    this.motionX *= 0.9599999785423279D
    this.motionY *= 0.9599999785423279D
    this.motionZ *= 0.9599999785423279D
    //    if (this.onGround) {
    //      this.motionX *= 0.699999988079071D
    //      this.motionZ *= 0.699999988079071D
    //    }
  }

  override def setParticleTextureIndex(par1: Int) {
    this.particleTextureIndexX = par1 % 16
    this.particleTextureIndexY = par1 / 16
  }
}
