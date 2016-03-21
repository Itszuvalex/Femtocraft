package com.itszuvalex.femtocraft.logistics.render

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.logistics.test.TileWorkerProviderTest
import com.itszuvalex.femtocraft.render.FemtoRenderUtils
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.render.Vector3
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{MathHelper, ResourceLocation}
import org.lwjgl.opengl.GL11

/**
  * Created by Christopher Harris (Itszuvalex) on 8/5/15.
  */
object WorkerProviderBeamRenderer {
  val BEAM_WIDTH    = .08f
  val RENDER_RADIUS = 64
  private val beamOuterLocation = new ResourceLocation(Femtocraft.ID + ":" + "textures/logistics_beam_outer.png")
  private val beamColorLocation = new ResourceLocation(Femtocraft.ID + ":" + "textures/logistics_beam_colored.png")
}


class WorkerProviderBeamRenderer extends TileEntitySpecialRenderer {

  override def renderTileEntityAt(tile: TileEntity, x: Double, y: Double, z: Double, partialTime: Float): Unit = {
    tile match {
      case node: TileWorkerProviderTest =>
        this.bindTexture(WorkerProviderBeamRenderer.beamOuterLocation)
        renderBeamsToAllChildren(x, y, z, partialTime, node, WorkerProviderBeamRenderer.BEAM_WIDTH, Color(160.toByte, 255.toByte, 255.toByte, 255.toByte))
        this.bindTexture(WorkerProviderBeamRenderer.beamColorLocation)
        renderBeamsToAllChildren(x, y, z, partialTime, node, WorkerProviderBeamRenderer.BEAM_WIDTH, Color(160.toByte, 255.toByte, 255.toByte, 255.toByte))
      case _ =>
    }
  }

  def renderBeamsToAllChildren(x: Double, y: Double, z: Double, partialTime: Float, node: TileWorkerProviderTest, beamWidth: Float, color: Color): Unit = {
    beamRenderSetup()
    node.getConnections.foreach { loc =>
      renderBeamToLocation(x, y, z, node, color, partialTime, loc, beamWidth)
                                }
    beamRenderTeardown()
  }

  def renderBeamToLocation(x: Double, y: Double, z: Double, node: TileWorkerProviderTest, color: Color, partialTime: Float, loc: Loc4, beamWidth: Float): Unit = {
    val f2: Float = node.getWorldObj.getTotalWorldTime.toFloat + partialTime
    val f3: Float = -f2 * 0.2F - MathHelper.floor_float(-f2 * 0.1F).toFloat
    val nloc = node.getProviderLocation
    val diff = Vector3(loc.x, loc.y, loc.z) - Vector3(nloc.x, nloc.y, nloc.z)
    val startLoc = Vector3(x, y, z)
    val offset = Vector3(0.5f, 0.5f, 0.5f)
    val xMin: Double = 0.0D
    val xMax: Double = 1.0D
    val yMin: Double = (-1.0F + f3).toDouble % 1
    val yMax: Double = diff.magnitude * (1 / (2 * beamWidth)) + yMin
    FemtoRenderUtils.drawBeam(startLoc + offset, startLoc + diff + offset, beamWidth,
                              xMin.toFloat, xMax.toFloat, yMin.toFloat, yMax.toFloat,
                              color.red.toInt & 255, color.green.toInt & 255, color.blue.toInt & 255, color.alpha.toInt & 255)
  }

  def beamRenderTeardown(): Unit = {
    GL11.glEnable(GL11.GL_LIGHTING)
    GL11.glEnable(GL11.GL_TEXTURE_2D)
    GL11.glDepthMask(true)
  }

  def beamRenderSetup(): Unit = {
    //    GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F)

    //        this.bindTexture(PowerNodeRenderer.beamLocation)
    GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0F)
    GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497.0F)
    GL11.glDisable(GL11.GL_LIGHTING)
    GL11.glDisable(GL11.GL_CULL_FACE)
    //        GL11.glDisable(GL11.GL_BLEND)
    GL11.glEnable(GL11.GL_BLEND)
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
    GL11.glDepthMask(true)
  }
}
