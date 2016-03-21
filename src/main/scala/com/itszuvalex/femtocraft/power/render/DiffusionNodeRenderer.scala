package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.power.node.IPowerNode
import net.minecraft.tileentity.TileEntity

class DiffusionNodeRenderer extends NodeCrystalRenderer {
  override def renderTileEntityAt(tile: TileEntity, x: Double, y: Double, z: Double, partialTime: Float): Unit = {
    tile match {
      case node: IPowerNode =>
        renderNode(node, x, y, z, partialTime)
        DiffusionNodeBeamRenderer.renderDiffuseBeams(node, x, y, z, partialTime)
      case _ =>
    }
  }
}
