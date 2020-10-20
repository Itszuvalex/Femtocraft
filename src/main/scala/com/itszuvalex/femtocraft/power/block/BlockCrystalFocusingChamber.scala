package com.itszuvalex.femtocraft.power.block

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.power.tile.TileCrystalFocusingChamber
import com.itszuvalex.itszulib.api.wrappers.{ITileEntity, IWorld}
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviors
import com.itszuvalex.itszulib.core.{BlockTileContainer, TileBlockContainerCore}
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess

class BlockCrystalFocusingChamber extends TileBlockContainerCore(Material.IRON, new BlockCrystalFocusingChamberCore(), BlockBehaviors.DEFAULT) {
  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def isNormalCube(state: IBlockState, world: IBlockAccess, pos: BlockPos): Boolean = false
}

class BlockCrystalFocusingChamberCore() extends BlockTileContainer(() => FemtoBlocks.blockCrystalFocusingChamber) {
  override def createTileEntity(world: IWorld, meta: Int): ITileEntity = new TileCrystalFocusingChamber
}

