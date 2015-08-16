package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.node.IPowerNode
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation

object DiffusionNodeRenderer {
  private val beamColorLocation = new ResourceLocation(Femtocraft.ID + ":" + "textures/diffusion_particles_colored.png")
  val BEAM_WIDTH    = .1f
  val RENDER_RADIUS = 64
}

class DiffusionNodeRenderer extends TileEntitySpecialRenderer with NodeCrystalRenderer with PowerBeamRenderer {

  override def renderTileEntityAt(tile: TileEntity, x: Double, y: Double, z: Double, partialTime: Float): Unit = {
    tile match {
      case node: IPowerNode =>
        this.bindTexture(NodeCrystalRenderer.crystalTexLocation)
        renderCrystal(x, y, z, node, partialTime)
        this.bindTexture(DiffusionNodeRenderer.beamColorLocation)
        this.renderBeamsToAllChildren(x, y, z, partialTime, node, DiffusionNodeRenderer.BEAM_WIDTH, new Color(node.getColor).setAlpha(64.toByte))
      case _ =>
    }

  }
}
