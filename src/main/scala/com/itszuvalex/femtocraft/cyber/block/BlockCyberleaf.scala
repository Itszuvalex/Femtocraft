package com.itszuvalex.femtocraft.cyber.block

import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.block.{Block, SoundType}
import net.minecraft.util.BlockRenderLayer

/**
  * Created by Christopher on 8/27/2015.
  */
class BlockCyberleaf extends Block(Material.LEAVES) {
  this.setHardness(0.2F)
  this.setLightOpacity(1)
  this.setSoundType(SoundType.PLANT)

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def getBlockLayer: BlockRenderLayer = BlockRenderLayer.CUTOUT_MIPPED

  override def isVisuallyOpaque: Boolean = false
}

