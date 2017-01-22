package com.itszuvalex.femtocraft.industry.render

import com.itszuvalex.femtocraft.render.FemtoRenderUtils
import com.itszuvalex.femtocraft.{Femtocraft, Resources}
import com.itszuvalex.itszulib.core.traits.tile.BlockFacing
import com.itszuvalex.itszulib.render.{RenderUtils, TileEntityRenderCube}
import com.itszuvalex.itszulib.util.Color
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{EnumFacing, ResourceLocation}
import org.lwjgl.opengl.GL11

/**
  * Created by Chris on 8/14/2016.
  */
abstract class FemtoMachineRender[T <: TileEntity](val machineFront: ResourceLocation) extends TileEntityRenderCube[T](Femtocraft.ID.toLowerCase(), Resources.TexBlock("blockmachineblock_side_base.png")) {
  val colorTex      = Resources.TexBlock("blockmachineblock_side_color.png")
  val frontTex      = Resources.TexBlock("blockmachineblock_front_base.png")
  val frontColorTex = Resources.TexBlock("blockmachineblock_front_color.png")
  var pass          = 0
  var color         = Color(0, 255.toByte, 255.toByte, 255.toByte)
  var lastTe: T     = _

  def getColor(te: T): Color

  override def renderTileEntityAt(te: T, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int): Unit = {
    lastTe = te
    facing = Option(te).map(_.getWorld.getBlockState(te.getPos).getValue(BlockFacing.FACING)).getOrElse(EnumFacing.NORTH)
    GL11.glColor3f(1f, 1f, 1f)
    pass = 0
    super.renderTileEntityAt(te, x, y, z, partialTicks, destroyStage)
    pass = 1
    color = getColor(te)
    super.renderTileEntityAt(te, x, y, z, partialTicks, destroyStage)
  }

  override def renderTileEntityAsItem(x: Double, y: Double, z: Double, partialTicks: Float): Unit = {
    facing = EnumFacing.SOUTH
    super.renderTileEntityAsItem(x, y, z, partialTicks)
  }

  override def renderTileEntityInWorld(te: T, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int): Unit = {
    super.renderTileEntityInWorld(te, x, y, z, partialTicks, destroyStage)
  }

  override def preFaceRender(facing: EnumFacing): Unit = {
    if (pass == 0) {
      GL11.glColor3f(1f, 1f, 1f)
      facing match {
        case EnumFacing.NORTH => bindTexture(frontTex)
        case _ => super.preFaceRender(facing)
      }
    }
    else {
      GL11.glColor4ub(color.red, color.green, color.blue, 255.toByte)
      FemtoRenderUtils.disableLightMaps()
      facing match {
        case EnumFacing.NORTH => bindTexture(frontColorTex)
        case _ => bindTexture(colorTex)
      }
    }
  }

  override def postFaceRender(facing: EnumFacing): Unit = {
    super.postFaceRender(facing)

    GL11.glColor3f(1f, 1f, 1f)
    if (facing == EnumFacing.NORTH) {
      FemtoRenderUtils.enableLightMap(lastTe)
      bindTexture(machineFront)
      RenderUtils.drawArbitraryFace(0, 0, 0, -.001f, 1.001f, -.001f, 1.001f, -.001f, 1.001f, facing, null, 0, 1, 0, 1)
    }
  }

  override def postRender(): Unit = {
    super.postRender()
    GL11.glColor3f(1f, 1f, 1f)
    FemtoRenderUtils.enableLightMap(lastTe)
  }
}