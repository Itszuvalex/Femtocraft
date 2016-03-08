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
  override def renderTileEntityAt(tile: TileEntity, x: Double, y: Double, z: Double, partialTime: Float): Unit = tile match {
    case node: IPowerNode =>
      renderNode(node, x, y, z, partialTime)
      PowerNodeBeamRenderer.renderPowerBeams(node, x, y, z, partialTime)
    case _ =>
  }
}
