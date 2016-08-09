package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.power.node.IPowerNode
import net.minecraft.tileentity.TileEntity

/**
  * Created by Christopher Harris (Itszuvalex) on 8/4/15.
  */

object PowerNodeRenderer {
  val RENDER_RADIUS = PowerNodeBeamRenderer.RENDER_RADIUS
}

class PowerNodeRenderer extends NodeCrystalRenderer {

  override def renderTileEntityAt(te: TileEntity with IPowerNode, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int): Unit = {
    renderNode(te, x, y, z, partialTicks)
    PowerNodeBeamRenderer.renderPowerBeams(te, x, y, z, partialTicks)
  }
}
