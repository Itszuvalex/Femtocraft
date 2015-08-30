package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.node.IPowerNode
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{MathHelper, ResourceLocation}
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.client.model.obj.WavefrontObject
import org.lwjgl.opengl.GL11

/**
 * Created by Christopher Harris (Itszuvalex) on 8/5/15.
 */
object NodeCrystalRenderer {
  val crystalModelLocation = new ResourceLocation(Femtocraft.ID + ":" + "models/crystal cluster/Crystals.obj")
  val crystalTexLocation   = new ResourceLocation(Femtocraft.ID + ":" + "models/crystal cluster/Crystals Texture 64x64.png")
}

trait NodeCrystalRenderer extends TileEntitySpecialRenderer {
  val crystalModel = AdvancedModelLoader.loadModel(NodeCrystalRenderer.crystalModelLocation).asInstanceOf[WavefrontObject]

  def renderNode(node: TileEntity with IPowerNode, x:Double, y:Double, z:Double, partialTime: Float) = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(NodeCrystalRenderer.crystalTexLocation)
    renderCrystal(x, y, z, node, partialTime)
  }

  def renderCrystal(x: Double, y: Double, z: Double, node: TileEntity with IPowerNode, partialTime: Float): Unit = {
    val tessellator = Tessellator.instance
    val color = new Color(node.getColor)
    GL11.glPushMatrix()
    GL11.glDisable(GL11.GL_LIGHTING)
    GL11.glDisable(GL11.GL_CULL_FACE)
    GL11.glTranslated(x + .5, y, z + .5)
    GL11.glScaled(.01, .01, .01)

    GL11.glColor4ub(color.red, color.green, color.blue, 220.toByte)

    val f2: Float = node.getWorldObj.getTotalWorldTime.toFloat + partialTime
    (1 to 10).map(num => ("Gengon0" + (if (num < 10) "0") + num, num)).foreach { name =>
      val offset = (name._2 * 97) % 10
      val dir = if (name._2 % 2 == 0) -1 else 1
      GL11.glPushMatrix()
      val height = MathHelper.sin((f2 + offset + x + y + z).toFloat * .1f) * 4f * dir
      GL11.glTranslated(0, height, 0)

      if (name._2 == 1) GL11.glRotated(f2 * name._2, 0, 1, 0)

      crystalModel.renderPart(name._1)
      GL11.glPopMatrix()
                                                                               }
    GL11.glEnable(GL11.GL_LIGHTING)
    GL11.glColor4f(1f, 1f, 1f, 1f)
    tessellator.setColorRGBA(255, 255, 255, 0)
    GL11.glPopMatrix()
  }
}
