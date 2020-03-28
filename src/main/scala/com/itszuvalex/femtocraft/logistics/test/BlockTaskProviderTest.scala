package com.itszuvalex.femtocraft.logistics.test

import com.itszuvalex.femtocraft.{FemtoBlocks, Femtocraft}
import com.itszuvalex.itszulib.api.wrappers.{ITileEntity, IWorld}
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviors
import com.itszuvalex.itszulib.core.{BlockTileContainer, TileBlockContainerCore}
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess

/**
  * Created by Christopher Harris (Itszuvalex) on 8/4/15.
  */
class BlockTaskProviderTest extends TileBlockContainerCore(Material.IRON, new BlockTileContainer(() => FemtoBlocks.testWorkerProvider) {
  override def createTileEntity(world: IWorld, meta: Int): ITileEntity = new TileTaskProviderTest
}, BlockBehaviors.DEFAULT) {
  setCreativeTab(Femtocraft.tab)

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def isNormalCube(state: IBlockState, world: IBlockAccess, pos: BlockPos): Boolean = false
}
