package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.node.IPowerNode
import com.itszuvalex.femtocraft.render.FemtoRender
import com.itszuvalex.itszulib.render.Vector3
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.client.renderer.{OpenGlHelper, Tessellator}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{MathHelper, ResourceLocation}
import net.minecraftforge.client.model.AdvancedModelLoader
import org.lwjgl.opengl.GL11

/**
 * Created by Christopher Harris (Itszuvalex) on 8/4/15.
 */
object PowerNodeRenderer {
  private val crystalModelLocation = new ResourceLocation(Femtocraft.ID + ":" + "models/crystal cluster/Crystals.obj")
  private val crystalTexLocation   = new ResourceLocation(Femtocraft.ID + ":" + "models/crystal cluster/Crystals Texture 64x64.png")
  private val beamLocation         = new ResourceLocation(Femtocraft.ID + ":" + "textures/power_beam.png")
  private val beamOuterLocation    = new ResourceLocation(Femtocraft.ID + ":" + "textures/power_beam_outer.png")
  private val beamColorLocation    = new ResourceLocation(Femtocraft.ID + ":" + "textures/power_beam_colored.png")
  val BEAM_WIDTH    = .1f
  val RENDER_RADIUS = 64
}

class PowerNodeRenderer extends TileEntitySpecialRenderer {
  private val crystalModel = AdvancedModelLoader.loadModel(PowerNodeRenderer.crystalModelLocation)

  override def renderTileEntityAt(tile: TileEntity, x: Double, y: Double, z: Double, partialTime: Float): Unit = {
    tile match {
      case node: IPowerNode =>
        val color = new Color(node.getColor)
        val tessellator: Tessellator = Tessellator.instance

        GL11.glPushMatrix()
        GL11.glTranslated(x + .5, y, z + .5)
        GL11.glScaled(.01,.01,.01)
        tessellator.setColorRGBA(color.red.toInt & 255,
                                 color.green.toInt & 255,
                                 color.blue.toInt & 255,
                                 0)

        //Bind the texture and render the model
        bindTexture(PowerNodeRenderer.crystalTexLocation)
        crystalModel.renderAll()

        GL11.glPopMatrix()

        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F)

        //        this.bindTexture(PowerNodeRenderer.beamLocation)
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
          val yMin: Double = (-1.0F + f3).toDouble % 1
          val yMax: Double = diff.magnitude * (1 / (2 * PowerNodeRenderer.BEAM_WIDTH)) + yMin
          this.bindTexture(PowerNodeRenderer.beamOuterLocation)
          FemtoRender.drawBeam(startLoc + offset, startLoc + diff + offset, PowerNodeRenderer.BEAM_WIDTH,
                               xMin.toFloat, xMax.toFloat, yMin.toFloat, yMax.toFloat)
          this.bindTexture(PowerNodeRenderer.beamColorLocation)
          FemtoRender.drawBeam(startLoc + offset, startLoc + diff + offset, PowerNodeRenderer.BEAM_WIDTH,
                               xMin.toFloat, xMax.toFloat, yMin.toFloat, yMax.toFloat,
                               color.red.toInt & 255, color.green.toInt & 255, color.blue.toInt & 255)
                                     }
        GL11.glEnable(GL11.GL_LIGHTING)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glDepthMask(true)
      case _ =>
    }

  }
}
