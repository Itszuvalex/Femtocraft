package com.itszuvalex.femtocraft.power.test

import com.itszuvalex.femtocraft.power.node.GenerationNode
import com.itszuvalex.femtocraft.power.render.PowerNodeRenderer
import com.itszuvalex.itszulib.render.Vector3
import net.minecraft.util.AxisAlignedBB

/**
 * Created by Christopher Harris (Itszuvalex) on 8/4/15.
 */
class TileGenerationNodeTest extends TileNodeTest with GenerationNode {
  override def getRenderBoundingBox: AxisAlignedBB = {
    val center = Vector3(xCoord + .5f, yCoord + .5f, zCoord + .5f)
    AxisAlignedBB.getBoundingBox(center.x - PowerNodeRenderer.RENDER_RADIUS,
                                 center.y - PowerNodeRenderer.RENDER_RADIUS,
                                 center.z - PowerNodeRenderer.RENDER_RADIUS,
                                 center.x + PowerNodeRenderer.RENDER_RADIUS,
                                 center.y + PowerNodeRenderer.RENDER_RADIUS,
                                 center.z + PowerNodeRenderer.RENDER_RADIUS)
  }
}

