package com.itszuvalex.femtocraft.industry.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.industry.tile.TileFrame
import com.itszuvalex.femtocraft.industry.{FrameMultiblockRegistry, FrameMultiblockRendererRegistry}
import com.itszuvalex.femtocraft.render.OBJDynamicRenderer._
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import org.lwjgl.opengl.GL11

/**
  * Created by Alex on 05.09.2015.
  */
object FrameRenderer {
  lazy val frameModel = LoadObj(FrameRenderer.frameModelLocation)
  val frameModelLocation = Resources.CustomModelBlock("frame/frame.obj")
  val frameTexLocation   = Resources.CustomModelBlockTex("frame/frame.png")
  val sidemap1           = Array("N", "E", "S", "W")
  val sidemap2           = Array("NW", "NE", "SE", "SW")

  def renderFrameAt(x: Double, y: Double, z: Double, partialTime: Float, marks: Set[(Int, Int, Int)]): Unit = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(frameTexLocation)
    GL11.glPushMatrix()
    //    GL11.glDisable(GL11.GL_LIGHTING)
    GL11.glTranslated(x + .5, y, z + .5)
    GL11.glEnable(GL11.GL_CULL_FACE)
    GL11.glDisable(GL11.GL_BLEND)
    GL11.glColor4f(1f, 1f, 1f, 1f)

    marks.foreach { case (a, b, c) =>
      frameModel.renderGroups(Set(
        ((a, b, c) match {
          case (_, 0, _) => "T"
          case (0, 2, _) => "B"
          case (1, 1, _) => "B"
          case _ => ""
        })
          + (if (a == 0 && b != 1) sidemap1 else sidemap2) (c)
      ), bindTextures = false)
    }

    GL11.glEnable(GL11.GL_BLEND)
    //    GL11.glEnable(GL11.GL_LIGHTING)
    GL11.glPopMatrix()
  }

}

class FrameRenderer extends TileEntitySpecialRenderer[TileFrame] {

  override def renderTileEntityAt(te: TileFrame, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int): Unit = {
    FrameRenderer.renderFrameAt(x, y, z, partialTicks, {
      for {
        a <- 0 to 1
        b <- 0 to (2 - a)
        c <- 0 to 3
        if te.getRenderMark(a, b, c)
      } yield (a, b, c)
    }.toSet
    )
    if (te.renderProgress > 0 && te.isController) {
      FrameMultiblockRegistry.getMultiblock(te.multiBlock) match {
        case Some(mb) =>
          FrameMultiblockRendererRegistry.getRenderer(mb.multiblockRenderID) match {
            case Some(render) =>
              render.renderInProgressAt(x, y, z, partialTicks, te)
            case _ =>
          }
        case _ =>
      }
    }
  }


}
