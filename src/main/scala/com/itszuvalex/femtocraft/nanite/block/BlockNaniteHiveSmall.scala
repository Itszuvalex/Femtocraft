package com.itszuvalex.femtocraft.nanite.block

import com.itszuvalex.femtocraft.nanite.tile.TileNaniteHiveSmall
import net.minecraft.block.BlockContainer
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.{IBlockAccess, World}

/**
  * Created by Christopher on 8/29/2015.
  */
class BlockNaniteHiveSmall extends BlockContainer(Material.IRON) {

  override def isNormalCube(state: IBlockState, world: IBlockAccess, pos: BlockPos): Boolean = false

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TileNaniteHiveSmall
}

