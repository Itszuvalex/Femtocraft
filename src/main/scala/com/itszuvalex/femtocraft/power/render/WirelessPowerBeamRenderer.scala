package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.render.FemtoRenderUtils
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.render.Vector3
import com.itszuvalex.itszulib.util.Color
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.MathHelper
import org.lwjgl.opengl.GL11

/**
  * Created by Christopher Harris (Itszuvalex) on 8/5/15.
  */

object WirelessPowerBeamRenderer {

  def renderBeamToChild(x: Double, y: Double, z: Double, partialTime: Float, node: ITileEntity, beamWidth: Float, color: Color, child: Loc4): Unit = {
    beamRenderSetup()
    renderBeamToLocation(x, y, z, node, color, partialTime, child, beamWidth)
    beamRenderTeardown()
  }

  def renderBeamToLocation(x: Double, y: Double, z: Double, node: ITileEntity, color: Color, partialTime: Float, loc: Loc4, beamWidth: Float, reverse: Boolean = false): Unit = {
    val f2  : Float  = node.getIWorld.toMinecraft.getTotalWorldTime.toFloat + partialTime
    val f3  : Float  = -f2 * 0.2F - MathHelper.floor(-f2 * 0.1F).toFloat
    val nloc         = Loc4(node)
    val extraOffset  = loc.getITileEntity(false) match {
      case Some(t: TileBeamRenderOffset) => t.offset
      case _ => Vector3(0, 0, 0)
    }
    val diff         = Vector3(loc.x, loc.y, loc.z) - Vector3(nloc.x, nloc.y, nloc.z)
    val startLoc     = Vector3(x, y, z)
    val offset       = Vector3(0.5f, 0.5f, 0.5f)
    val xMin: Double = 0.0D
    val xMax: Double = 1.0D
    val yMin: Double = (-1.0F + f3).toDouble % 1
    val yMax: Double = diff.magnitude * (1 / (2 * beamWidth)) + yMin
    FemtoRenderUtils.drawBeam(startLoc + offset, startLoc + diff + offset + extraOffset, beamWidth,
                              xMin.toFloat, xMax.toFloat, yMin.toFloat, yMax.toFloat,
                              color.red.toInt & 255, color.green.toInt & 255, color.blue.toInt & 255, color.alpha.toInt & 255)
  }

  def beamRenderTeardown(): Unit = {
    GL11.glEnable(GL11.GL_LIGHTING)
    GL11.glEnable(GL11.GL_TEXTURE_2D)
    GL11.glEnable(GL11.GL_CULL_FACE)
    GL11.glDisable(GL11.GL_BLEND)
    GL11.glDepthMask(true)
  }

  def beamRenderSetup(): Unit = {
    GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0F)
    GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497.0F)
    GL11.glDisable(GL11.GL_LIGHTING)
    GL11.glDisable(GL11.GL_CULL_FACE)
    GL11.glEnable(GL11.GL_BLEND)
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
  }

  def renderBeamsToAllChildren(x: Double, y: Double, z: Double, partialTime: Float, node: ITileEntity, beamWidth: Float, color: Color): Unit = {
    beamRenderSetup()
    node.getModule(ManagerModules.TILE_WIRELESS_POWER_NODE, EnumFacing.UP).renderLocations.foreach { loc =>
      renderBeamToLocation(x, y, z, node, color, partialTime, loc, beamWidth)
    }
    beamRenderTeardown()
  }
}

trait WirelessPowerBeamRenderer {
  def renderBeamToChild(x: Double, y: Double, z: Double, partialTime: Float, node: ITileEntity, beamWidth: Float, color: Color, child: Loc4): Unit =
    WirelessPowerBeamRenderer.renderBeamToChild(x, y, z, partialTime, node, beamWidth, color, child)

  def renderBeamsToAllChildren(x: Double, y: Double, z: Double, partialTime: Float, node: ITileEntity, beamWidth: Float, color: Color): Unit =
    WirelessPowerBeamRenderer.renderBeamsToAllChildren(x, y, z, partialTime, node, beamWidth, color)

  def renderBeamToLocation(x: Double, y: Double, z: Double, node: ITileEntity, color: Color, partialTime: Float, loc: Loc4, beamWidth: Float): Unit =
    WirelessPowerBeamRenderer.renderBeamToLocation(x, y, z, node, color, partialTime, loc, beamWidth)
}
