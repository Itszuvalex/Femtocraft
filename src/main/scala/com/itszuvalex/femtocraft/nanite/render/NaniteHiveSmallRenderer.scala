package com.itszuvalex.femtocraft.nanite.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.nanite.tile.TileNaniteHiveSmall
import com.itszuvalex.femtocraft.power.render.DiffusionNodeBeamRenderer
import com.itszuvalex.femtocraft.render.OBJDynamicRenderer._
import com.itszuvalex.itszulib.render.TileEntityCombinedRenderer
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.util.math.MathHelper
import org.lwjgl.opengl.GL11

/**
  * Created by Christopher on 8/29/2015.
  */
object NaniteHiveSmallRenderer {
  val hiveModelLocation    = Resources.CustomModelBlock("nanite hive small/Nanite Hive Small.obj")
  val hiveTexLocation      = Resources.CustomModelBlockTex("nanite hive small/nanite hive small.png")
  val hiveColorTexLocation = Resources.CustomModelBlockTex("nanite hive small/nanite hive small color.png")
}

class NaniteHiveSmallRenderer extends TileEntityCombinedRenderer[TileNaniteHiveSmall] {
  val model = LoadObj(NaniteHiveSmallRenderer.hiveModelLocation)

  override def renderTileEntityAsItem(x: Double, y: Double, z: Double, partialTicks: Float): Unit = {
    super.renderTileEntityAsItem(x, y, z, partialTicks)
    renderAtLocWithColor(x, y, z, Minecraft.getMinecraft.getRenderPartialTicks, 0, Option(Minecraft.getMinecraft.theWorld).map(_.getTotalWorldTime.toFloat).getOrElse(0f), Color(0, 255.toByte, 255.toByte, 255.toByte))
  }

  override def renderTileEntityInWorld(te: TileNaniteHiveSmall, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int): Unit = {
    super.renderTileEntityInWorld(te, x, y, z, partialTicks, destroyStage)
    renderAtLocWithColor(x, y, z, partialTicks, destroyStage, te.getWorld.getTotalWorldTime.toFloat, new Color(te.getColor))
    DiffusionNodeBeamRenderer.renderDiffuseBeams(te, x, y, z, partialTicks)
  }

  private def renderAtLocWithColor(x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, time: Float, color: Color): Unit = {
    GL11.glPushMatrix()
    GL11.glColor4f(1f, 1f, 1f, 1f)
    GL11.glTranslated(x + .5, y, z + .5)
    preRender()
    model.renderGroups(Set("Box001"))
    GL11.glRotatef(time + partialTicks, 0, 1, 0)
    model.renderGroups(Set("Sphere001"))
    Minecraft.getMinecraft.getTextureManager.bindTexture(NaniteHiveSmallRenderer.hiveColorTexLocation)
    val shift = Math.abs(MathHelper.sin(time * .03f) * .5f) + .5f
    GL11.glColor4ub(((color.red.toInt & 255) * shift).toByte, ((color.green & 255) * shift).toByte, ((color.blue & 255) * shift).toByte, 255.toByte)
    model.renderGroups(Set("Sphere001"))
    GL11.glPopMatrix()
  }

  def preRender() = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(NaniteHiveSmallRenderer.hiveTexLocation)
  }

  //  override def renderInventoryBlock(block: Block, metadata: Int, modelId: Int, renderer: RenderBlocks): Unit = {
  //    GL11.glPushMatrix()
  //    GL11.glTranslated(.5, -.1, .5)
  //    GL11.glColor4f(1, 1, 1, 1)
  //    preRender()
  //    model.renderAll()
  //    Minecraft.getMinecraft.getTextureManager.bindTexture(NaniteHiveSmallRenderer.hiveColorTexLocation)
  //    GL11.glColor4ub(0, 0, 0, 255.toByte)
  //    model.renderPart("Sphere001")
  //    GL11.glPopMatrix()
  //  }

  //  override def renderWorldBlock(world: IBlockAccess, x: Int, y: Int, z: Int, block: Block, modelId: Int, renderer: RenderBlocks): Boolean = {
  //    false
  //  }
}
