package com.itszuvalex.femtocraft.industry.block

import com.itszuvalex.femtocraft.industry.tile.TileNanoFurnace
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.{IBlockAccess, World}

/**
  * Created by Chris on 8/14/2016.
  */
class BlockNanoFurnace extends TileContainer(Material.IRON) {
  override def createNewTileEntity(worldIn: World, meta: Int): TileEntity = new TileNanoFurnace

  override def isNormalCube(state: IBlockState, world: IBlockAccess, pos: BlockPos): Boolean = false

  override def isOpaqueCube(state: IBlockState): Boolean = false
}
