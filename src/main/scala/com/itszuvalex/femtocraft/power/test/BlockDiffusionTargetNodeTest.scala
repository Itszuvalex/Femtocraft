package com.itszuvalex.femtocraft.power.test

import net.minecraft.world.World

/**
 * Created by Christopher Harris (Itszuvalex) on 8/4/15.
 */
class BlockDiffusionTargetNodeTest extends BlockNodeTest {
  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int) = new TileDiffusionTargetNodeTest
  override def getRenderBlockPass: Int = 1
}
