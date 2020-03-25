package com.itszuvalex.femtocraft.power.block

import com.itszuvalex.femtocraft.power.tile.TileGlowStick
import com.itszuvalex.femtocraft.{FemtoBlocks, Femtocraft}
import com.itszuvalex.itszulib.api.wrappers.{ITileEntity, IWorld}
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviors
import com.itszuvalex.itszulib.core.{BlockTileContainer, TileBlockContainerCore}
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}
import net.minecraft.world.IBlockAccess

/**
  * Created by Christopher Harris (Itszuvalex) on 1/29/2016.
  */
class BlockGlowStick extends TileBlockContainerCore(Material.CIRCUITS, new BlockTileContainer(() => FemtoBlocks.blockGlowStick) {
  override def createTileEntity(world: IWorld, meta: Int): ITileEntity = new TileGlowStick
}, BlockBehaviors.DEFAULT) {
  setCreativeTab(Femtocraft.tab)
  setLightLevel(1f)

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def isNormalCube(state: IBlockState, world: IBlockAccess, pos: BlockPos): Boolean = false

  override def getCollisionBoundingBox(blockState: IBlockState, worldIn: IBlockAccess, pos: BlockPos): AxisAlignedBB = null
}
