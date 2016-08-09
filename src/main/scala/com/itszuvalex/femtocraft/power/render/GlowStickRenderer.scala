package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.power.tile.TileGlowStick
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer

/**
  * Created by Christopher Harris (Itszuvalex) on 1/29/2016.
  */
abstract class GlowStickRenderer extends TileEntitySpecialRenderer[TileGlowStick] {

  //  override def renderInventoryBlock(block: Block, metadata: Int, modelId: Int, renderer: RenderBlocks): Unit = {
  //    block match {
  //      case stick: BlockGlowStick =>
  //        if (model == null)
  //          makeGlowStickModel(stick)
  //
  //        Tessellator.instance.startDrawingQuads()
  //        Tessellator.instance.setColorOpaque_F(1, 1, 1)
  //        GL11.glPushMatrix()
  //        GL11.glTranslated(-.5, -.5, -.5)
  //        renderGlowStick(0, 0, 0, 0)
  //        GL11.glPopMatrix()
  //        Tessellator.instance.draw()
  //      case _ =>
  //    }
  //  }
  //
  //  override def renderWorldBlock(world: IBlockAccess, x: Int, y: Int, z: Int, block: Block, modelId: Int, renderer: RenderBlocks): Boolean = {
  //    block match {
  //      case stick: BlockGlowStick =>
  //        if (model == null)
  //          makeGlowStickModel(stick)
  //        world.getTileEntity(x, y, z) match {
  //          case t: TileGlowStick =>
  //            Tessellator.instance.setBrightness(block.getMixedBrightnessForBlock(renderer.blockAccess, x, y, z))
  //            Tessellator.instance.setColorOpaque_F(1, 1, 1)
  //            renderGlowStick(x, y, z, t.color)
  //          case _ =>
  //        }
  //        true
  //      case _ =>
  //        false
  //    }
  //  }


//  override def renderTileEntityAt(te: TileGlowStick, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int): Unit = {
//    val ccolor = new Color(te.color)
//    Tessellator.instance.setColorOpaque(0xff & ccolor.red.toInt, 0xff & ccolor.green.toInt, 0xff & ccolor.blue.toInt)
//    translationBlock(x, y, z) {
//                                drawBlock(DefaultVertexFormats.POSITION_TEX) {
//                                                                               val north = makeNorthFace(6f / 16f, 10f / 16f, 0, 1, 6f / 16f,
//                                                                                                         block.sideIcon,
//                                                                                                         block.sideIcon.getInterpolatedU(6f),
//                                                                                                         block.sideIcon.getInterpolatedU(10f),
//                                                                                                         block.sideIcon.getMinV,
//                                                                                                         block.sideIcon.getMaxV)
//
//                                                                               val west = north.rotatedOnYAxis(Math.PI / 2f, .5f, .5f)
//                                                                               val south = west.rotatedOnYAxis(Math.PI / 2f, .5f, .5f)
//                                                                               val east = south.rotatedOnYAxis(Math.PI / 2f, .5f, .5f)
//
//                                                                               val top = makeTopFace(6f / 16f, 10f / 16f, 6f / 16f, 10f / 16f, 1,
//                                                                                                     block.sideIcon,
//                                                                                                     block.sideIcon.getMinU,
//                                                                                                     block.sideIcon.getInterpolatedU(4),
//                                                                                                     block.sideIcon.getInterpolatedV(12),
//                                                                                                     block.sideIcon.getMaxV)
//                                                                               val bottom = top.rotatedOnXAxis(Math.PI, .5f, .5f)
//
//                                                                               model.addQuad(north)
//                                                                               model.addQuad(west)
//                                                                               model.addQuad(south)
//                                                                               model.addQuad(east)
//                                                                               model.addQuad(top)
//                                                                               model.addQuad(bottom)
//
//                                                                               val north_color = makeNorthFace(6f / 16f, 10f / 16f, 0, 1, 6f / 16f,
//                                                                                                               block.coloredIcon,
//                                                                                                               block.coloredIcon.getInterpolatedU(6f),
//                                                                                                               block.coloredIcon.getInterpolatedU(10f),
//                                                                                                               block.coloredIcon.getMinV,
//                                                                                                               block.coloredIcon.getMaxV)
//                                                                               val west_color = north_color.rotatedOnYAxis(Math.PI / 2f, .5f, .5f)
//                                                                               val south_color = west_color.rotatedOnYAxis(Math.PI / 2f, .5f, .5f)
//                                                                               val east_color = south_color.rotatedOnYAxis(Math.PI / 2f, .5f, .5f)
//                                                                               coloredModel.addQuad(north_color)
//                                                                               coloredModel.addQuad(west_color)
//                                                                               coloredModel.addQuad(south_color)
//                                                                               coloredModel.addQuad(east_color)
//                                                                             }
//                              }
//  }
}
