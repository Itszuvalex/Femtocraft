package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.render.OBJDynamicRenderer._
import com.itszuvalex.itszulib.api.ItszuLibModules
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.MathHelper
import org.lwjgl.opengl.GL11

/**
  * Created by Christopher Harris (Itszuvalex) on 8/5/15.
  */
object NodeCrystalRenderer {
  val crystalModelLocation = Resources.CustomModelBlock("crystal cluster/crystals.obj")
  val crystalTexLocation   = Resources.CustomModelBlockTex("crystal cluster/crystals texture 64x64.png")
}

trait NodeCrystalRenderer[T <: TileEntity with ITileEntity] extends TileEntitySpecialRenderer[T] {
  val crystalModel = LoadObj(NodeCrystalRenderer.crystalModelLocation)

  def renderNode(node: ITileEntity, x: Double, y: Double, z: Double, partialTime: Float) = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(NodeCrystalRenderer.crystalTexLocation)
    renderCrystal(x, y, z, node, partialTime)
  }

  def renderCrystal(x: Double, y: Double, z: Double, node: ITileEntity, partialTime: Float): Unit = {
    val color = node.getModule(ItszuLibModules.COLORABLE, EnumFacing.UP)
    GL11.glPushMatrix()
    GL11.glDisable(GL11.GL_CULL_FACE)
    GL11.glTranslated(x + .5, y, z + .5)
    GL11.glScaled(.01, .01, .01)

    GL11.glColor4ub(color.red, color.green, color.blue, 220.toByte)

    val f2: Float = node.getIWorld.toMinecraft.getTotalWorldTime.toFloat + partialTime // TODO
    (1 to 10).map(num => ("Gengon0" + (if (num < 10) "0") + num, num)).foreach { name =>
      val offset = (name._2 * 97) % 10
      val dir    = if (name._2 % 2 == 0) -1 else 1
      GL11.glPushMatrix()
      val height = MathHelper.sin((f2 + offset + x + y + z).toFloat * .1f) * 4f * dir
      GL11.glTranslated(0, height, 0)

      if (name._2 == 1) GL11.glRotated(f2 * name._2, 0, 1, 0)

      crystalModel.renderGroups(Set(name._1))
      GL11.glPopMatrix()
    }
    GL11.glPopMatrix()
  }
}
