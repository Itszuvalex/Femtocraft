package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.power.test.TileDiffusionNodeTest

class DiffusionNodeRenderer extends NodeCrystalRenderer[TileDiffusionNodeTest] {

  override def renderTileEntityAt(te: TileDiffusionNodeTest, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int): Unit = {
    renderNode(te, x, y, z, partialTicks)
    DiffusionNodeBeamRenderer.renderDiffuseBeams(te, x, y, z, partialTicks)
  }
}
