package com.itszuvalex.femtocraft.cyber.block

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.cyber.tile.TileCybermatDisintegrator
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.{IBlockAccess, World}

/**
  * Created by Alex on 30.09.2015.
  */
class BlockCybermatDisintegrator extends TileContainer(Material.IRON) {
  setCreativeTab(Femtocraft.tab)

  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TileCybermatDisintegrator

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def isNormalCube(state: IBlockState, world: IBlockAccess, pos: BlockPos): Boolean = false
}
