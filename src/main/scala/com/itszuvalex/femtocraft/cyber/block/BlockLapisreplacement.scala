package com.itszuvalex.femtocraft.cyber.block

import java.util.Random

import com.itszuvalex.femtocraft.Femtocraft
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
  * Created by Christopher on 8/27/2015.
  */
object BlockLapisreplacement {
  val MIN_DROP = 3
  val MAX_DROP = 5
}

class BlockLapisreplacement extends Block(Material.IRON) {
  setHardness(3.0F)
  setCreativeTab(Femtocraft.tab)


  /**
    * Get the Item that this Block should drop when harvested.
    */
  override def getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item = Femtocraft.items.itemLapisreplacementDust

  /**
    * Get the quantity dropped based on the given fortune level
    */
  override def quantityDroppedWithBonus(fortune: Int, random: Random): Int = this.quantityDropped(random) + random.nextInt(fortune + 1)

  /**
    * Returns the quantity of items to drop on block destruction.
    */
  override def quantityDropped(random: Random): Int = BlockLapisreplacement.MIN_DROP + random.nextInt(BlockLapisreplacement.MAX_DROP - BlockLapisreplacement.MIN_DROP + 1)

  /**
    * Spawns this Block's drops into the World as EntityItems.
    */
  override def dropBlockAsItemWithChance(worldIn: World, pos: BlockPos, state: IBlockState, chance: Float, fortune: Int) {
    super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune)
  }
}
