package com.itszuvalex.femtocraft.logistics.block

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.logistics.tile.TileFluidRepository
import com.itszuvalex.itszulib.api.wrappers.{ITileEntity, IWorld}
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviors
import com.itszuvalex.itszulib.core.{BlockTileContainer, TileBlockContainerCore}
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState

/**
  * Created by Christopher Harris (Itszuvalex) on 12/20/15.
  */
class BlockFluidRepository extends TileBlockContainerCore(Material.IRON, new BlockFluidRepositoryContainerDelegate(), BlockBehaviors.FACING_HORIZONTAL) {

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def isNormalCube(state: IBlockState): Boolean = false
}

class BlockFluidRepositoryContainerDelegate() extends BlockTileContainer(() => FemtoBlocks.blockFluidRepository) {
  override def createTileEntity(world: IWorld, meta: Int): ITileEntity = new TileFluidRepository
}
