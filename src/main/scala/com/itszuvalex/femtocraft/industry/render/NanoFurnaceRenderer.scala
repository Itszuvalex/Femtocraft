package com.itszuvalex.femtocraft.industry.render

import com.itszuvalex.femtocraft.industry.tile.TileNanoFurnace
import com.itszuvalex.femtocraft.{Femtocraft, Resources}
import com.itszuvalex.itszulib.render.TileEntityRenderCube
import com.itszuvalex.itszulib.util.Color
import net.minecraft.util.EnumFacing
import org.lwjgl.opengl.GL11

/**
  * Created by Chris on 8/14/2016.
  */
class NanoFurnaceRenderer extends TileEntityRenderCube[TileNanoFurnace](Femtocraft.ID.toLowerCase(), Resources.TexBlock("BlockMachineBlock_side_base.png")) {
  val colorText = Resources.TexBlock("BlockMachineBlock_side_color.png")
  var pass      = 0

  override def renderTileEntityAt(te: TileNanoFurnace, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int): Unit = {
    GL11.glColor3f(1f, 1f, 1f)
    pass = 0
    super.renderTileEntityAt(te, x, y, z, partialTicks, destroyStage)
    pass = 1
    var color = Color(0, 0, 0, 0)
    val parent = te.getParent
    if (parent != null)
      color = new Color(parent.getColor)

    GL11.glColor4ub(color.red, color.green, color.blue, 255.toByte)
    super.renderTileEntityAt(te, x, y, z, partialTicks, destroyStage)
  }

  override def preRender(facing: EnumFacing): Unit = {
    if (pass == 0)
      super.preRender(facing)
    else
      bindTexture(colorText)
  }
}
