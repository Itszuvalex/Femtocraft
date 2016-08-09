package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.power.node.IPowerNode
import net.minecraft.tileentity.TileEntity

class DiffusionNodeRenderer extends NodeCrystalRenderer {

  override def renderTileEntityAt(te: TileEntity with IPowerNode, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int): Unit = {
    renderNode(te, x, y, z, partialTicks)
    DiffusionNodeBeamRenderer.renderDiffuseBeams(te, x, y, z, partialTicks)
  }
}
