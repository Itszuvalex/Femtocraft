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
import com.itszuvalex.femtocraft.render.FemtoRenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.Particle._
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.{Tessellator, VertexBuffer}
import net.minecraft.entity.Entity
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.{MathHelper, Vec3d}
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
  def this(par1World: World, x: Double, y: Double, z: Double, red: Float, green: Float, blue: Float) =
    this(par1World, x, y, z, 1.0F, red, green, blue)

  {
    this.motionX *= 0.10000000149011612D
    this.motionY *= 0.10000000149011612D
    this.motionZ *= 0.10000000149011612D
    this.particleTextureIndexX = 0
    this.particleTextureIndexY = 0
    val f4 = Math.random.toFloat * 0.2F + 0.8F
    this.particleRed = ((Math.random * 0.20000000298023224D).toFloat + 0.8F) * red * f4
    this.particleGreen = ((Math.random * 0.20000000298023224D).toFloat + 0.8F) * green * f4
    this.particleBlue = ((Math.random * 0.20000000298023224D).toFloat + 0.8F) * blue * f4
    this.particleScale *= 0.75F
    this.particleScale *= scale
    this.particleMaxAge = (8.0D / (Math.random * 0.8D + 0.2D)).toInt
    this.particleMaxAge = (this.particleMaxAge.toFloat * scale).toInt
  }

  override def getFXLayer = 3


  override def renderParticle(worldRendererIn: VertexBuffer, entityIn: Entity, partialTicks: Float, rotationX: Float, rotationZ: Float, rotationYZ: Float, rotationXY: Float, rotationXZ: Float): Unit = {
    val tessellator = Tessellator.getInstance()
    val vertexbuffer = tessellator.getBuffer
    vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
    Minecraft.getMinecraft.getTextureManager.bindTexture(EntityFxPower.particleLocation)
    FemtoRenderUtils.disableLightMaps()

    var f = this.particleTextureIndexX.toFloat / 16.0F
    var f1 = f + 0.0624375F
    var f2 = this.particleTextureIndexY.toFloat / 16.0F
    var f3 = f2 + 0.0624375F
    val f4 = 0.1F * this.particleScale
    if (this.particleTexture != null) {
      f = this.particleTexture.getMinU
      f1 = this.particleTexture.getMaxU
      f2 = this.particleTexture.getMinV
      f3 = this.particleTexture.getMaxV
    }
    val f5 = (this.prevPosX + (this.posX - this.prevPosX) * partialTicks.toDouble - interpPosX).toFloat
    val f6 = (this.prevPosY + (this.posY - this.prevPosY) * partialTicks.toDouble - interpPosY).toFloat
    val f7 = (this.prevPosZ + (this.posZ - this.prevPosZ) * partialTicks.toDouble - interpPosZ).toFloat
    val avec3d = Array[Vec3d](new Vec3d((-rotationX * f4 - rotationXY * f4).toDouble, (-rotationZ * f4).toDouble, (-rotationYZ * f4 - rotationXZ * f4).toDouble), new Vec3d((-rotationX * f4 + rotationXY * f4).toDouble, (rotationZ * f4).toDouble, (-rotationYZ * f4 + rotationXZ * f4).toDouble), new Vec3d((rotationX * f4 + rotationXY * f4).toDouble, (rotationZ * f4).toDouble, (rotationYZ * f4 + rotationXZ * f4).toDouble), new Vec3d((rotationX * f4 - rotationXY * f4).toDouble, (-rotationZ * f4).toDouble, (rotationYZ * f4 - rotationXZ * f4).toDouble))
    if (this.particleAngle != 0.0F) {
      val f8 = this.particleAngle + (this.particleAngle - this.prevParticleAngle) * partialTicks
      val f9 = MathHelper.cos(f8 * 0.5F)
      val f10 = MathHelper.sin(f8 * 0.5F) * cameraViewDir.xCoord.toFloat
      val f11 = MathHelper.sin(f8 * 0.5F) * cameraViewDir.yCoord.toFloat
      val f12 = MathHelper.sin(f8 * 0.5F) * cameraViewDir.zCoord.toFloat
      val vec3d = new Vec3d(f10.toDouble, f11.toDouble, f12.toDouble)
      var l = 0
      while (l < 4) {
        {
          avec3d(l) = vec3d.scale(2.0D * avec3d(l).dotProduct(vec3d)).add(avec3d(l).scale((f9 * f9).toDouble - vec3d.dotProduct(vec3d))).add(vec3d.crossProduct(avec3d(l)).scale((2.0F * f9).toDouble))
        }
        {l += 1; l}
      }
    }
    worldRendererIn.pos(f5.toDouble + avec3d(0).xCoord, f6.toDouble + avec3d(0).yCoord, f7.toDouble + avec3d(0).zCoord).tex(f1.toDouble, f3.toDouble).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).endVertex()
    worldRendererIn.pos(f5.toDouble + avec3d(1).xCoord, f6.toDouble + avec3d(1).yCoord, f7.toDouble + avec3d(1).zCoord).tex(f1.toDouble, f2.toDouble).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).endVertex()
    worldRendererIn.pos(f5.toDouble + avec3d(2).xCoord, f6.toDouble + avec3d(2).yCoord, f7.toDouble + avec3d(2).zCoord).tex(f.toDouble, f2.toDouble).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).endVertex()
    worldRendererIn.pos(f5.toDouble + avec3d(3).xCoord, f6.toDouble + avec3d(3).yCoord, f7.toDouble + avec3d(3).zCoord).tex(f.toDouble, f3.toDouble).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).endVertex()
    tessellator.draw()
    FemtoRenderUtils.enableLightMap(null)
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
    this.move(this.motionX, this.motionY, this.motionZ)
    if (this.posY == this.prevPosY) {
      this.motionX *= 1.1D
      this.motionZ *= 1.1D
    }
    this.motionX *= 0.9599999785423279D
    this.motionY *= 0.9599999785423279D
    this.motionZ *= 0.9599999785423279D
  }

  override def setParticleTextureIndex(par1: Int) {
    this.particleTextureIndexX = par1 % 16
    this.particleTextureIndexY = par1 / 16
  }
}
