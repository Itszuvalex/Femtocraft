package com.itszuvalex.femtocraft.logistics.render

import com.itszuvalex.femtocraft.Femtocraft
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.client.model.obj.WavefrontObject
import org.lwjgl.opengl.GL11

/**
  * Created by Alex on 19.08.2015.
  */
object TestRenderer {
  val testModelLocation = new ResourceLocation(Femtocraft.ID + ":" + "")
  val testTexLocation   = new ResourceLocation(Femtocraft.ID + ":" + "")
}

class TestRenderer extends TileEntitySpecialRenderer {
  val testModel = AdvancedModelLoader.loadModel(TestRenderer.testModelLocation).asInstanceOf[WavefrontObject]

  override def renderTileEntityAt(tile: TileEntity, x: Double, y: Double, z: Double, partialTime: Float): Unit = {
    val tesselator = Tessellator.instance
    this.bindTexture(TestRenderer.testTexLocation)
    GL11.glPushMatrix()
    GL11.glDisable(GL11.GL_LIGHTING)
    GL11.glDisable(GL11.GL_CULL_FACE)
    GL11.glDisable(GL11.GL_BLEND)
    GL11.glTranslated(x + .5, y, z + .5)
    GL11.glColor4f(1f, 1f, 1f, 1f)

    // Toggleable comment block: first line: /* = commented, //* = uncommented
    //*start toggleable comment block
    GL11.glPushMatrix()
    testModel.renderAll()
    GL11.glPopMatrix()
    //end toggleable comment block*/

    GL11.glColor4f(1f, 1f, 1f, 1f)
    GL11.glEnable(GL11.GL_LIGHTING)
    GL11.glEnable(GL11.GL_BLEND)
    GL11.glPopMatrix()
  }

}
