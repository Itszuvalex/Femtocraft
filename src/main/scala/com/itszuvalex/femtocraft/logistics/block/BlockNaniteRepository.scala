package com.itszuvalex.femtocraft.logistics.block

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.logistics.tile.TileNaniteRepository
import com.itszuvalex.itszulib.api.wrappers.{ITileEntity, IWorld}
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviors
import com.itszuvalex.itszulib.core.{BlockTileContainer, TileBlockContainerCore}
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState

/**
  * Created by Christopher Harris (Itszuvalex) on 12/20/15.
  */
class BlockNaniteRepository extends TileBlockContainerCore(Material.IRON, new BlockNaniteRepositoryContainerDelegate(), BlockBehaviors.FACING_HORIZONTAL) {

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def isNormalCube(state: IBlockState): Boolean = false
}

class BlockNaniteRepositoryContainerDelegate() extends BlockTileContainer(null) {
  override def createTileEntity(world: IWorld, meta: Int): ITileEntity = new TileNaniteRepository

  override def toMinecraft: Block = FemtoBlocks.blockNaniteRepository
}
