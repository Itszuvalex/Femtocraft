package com.itszuvalex.femtocraft.industry.block

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.industry.tile.TileCrystalFurnace
import com.itszuvalex.itszulib.api.wrappers.{ITileEntity, IWorld}
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviors
import com.itszuvalex.itszulib.core.{BlockTileContainer, TileBlockContainerCore}
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess

class BlockCrystalFurnace extends TileBlockContainerCore(Material.IRON, new BlockTileContainer(FemtoBlocks.blockCrystalFurnace _) {
  override def createTileEntity(world: IWorld, meta: Int): ITileEntity = new TileCrystalFurnace
}, BlockBehaviors.FACING_HORIZONTAL){

  override def isNormalCube(state: IBlockState, world: IBlockAccess, pos: BlockPos): Boolean = false

  override def isOpaqueCube(state: IBlockState): Boolean = false
}
