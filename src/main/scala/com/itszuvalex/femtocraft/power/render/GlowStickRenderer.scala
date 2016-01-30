package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.power.block.BlockGlowStick
import com.itszuvalex.femtocraft.power.tile.TileGlowStick
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.itszulib.render.{Point3D, RenderModel, RenderUtils}
import com.itszuvalex.itszulib.util.Color
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler
import net.minecraft.block.Block
import net.minecraft.client.renderer.{RenderBlocks, Tessellator}
import net.minecraft.world.IBlockAccess
import org.lwjgl.opengl.GL11

/**
  * Created by Christopher Harris (Itszuvalex) on 1/29/2016.
  */
class GlowStickRenderer extends ISimpleBlockRenderingHandler {
  var model       : RenderModel = null
  var coloredModel: RenderModel = null

  override def getRenderId = RenderIDs.glowStickID

  override def shouldRender3DInInventory(modelId: Int) = true

  override def renderInventoryBlock(block: Block, metadata: Int, modelId: Int, renderer: RenderBlocks): Unit = {
    block match {
      case stick: BlockGlowStick =>
        if (model == null)
          makeGlowStickModel(stick)

        Tessellator.instance.startDrawingQuads()
        Tessellator.instance.setColorOpaque_F(1, 1, 1)
        GL11.glPushMatrix()
        GL11.glTranslated(-.5, -.5, -.5)
        renderGlowStick(0, 0, 0, 0)
        GL11.glPopMatrix()
        Tessellator.instance.draw()
      case _ =>
    }
  }

  override def renderWorldBlock(world: IBlockAccess, x: Int, y: Int, z: Int, block: Block, modelId: Int, renderer: RenderBlocks): Boolean = {
    block match {
      case stick: BlockGlowStick =>
        if (model == null)
          makeGlowStickModel(stick)
        world.getTileEntity(x, y, z) match {
          case t: TileGlowStick =>
            Tessellator.instance.setBrightness(block.getMixedBrightnessForBlock(renderer.blockAccess, x, y, z))
            Tessellator.instance.setColorOpaque_F(1, 1, 1)
            renderGlowStick(x, y, z, t.color)
          case _ =>
        }
        true
      case _ =>
        false
    }
  }

  def makeGlowStickModel(block: BlockGlowStick): Unit = {
    model = new RenderModel(new Point3D(0, 0, 0), new Point3D(.5f, .5f, .5f))
    coloredModel = new RenderModel(new Point3D(0, 0, 0), new Point3D(.5f, .5f, .5f))

    val north = RenderUtils.makeNorthFace(6f / 16f, 10f / 16f, 0, 1, 6f / 16f,
                                          block.sideIcon,
                                          block.sideIcon.getInterpolatedU(6f),
                                          block.sideIcon.getInterpolatedU(10f),
                                          block.sideIcon.getMinV,
                                          block.sideIcon.getMaxV)

    val west = north.rotatedOnYAxis(Math.PI / 2f, .5f, .5f)
    val south = west.rotatedOnYAxis(Math.PI / 2f, .5f, .5f)
    val east = south.rotatedOnYAxis(Math.PI / 2f, .5f, .5f)

    val top = RenderUtils.makeTopFace(6f / 16f, 10f / 16f, 6f / 16f, 10f / 16f, 1,
                                      block.sideIcon,
                                      block.sideIcon.getMinU,
                                      block.sideIcon.getInterpolatedU(4),
                                      block.sideIcon.getInterpolatedV(12),
                                      block.sideIcon.getMaxV)
    val bottom = top.rotatedOnXAxis(Math.PI, .5f, .5f)

    model.addQuad(north)
    model.addQuad(west)
    model.addQuad(south)
    model.addQuad(east)
    model.addQuad(top)
    model.addQuad(bottom)

    val north_color = RenderUtils.makeNorthFace(6f / 16f, 10f / 16f, 0, 1, 6f / 16f,
                                                block.coloredIcon,
                                                block.coloredIcon.getInterpolatedU(6f),
                                                block.coloredIcon.getInterpolatedU(10f),
                                                block.coloredIcon.getMinV,
                                                block.coloredIcon.getMaxV)
    val west_color = north_color.rotatedOnYAxis(Math.PI / 2f, .5f, .5f)
    val south_color = west_color.rotatedOnYAxis(Math.PI / 2f, .5f, .5f)
    val east_color = south_color.rotatedOnYAxis(Math.PI / 2f, .5f, .5f)
    coloredModel.addQuad(north_color)
    coloredModel.addQuad(west_color)
    coloredModel.addQuad(south_color)
    coloredModel.addQuad(east_color)

  }

  def renderGlowStick(x: Int, y: Int, z: Int, color: Int): Unit = {
    model.location = new Point3D(x, y, z)
    coloredModel.location = new Point3D(x, y, z)
    model.draw()
    val ccolor = new Color(color)
    Tessellator.instance.setColorOpaque(0xff & ccolor.red.toInt, 0xff & ccolor.green.toInt, 0xff & ccolor.blue.toInt)
    coloredModel.draw()
  }
}
