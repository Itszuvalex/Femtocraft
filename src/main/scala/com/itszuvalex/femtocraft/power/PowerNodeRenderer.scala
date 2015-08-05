package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.power.node.IPowerNode
import com.itszuvalex.femtocraft.render.FemtoRender
import com.itszuvalex.itszulib.render.Vector3
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.client.renderer.{OpenGlHelper, Tessellator}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{MathHelper, ResourceLocation}
import org.lwjgl.opengl.GL11

/**
 * Created by Christopher Harris (Itszuvalex) on 8/4/15.
 */
object PowerNodeRenderer {
  private val beamLocation = new ResourceLocation("textures/entity/beacon_beam.png")
  val BEAM_WIDTH = .1f
  val RENDER_RADIUS = 64
}

class PowerNodeRenderer extends TileEntitySpecialRenderer {
  override def renderTileEntityAt(tile: TileEntity, x: Double, y: Double, z: Double, partialTime: Float): Unit = {
    tile match {
      case node: IPowerNode =>
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F)

        val tessellator: Tessellator = Tessellator.instance
        this.bindTexture(PowerNodeRenderer.beamLocation)
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0F)
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497.0F)
        GL11.glDisable(GL11.GL_LIGHTING)
        GL11.glDisable(GL11.GL_CULL_FACE)
        GL11.glDisable(GL11.GL_BLEND)
        GL11.glDepthMask(true)
        OpenGlHelper.glBlendFunc(770, 1, 1, 0)
        val f2: Float = tile.getWorldObj.getTotalWorldTime.toFloat + partialTime
        val f3: Float = -f2 * 0.2F - MathHelper.floor_float(-f2 * 0.1F).toFloat
        val b0: Byte = 1
        val d5: Double = b0.toDouble * 0.2D
        val xMin: Double = 0.0D
        val xMax: Double = 1.0D
        node.getChildrenLocs.foreach { loc =>
          val nloc = node.getNodeLoc
          val diff = Vector3(loc.x, loc.y, loc.z) - Vector3(nloc.x, nloc.y, nloc.z)
          val startLoc = Vector3(x, y, z)
          val offset = Vector3(0.5f, 0.5f, 0.5f)
          val yMin: Double = (-1.0F + f3).toDouble
          val yMax: Double = diff.magnitude * (0.5D / d5) + yMin
          FemtoRender.drawBeam(startLoc + offset, startLoc + diff + offset, PowerNodeRenderer.BEAM_WIDTH, xMin.toFloat, xMax.toFloat, yMin.toFloat, yMax.toFloat)
                                     }
        GL11.glEnable(GL11.GL_LIGHTING)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDepthMask(true)
      case _ =>
    }

  }
}
