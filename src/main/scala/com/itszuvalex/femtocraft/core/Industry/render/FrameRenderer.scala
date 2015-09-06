package com.itszuvalex.femtocraft.core.Industry.render

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.core.Industry.tile.TileFrame
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.client.model.obj.WavefrontObject
import org.lwjgl.opengl.GL11

/**
 * Created by Alex on 05.09.2015.
 */
object FrameRenderer {
  val frameModelLocation = new ResourceLocation(Femtocraft.ID + ":" + "models/frame/Frame.obj")
  val frameTexLocation   = new ResourceLocation(Femtocraft.ID + ":" + "models/frame/frame.png")
}

class FrameRenderer extends TileEntitySpecialRenderer {
  val frameModel = AdvancedModelLoader.loadModel(FrameRenderer.frameModelLocation).asInstanceOf[WavefrontObject]
  val sidemap1   = Array("N", "E", "S", "W")
  val sidemap2   = Array("NW", "NE", "SE", "SW")

  override def renderTileEntityAt(tile: TileEntity, x: Double, y: Double, z: Double, partialTime: Float): Unit = {
    val frame = tile.asInstanceOf[TileFrame]
    val thingsToRender = frame.thingsToRender
    this.bindTexture(FrameRenderer.frameTexLocation)
    GL11.glPushMatrix()
    GL11.glDisable(GL11.GL_LIGHTING)
    GL11.glTranslated(x + .5, y, z + .5)
    GL11.glEnable(GL11.GL_CULL_FACE)
    GL11.glDisable(GL11.GL_BLEND)
    GL11.glColor4f(1f, 1f, 1f, 1f);
    {
      for {
        a <- 0 to 1
        b <- 0 to (2 - a)
        c <- 0 to 3
        if thingsToRender(a)(b)(c)
      } yield (a, b, c)
    }.foreach { case (a, b, c) =>
      frameModel.renderPart(
                             ((a, b, c) match {
                               case (_, 0, _) => "T"
                               case (0, 2, _) => "B"
                               case (1, 1, __) => "B"
                               case _ => ""
                             })
                             + (if (a == 0 && b != 1) sidemap1 else sidemap2)(c)
                           )
              }

    //
    //    for (i <- 0 to 3) {
    //      //      GL11.glPushMatrix()
    //      if (thingsToRender(0)(0)(i)) frameModel.renderPart("T" + sidemap1(i))
    //      //      GL11.glPopMatrix()
    //    }
    //    for (i <- 0 to 3) {
    //      //      GL11.glPushMatrix()
    //      if (thingsToRender(0)(1)(i)) frameModel.renderPart(sidemap2(i))
    //      //      GL11.glPopMatrix()
    //    }
    //    for (i <- 0 to 3) {
    //      //      GL11.glPushMatrix()
    //      if (thingsToRender(0)(2)(i)) frameModel.renderPart("B" + sidemap1(i))
    //      //      GL11.glPopMatrix()
    //    }
    //    for (i <- 0 to 3) {
    //      //      GL11.glPushMatrix()
    //      if (thingsToRender(1)(0)(i)) frameModel.renderPart("T" + sidemap2(i))
    //      //      GL11.glPopMatrix()
    //    }
    //    for (i <- 0 to 3) {
    //      //      GL11.glPushMatrix()
    //      if (thingsToRender(1)(1)(i)) frameModel.renderPart("B" + sidemap2(i))
    //      //      GL11.glPopMatrix()
    //    }

    GL11.glEnable(GL11.GL_BLEND)
    GL11.glEnable(GL11.GL_LIGHTING)
    GL11.glPopMatrix()

  }

}
