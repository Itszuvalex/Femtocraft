package com.itszuvalex.femtocraft.power.test

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.node.PowerNode
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.world.World

/**
 * Created by Christopher Harris (Itszuvalex) on 8/4/15.
 */
abstract class BlockNodeTest extends TileContainer(Material.iron) {
  setCreativeTab(Femtocraft.tab)

  override def breakBlock(p_149749_1_ : World, p_149749_2_ : Int, p_149749_3_ : Int, p_149749_4_ : Int, p_149749_5_ : Block, p_149749_6_ : Int): Unit = {
    p_149749_1_.getTileEntity(p_149749_2_, p_149749_3_, p_149749_4_) match {
      case i: PowerNode => i.onBlockBreak()
      case _ =>
    }
    super.breakBlock(p_149749_1_, p_149749_2_, p_149749_3_, p_149749_4_, p_149749_5_, p_149749_6_)
  }
}
