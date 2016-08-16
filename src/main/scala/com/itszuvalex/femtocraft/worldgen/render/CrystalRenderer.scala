package com.itszuvalex.femtocraft.worldgen.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.render.OBJDynamicRenderer._
import com.itszuvalex.femtocraft.worldgen.block.TileCrystalsWorldgen
import com.itszuvalex.itszulib.render.RenderUtils._
import com.itszuvalex.itszulib.render.TileEntityCombinedRenderer
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.util.math.MathHelper
import org.lwjgl.opengl.GL11

/**
  * Created by Christopher Harris (Itszuvalex) on 8/5/15.
  */
object CrystalRenderer {
  val crystalModelLocation = Resources.CustomModelBlock("crystal cluster/Crystals.obj")
  val crystalTexLocation   = Resources.CustomModelBlockTex("crystal cluster/Crystals Texture 64x64.png")
}

class CrystalRenderer extends TileEntityCombinedRenderer[TileCrystalsWorldgen] {
  val crystalModel = LoadObj(CrystalRenderer.crystalModelLocation)


  override def renderTileEntityAsItem(x: Double, y: Double, z: Double, partialTicks: Float): Unit = {
    super.renderTileEntityAsItem(x, y, z, partialTicks)
    renderCrystalAt(x, y, z, Minecraft.getMinecraft.getRenderPartialTicks, Option(Minecraft.getMinecraft.theWorld).map(_.getTotalWorldTime.toFloat).getOrElse(0f), Color(0, 255.toByte, 255.toByte, 255.toByte), Array.fill(11)(Color(255.toByte, 0, 0, 0).toInt))
  }

  override def renderTileEntityInWorld(te: TileCrystalsWorldgen, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int): Unit = {
    super.renderTileEntityInWorld(te, x, y, z, partialTicks, destroyStage)
    renderCrystalAt(x, y, z, partialTicks, te.getWorld.getTotalWorldTime.toFloat, new Color(te.color), te.colorOffsets)
  }

  def renderCrystalAt(x: Double, y: Double, z: Double, partialTicks: Float, time: Float, color: Color, colorOffsets: Array[Int]): Unit = {
    this.bindTexture(CrystalRenderer.crystalTexLocation)
    GL11.glDisable(GL11.GL_CULL_FACE)
    GL11.glDisable(GL11.GL_LIGHTING)
    GL11.glPushMatrix()

    translationBlock(x + .5, y, z + .5) {
      GL11.glScaled(.01, .01, .01)

      val f2: Float = time + partialTicks
      (1 to 10).map(num => ("Gengon0" + (if (num < 10) "0") + num, num)).foreach { name =>
        val offset = (name._2 * 97) % 10
        val dir = if (name._2 % 2 == 0) -1 else 1
        val height = MathHelper.sin((f2 + offset + x + y + z).toFloat * .1f) * 4f * dir
        translationBlock(0, height, 0) {

          if (name._2 == 1) GL11.glRotated(f2 * name._2, 0, 1, 0)

          val colorOffset: Color = new Color(colorOffsets(name._2))

          GL11.glColor4ub((color.red + colorOffset.red - 15).toByte, (color.green + colorOffset.green - 15).toByte, (color.blue + colorOffset.blue - 15).toByte, colorOffset.alpha)
          crystalModel.renderGroups(Set(name._1), bindTextures = false)
        }
      }
    }
    GL11.glPopMatrix() // Stop leaking scaling change, idiot!
    GL11.glColor4f(1f, 1f, 1f, 1f)
    GL11.glEnable(GL11.GL_LIGHTING)
    GL11.glEnable(GL11.GL_CULL_FACE)
  }
}
