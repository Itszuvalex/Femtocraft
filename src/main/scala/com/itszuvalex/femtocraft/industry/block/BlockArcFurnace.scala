package com.itszuvalex.femtocraft.industry.block

import com.itszuvalex.femtocraft.industry.tile.TileArcFurnace
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.{IBlockAccess, World}

/**
  * Created by Christopher Harris (Itszuvalex) on 8/28/15.
  */
class BlockArcFurnace extends TileContainer(Material.IRON) {
  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TileArcFurnace

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def isNormalCube(state: IBlockState, world: IBlockAccess, pos: BlockPos): Boolean = false
}
