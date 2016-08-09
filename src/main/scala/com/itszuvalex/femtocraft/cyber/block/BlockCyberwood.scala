package com.itszuvalex.femtocraft.cyber.block

import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess

/**
  * Created by Christopher on 8/27/2015.
  */
class BlockCyberwood extends Block(Material.WOOD) {
  override def canSustainLeaves(state: IBlockState, world: IBlockAccess, pos: BlockPos): Boolean = true
}
