package com.itszuvalex.femtocraft.power.test

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.material.Material
import net.minecraft.world.World

/**
 * Created by Christopher Harris (Itszuvalex) on 8/4/15.
 */
class BlockDiffusionTargetNodeTest extends TileContainer(Material.iron) {
  setCreativeTab(Femtocraft.tab)
  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int) = new TileDiffusionTargetNodeTest
}
