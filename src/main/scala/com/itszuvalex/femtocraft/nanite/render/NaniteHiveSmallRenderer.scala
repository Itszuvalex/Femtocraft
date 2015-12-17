package com.itszuvalex.femtocraft.nanite.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.power.node.IPowerNode
import com.itszuvalex.femtocraft.power.render.DiffusionNodeBeamRenderer
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.itszulib.util.Color
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.MathHelper
import net.minecraft.world.IBlockAccess
import net.minecraftforge.client.model.AdvancedModelLoader
import net.minecraftforge.client.model.obj.WavefrontObject
import org.lwjgl.opengl.GL11

/**
  * Created by Christopher on 8/29/2015.
  */
object NaniteHiveSmallRenderer {
  val hiveModelLocation    = Resources.Model("nanite hive small/Nanite Hive Small.obj")
  val hiveTexLocation      = Resources.Model("nanite hive small/nanite hive small.png")
  val hiveColorTexLocation = Resources.Model("nanite hive small/nanite hive small color.png")
}

class NaniteHiveSmallRenderer extends TileEntitySpecialRenderer with ISimpleBlockRenderingHandler with DiffusionNodeBeamRenderer {
  val model = AdvancedModelLoader.loadModel(NaniteHiveSmallRenderer.hiveModelLocation).asInstanceOf[WavefrontObject]

  override def renderTileEntityAt(tile: TileEntity, x: Double, y: Double, z: Double, partialTime: Float): Unit =
    tile match {
      case node: IPowerNode =>
        GL11.glPushMatrix()
        GL11.glTranslated(x + .5, y, z + .5)
        preRender()
        model.renderPart("Box001")
        val time = node.getWorldObj.getTotalWorldTime.toFloat
        GL11.glRotatef(time + partialTime, 0, 1, 0)
        model.renderPart("Sphere001")
        Minecraft.getMinecraft.getTextureManager.bindTexture(NaniteHiveSmallRenderer.hiveColorTexLocation)
        val color = new Color(node.getColor)
        val shift = Math.abs(MathHelper.sin(time * .03f) * .5f) + .5f
        GL11.glColor4ub(((color.red.toInt & 255) * shift).toByte, ((color.green & 255) * shift).toByte, ((color.blue & 255) * shift).toByte, 255.toByte)
        model.renderPart("Sphere001")
        GL11.glPopMatrix()
        renderDiffuseBeams(node, x, y, z, partialTime)
      case _ =>
    }

  def preRender() = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(NaniteHiveSmallRenderer.hiveTexLocation)
  }

  override def getRenderId: Int = RenderIDs.naniteHiveSmallID

  override def shouldRender3DInInventory(modelId: Int): Boolean = true

  override def renderInventoryBlock(block: Block, metadata: Int, modelId: Int, renderer: RenderBlocks): Unit = {
    GL11.glPushMatrix()
    GL11.glTranslated(.5, 0, .5)
    GL11.glColor4f(1, 1, 1, 1)
    preRender()
    model.renderAll()
    Minecraft.getMinecraft.getTextureManager.bindTexture(NaniteHiveSmallRenderer.hiveColorTexLocation)
    GL11.glColor4ub(0, 0, 0, 255.toByte)
    model.renderPart("Sphere001")
    GL11.glPopMatrix()
  }

  override def renderWorldBlock(world: IBlockAccess, x: Int, y: Int, z: Int, block: Block, modelId: Int, renderer: RenderBlocks): Boolean = {
    false
  }
}
