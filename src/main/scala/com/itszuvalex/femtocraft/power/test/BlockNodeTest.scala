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

  /**
   * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
   * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
   */
  override def isOpaqueCube =
    false


  override def getRenderBlockPass: Int = -1

  /**
   * If this block doesn't render as an ordinary block it will return False (examples: signs, buttons, stairs, etc)
   */
  override def renderAsNormalBlock = false
}
