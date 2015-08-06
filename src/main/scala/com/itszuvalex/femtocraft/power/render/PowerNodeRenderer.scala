package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.node.IPowerNode
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation

/**
 * Created by Christopher Harris (Itszuvalex) on 8/4/15.
 */
object PowerNodeRenderer {
  private val beamOuterLocation = new ResourceLocation(Femtocraft.ID + ":" + "textures/power_beam_outer.png")
  private val beamColorLocation = new ResourceLocation(Femtocraft.ID + ":" + "textures/power_beam_colored.png")
  val BEAM_WIDTH    = .2f
  val RENDER_RADIUS = 64

  var alphaOverride : Int = -1

  def setAlphaOn(): Unit = {
    alphaOverride = -1
  }

  def setAlphaMinimal(): Unit = {
    alphaOverride = 32
  }

  def setAlphaOff(): Unit = {
    alphaOverride = 0
  }
}

class PowerNodeRenderer extends TileEntitySpecialRenderer with CrystalRenderer with BeamRenderer {

  override def renderTileEntityAt(tile: TileEntity, x: Double, y: Double, z: Double, partialTime: Float): Unit = {
    tile match {
      case node: IPowerNode =>
        val nodecolor : Color = new Color(node.getColor)
        this.bindTexture(CrystalRenderer.crystalTexLocation)
        renderCrystal(x, y, z, node, partialTime)
        this.bindTexture(PowerNodeRenderer.beamOuterLocation)
        renderBeamsToAllChildren(x, y, z, partialTime, node, PowerNodeRenderer.BEAM_WIDTH, Color(if (PowerNodeRenderer.alphaOverride != -1) PowerNodeRenderer.alphaOverride.toByte else nodecolor.alpha, 255.toByte, 255.toByte, 255.toByte))
        this.bindTexture(PowerNodeRenderer.beamColorLocation)
        renderBeamsToAllChildren(x, y, z, partialTime, node, PowerNodeRenderer.BEAM_WIDTH, Color(if (PowerNodeRenderer.alphaOverride != -1) PowerNodeRenderer.alphaOverride.toByte else nodecolor.alpha, nodecolor.red, nodecolor.green, nodecolor.blue))
      case _ =>
    }

  }
}
