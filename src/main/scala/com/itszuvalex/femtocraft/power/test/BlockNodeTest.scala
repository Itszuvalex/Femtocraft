package com.itszuvalex.femtocraft.power.test

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.node.PowerNode
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.{IBlockAccess, World}

/**
  * Created by Christopher Harris (Itszuvalex) on 8/4/15.
  */
abstract class BlockNodeTest extends TileContainer(Material.IRON) {
  setCreativeTab(Femtocraft.tab)


  override def breakBlock(world: World, pos: BlockPos, state: IBlockState): Unit = {
    world.getTileEntity(pos) match {
      case i: PowerNode => i.onBlockBreak()
      case _ =>
    }
    super.breakBlock(world, pos, state)
  }

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def isNormalCube(state: IBlockState, world: IBlockAccess, pos: BlockPos): Boolean = false
}
