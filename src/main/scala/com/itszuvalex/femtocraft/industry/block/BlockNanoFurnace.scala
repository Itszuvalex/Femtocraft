package com.itszuvalex.femtocraft.industry.block

import com.itszuvalex.femtocraft.industry.tile.TileNanoFurnace
import com.itszuvalex.itszulib.api.wrappers.{ITileEntity, IWorld}
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviors
import com.itszuvalex.itszulib.core.{BlockTileContainer, TileBlockContainerCore}
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess

/**
  * Created by Chris on 8/14/2016.
  */
class BlockNanoFurnace extends TileBlockContainerCore(Material.IRON, new BlockNanoFurnaceDelegate(this), BlockBehaviors.FACING_HORIZONTAL) {

  override def isNormalCube(state: IBlockState, world: IBlockAccess, pos: BlockPos): Boolean = false

  override def isOpaqueCube(state: IBlockState): Boolean = false
}

class BlockNanoFurnaceDelegate(block: Block) extends BlockTileContainer(block) {
  override def createTileEntity(world: IWorld, meta: Int): ITileEntity = new TileNanoFurnace
}