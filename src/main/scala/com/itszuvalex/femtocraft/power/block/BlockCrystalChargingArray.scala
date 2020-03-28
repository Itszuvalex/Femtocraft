package com.itszuvalex.femtocraft.power.block

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.power.tile.TileCrystalChargingArray
import com.itszuvalex.itszulib.api.wrappers.{ITileEntity, IWorld}
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviors
import com.itszuvalex.itszulib.core.{BlockTileContainer, TileBlockContainerCore}
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess

/**
  * Created by Chris on 1/8/2017.
  */
class BlockCrystalChargingArray extends TileBlockContainerCore(Material.IRON, new BlockCrystalChargingArrayContainerDelegate(), BlockBehaviors.FACING_HORIZONTAL) {
  override def isNormalCube(state: IBlockState, world: IBlockAccess, pos: BlockPos): Boolean = false

  override def isOpaqueCube(state: IBlockState): Boolean = false
}

class BlockCrystalChargingArrayContainerDelegate() extends BlockTileContainer(FemtoBlocks.blockCrystalChargingArray _) {
  override def createTileEntity(world: IWorld, meta: Int): ITileEntity = new TileCrystalChargingArray
}
