package com.itszuvalex.femtocraft.industry.render

import com.itszuvalex.femtocraft.industry.tile.TileNanoFurnace
import com.itszuvalex.femtocraft.{Femtocraft, Resources}
import com.itszuvalex.itszulib.core.traits.tile.BlockFacing
import com.itszuvalex.itszulib.render.TileEntityRenderCube
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.EnumFacing
import org.lwjgl.opengl.GL11

/**
  * Created by Chris on 8/14/2016.
  */
class NanoFurnaceRenderer extends TileEntityRenderCube[TileNanoFurnace](Femtocraft.ID.toLowerCase(), Resources.TexBlock("BlockMachineBlock_side_base.png")) {
  val colorTex      = Resources.TexBlock("BlockMachineBlock_side_color.png")
  val frontTex      = Resources.TexBlock("BlockMachineBlock_front_base.png")
  val frontColorTex = Resources.TexBlock("BlockMachineBlock_front_color.png")
  var pass          = 0
  var color         = Color(0, 255.toByte, 255.toByte, 255.toByte)

  override def renderTileEntityAt(te: TileNanoFurnace, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int): Unit = {
    facing = Option(te).map(_.getWorld.getBlockState(te.getPos).getValue(BlockFacing.FACING)).getOrElse(EnumFacing.NORTH)
    GL11.glColor3f(1f, 1f, 1f)
    pass = 0
    super.renderTileEntityAt(te, x, y, z, partialTicks, destroyStage)
    pass = 1
    color = Option(te).map(_.getParent).flatMap(Option(_)).map(parent => new Color(parent.getColor)).getOrElse(Color(0, 255.toByte, 255.toByte, 255.toByte))
    super.renderTileEntityAt(te, x, y, z, partialTicks, destroyStage)
    GlStateManager.enableLighting()
  }

  override def renderTileEntityAsItem(x: Double, y: Double, z: Double, partialTicks: Float): Unit = {
    facing = EnumFacing.SOUTH
    super.renderTileEntityAsItem(x, y, z, partialTicks)
  }

  override def preFaceRender(facing: EnumFacing): Unit = {
    if (pass == 0) {
      facing match {
        case EnumFacing.NORTH => bindTexture(frontTex)
        case _ => super.preFaceRender(facing)
      }
    }
    else {
      GL11.glColor4ub(color.red, color.green, color.blue, 255.toByte)
      GlStateManager.disableLighting()
      GlStateManager
      facing match {
        case EnumFacing.NORTH => bindTexture(frontColorTex)
        case _ => bindTexture(colorTex)
      }
    }
  }
}
