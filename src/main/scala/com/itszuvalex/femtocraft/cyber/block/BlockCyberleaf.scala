package com.itszuvalex.femtocraft.cyber.block

import java.util.Random

import com.itszuvalex.femtocraft.Femtocraft
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.block.{Block, SoundType}
import net.minecraft.item.Item
import net.minecraft.util.BlockRenderLayer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
  * Created by Christopher on 8/27/2015.
  */
object BlockCyberleaf {
  val MIN_DROP = 3
  val MAX_DROP = 5
}

class BlockCyberleaf extends Block(Material.LEAVES) {
  this.setHardness(0.2F)
  this.setLightOpacity(1)
  this.setSoundType(SoundType.PLANT)

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def getBlockLayer: BlockRenderLayer = BlockRenderLayer.CUTOUT_MIPPED

  /**
    * Get the Item that this Block should drop when harvested.
    */
  override def getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item = Femtocraft.items.itemCyberleaf

  /**
    * Get the quantity dropped based on the given fortune level
    */
  override def quantityDroppedWithBonus(fortune: Int, random: Random): Int = this.quantityDropped(random) + random.nextInt(fortune + 1)

  /**
    * Returns the quantity of items to drop on block destruction.
    */
  override def quantityDropped(random: Random): Int = BlockCyberleaf.MIN_DROP + random.nextInt(BlockCyberleaf.MAX_DROP - BlockCyberleaf.MIN_DROP + 1)

  /**
    * Spawns this Block's drops into the World as EntityItems.
    */
  override def dropBlockAsItemWithChance(worldIn: World, pos: BlockPos, state: IBlockState, chance: Float, fortune: Int) {
    super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune)
  }
}

