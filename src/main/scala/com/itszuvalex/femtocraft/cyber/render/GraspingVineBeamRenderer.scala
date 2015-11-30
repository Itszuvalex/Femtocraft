package com.itszuvalex.femtocraft.cyber.render

import com.itszuvalex.femtocraft.cyber.tile.TileGraspingVines
import com.itszuvalex.femtocraft.render.FemtoRenderUtils
import com.itszuvalex.itszulib.render.Vector3
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.entity.Entity
import org.lwjgl.opengl.GL11

/**
  * Created by Christopher on 11/22/2015.
  */
trait GraspingVineBeamRenderer extends TileEntitySpecialRenderer {

  def renderBeamsToEntities(x: Double, y: Double, z: Double, partialTime: Float, node: TileGraspingVines, entities: Set[Entity], beamWidth: Float, color: Color): Unit = {
    beamRenderSetup()
    entities.foreach { entity =>
      renderBeamToEntity(x, y, z, node, entity, color, partialTime, beamWidth)
                     }
    beamRenderTeardown()
  }

  def renderBeamToEntity(x: Double, y: Double, z: Double, node: TileGraspingVines, entity: Entity, color: Color, partialTime: Float, beamWidth: Float): Unit = {
    //    val f2: Float = node.getWorldObj.getTotalWorldTime.toFloat + partialTime
    //    val f3: Float = -f2 * 0.2F - MathHelper.floor_float(-f2 * 0.1F).toFloat
    val px = (entity.prevPosX + (entity.posX - entity.prevPosX) * partialTime) //+ entity.width / 2f
    var py = (entity.prevPosY + (entity.posY - entity.prevPosY) * partialTime) + entity.height / 2f
    val pz = (entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTime) //+ entity.width / 2f

    if (entity == Minecraft.getMinecraft.thePlayer)
      py -= 1f

    val nloc = node.getLoc
    val diff = Vector3(px, py, pz) - Vector3(nloc.x, nloc.y, nloc.z)
    val startLoc = Vector3(x, y, z)
    val offset = Vector3(0.5f, 1f, 0.5f)
    val xMin: Double = 0.0D
    val xMax: Double = 1.0D
    val yMin: Double = 0.0D
    val yMax: Double = diff.magnitude * (1 / (2 * beamWidth)) + yMin
    FemtoRenderUtils.drawBeam(startLoc + offset, startLoc + diff, beamWidth,
                              xMin.toFloat, xMax.toFloat, yMin.toFloat, yMax.toFloat,
                              color.red.toInt & 255, color.green.toInt & 255, color.blue.toInt & 255, color.alpha.toInt & 255)
  }

  def beamRenderTeardown(): Unit = {
    GL11.glEnable(GL11.GL_LIGHTING)
    GL11.glEnable(GL11.GL_TEXTURE_2D)
    GL11.glEnable(GL11.GL_CULL_FACE)
    GL11.glDisable(GL11.GL_BLEND)
    GL11.glDepthMask(true)
  }

  def beamRenderSetup(): Unit = {
    GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0F)
    GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497.0F)
    GL11.glDisable(GL11.GL_LIGHTING)
    GL11.glDisable(GL11.GL_CULL_FACE)
    GL11.glEnable(GL11.GL_BLEND)
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
  }
}
