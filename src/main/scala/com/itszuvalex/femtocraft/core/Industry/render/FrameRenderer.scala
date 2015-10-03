package com.itszuvalex.femtocraft.core.Industry.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.core.Industry.{FrameMultiblockRendererRegistry, FrameMultiblockRegistry}
import com.itszuvalex.femtocraft.core.Industry.tile.TileFrame
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.client.model.obj.WavefrontObject
import org.lwjgl.opengl.GL11

/**
 * Created by Alex on 05.09.2015.
 */
object FrameRenderer {
  val frameModelLocation = Resources.Model("frame/Frame.obj")
  val frameTexLocation   = Resources.Model("frame/frame.png")

  lazy val frameModel = AdvancedModelLoader.loadModel(FrameRenderer.frameModelLocation).asInstanceOf[WavefrontObject]

  val sidemap1 = Array("N", "E", "S", "W")
  val sidemap2 = Array("NW", "NE", "SE", "SW")

  def renderFrameAt(x: Double, y: Double, z: Double, partialTime: Float, marks: Set[(Int, Int, Int)]): Unit = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(frameTexLocation)
    GL11.glPushMatrix()
    //    GL11.glDisable(GL11.GL_LIGHTING)
    GL11.glTranslated(x + .5, y, z + .5)
    GL11.glEnable(GL11.GL_CULL_FACE)
    GL11.glDisable(GL11.GL_BLEND)
    GL11.glColor4f(1f, 1f, 1f, 1f)

    marks.foreach { case (a, b, c) =>
      frameModel.renderPart(
                             ((a, b, c) match {
                               case (_, 0, _) => "T"
                               case (0, 2, _) => "B"
                               case (1, 1, _) => "B"
                               case _         => ""
                             })
                             + (if (a == 0 && b != 1) sidemap1 else sidemap2)(c)
                           )
                  }

    GL11.glEnable(GL11.GL_BLEND)
    //    GL11.glEnable(GL11.GL_LIGHTING)
    GL11.glPopMatrix()
  }

}

class FrameRenderer extends TileEntitySpecialRenderer {

  override def renderTileEntityAt(tile: TileEntity, x: Double, y: Double, z: Double, partialTime: Float): Unit =
    tile match {
      case frame: TileFrame =>
        FrameRenderer.renderFrameAt(x, y, z, partialTime, {
                                                            for {
                                                              a <- 0 to 1
                                                              b <- 0 to (2 - a)
                                                              c <- 0 to 3
                                                              if frame.getRenderMark(a, b, c)
                                                            } yield (a, b, c)
                                                          }.toSet
                                   )
        if (frame.progress > 0 && frame.isController) {
          FrameMultiblockRegistry.getMultiblock(frame.multiBlock) match {
            case Some(mb) =>
              FrameMultiblockRendererRegistry.getRenderer(mb.multiblockRenderID) match {
                case Some(render) =>
                  render.renderInProgressAt(x, y, z, partialTime, frame)
                case _ =>
              }
            case _ =>
          }
        }

      case _ =>
    }


}
