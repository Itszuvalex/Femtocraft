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
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.{BufferBuilder, Tessellator}
import net.minecraft.entity.Entity
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.{Side, SideOnly}


@SideOnly(Side.CLIENT)
object EntityFxNanites {
  val particleLocation = new ResourceLocation(Femtocraft.ID.toLowerCase, "textures/particles/particles.png")
}

@SideOnly(Side.CLIENT)
class EntityFxNanites(args: ParticleArgs) extends
  BaseEntityFx(args) {
  loadFromArgs(args)

  def this(par1World: World, x: Double, y: Double, z: Double, _scale: Float, _red: Float, _green: Float, _blue: Float, velX: Double, velY: Double, velZ: Double) =
    this(new ParticleArgs(
      dimension = par1World.provider.getDimension,
      posX = x,
      posY = y,
      posZ = z,
      scale = _scale,
      red = _red,
      green = _green,
      blue = _blue,
      motionX = velX.toFloat,
      motionY = velY.toFloat,
      motionZ = velZ.toFloat
    )
    )

  def this(par1World: World, x: Double, y: Double, z: Double, red: Float, green: Float, blue: Float, velX: Double, velY: Double, velZ: Double) =
    this(par1World, x, y, z, 1.0F, red, green, blue, velX, velY, velZ)

  def loadFromArgs(args: ParticleArgs): Unit = {
    this.motionX = args.motionX
    this.motionY = args.motionY
    this.motionZ = args.motionZ
    this.particleTextureIndexX = 0
    this.particleTextureIndexY = 1
    val f4 = Math.random.toFloat * 0.4F + 0.6F
    this.particleRed = ((Math.random * 0.20000000298023224D).toFloat + 0.8F) * args.red * f4
    this.particleGreen = ((Math.random * 0.20000000298023224D).toFloat + 0.8F) * args.green * f4
    this.particleBlue = ((Math.random * 0.20000000298023224D).toFloat + 0.8F) * args.blue * f4
    this.particleMaxAge = 60
  }

  override def getFXLayer = 3


  override def renderParticle(worldRendererIn: BufferBuilder, entityIn: Entity, partialTicks: Float, rotationX: Float, rotationZ: Float, rotationYZ: Float, rotationXY: Float, rotationXZ: Float): Unit = {
    val tessellator = Tessellator.getInstance()
    val vertexbuffer = tessellator.getBuffer
    vertexbuffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP)
    Minecraft.getMinecraft.getTextureManager.bindTexture(EntityFxNanites.particleLocation)
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

    if (this.particleAge < 20) {
      this.particleScale = particleAge.toFloat / 20f
    }
    else if ((this.particleMaxAge - this.particleAge) < 20) {
      this.particleScale = (this.particleMaxAge.toFloat - this.particleAge.toFloat) / 20f
    }
    else {
      this.particleScale = 1f
    }


    this.setParticleTextureIndex((this.particleAge * 16 / this.particleMaxAge) % 8)
    this.move(this.motionX, this.motionY, this.motionZ)
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
    this.particleTextureIndexY = 1 + par1 / 16
  }

}
