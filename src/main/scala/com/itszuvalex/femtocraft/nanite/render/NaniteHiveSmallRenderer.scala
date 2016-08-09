package com.itszuvalex.femtocraft.nanite.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.nanite.tile.TileNaniteHiveSmall
import com.itszuvalex.femtocraft.power.render.DiffusionNodeBeamRenderer
import com.itszuvalex.femtocraft.render.OBJDynamicRenderer._
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.VertexBuffer
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.util.math.MathHelper
import net.minecraftforge.client.model.ModelLoaderRegistry
import net.minecraftforge.client.model.obj.OBJModel
import org.lwjgl.opengl.GL11

/**
  * Created by Christopher on 8/29/2015.
  */
object NaniteHiveSmallRenderer {
  val hiveModelLocation    = Resources.Model("nanite hive small/Nanite Hive Small.obj")
  val hiveTexLocation      = Resources.Model("nanite hive small/nanite hive small.png")
  val hiveColorTexLocation = Resources.Model("nanite hive small/nanite hive small color.png")
}

class NaniteHiveSmallRenderer extends TileEntitySpecialRenderer[TileNaniteHiveSmall] {
  val model = ModelLoaderRegistry.getModelOrMissing(NaniteHiveSmallRenderer.hiveModelLocation).asInstanceOf[OBJModel]

  override def renderTileEntityFast(te: TileNaniteHiveSmall, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, buffer: VertexBuffer): Unit = {
    GL11.glPushMatrix()
    GL11.glTranslated(x + .5, y, z + .5)
    preRender()
    model.renderGroups(Set("Box001"))
    val time = te.getWorld.getTotalWorldTime.toFloat
    GL11.glRotatef(time + partialTicks, 0, 1, 0)
    model.renderGroups(Set("Sphere001"))
    Minecraft.getMinecraft.getTextureManager.bindTexture(NaniteHiveSmallRenderer.hiveColorTexLocation)
    val color = new Color(te.getColor)
    val shift = Math.abs(MathHelper.sin(time * .03f) * .5f) + .5f
    GL11.glColor4ub(((color.red.toInt & 255) * shift).toByte, ((color.green & 255) * shift).toByte, ((color.blue & 255) * shift).toByte, 255.toByte)
    model.renderGroups(Set("Sphere001"))
    GL11.glPopMatrix()
    DiffusionNodeBeamRenderer.renderDiffuseBeams(te, x, y, z, partialTicks)
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
