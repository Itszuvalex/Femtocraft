package com.itszuvalex.femtocraft.power.block

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.tile.TileGlowStick
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}
import net.minecraft.world.{IBlockAccess, World}

/**
  * Created by Christopher Harris (Itszuvalex) on 1/29/2016.
  */
class BlockGlowStick extends TileContainer(Material.CIRCUITS) {
  setCreativeTab(Femtocraft.tab)
  setLightLevel(1f)

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def isNormalCube(state: IBlockState, world: IBlockAccess, pos: BlockPos): Boolean = false

  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int) = new TileGlowStick

  override def getCollisionBoundingBox(blockState: IBlockState, worldIn: World, pos: BlockPos): AxisAlignedBB = null
}
