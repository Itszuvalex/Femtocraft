package com.itszuvalex.femtocraft.cyber.block

import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraftforge.common.{EnumPlantType, IPlantable}

/**
  * Created by Christopher on 8/27/2015.
  */
class BlockCyberweave extends Block(Material.IRON) {

  override def canSustainPlant(state: IBlockState, world: IBlockAccess, pos: BlockPos, direction: EnumFacing, plantable: IPlantable): Boolean = {
    val plantType = plantable.getPlantType(world, pos.up())

    plantType match {
      case EnumPlantType.Plains => true
      case EnumPlantType.Beach =>
        world.getBlockState(pos.east()).getMaterial == Material.WATER ||
          world.getBlockState(pos.west()).getMaterial == Material.WATER ||
          world.getBlockState(pos.north()).getMaterial == Material.WATER ||
          world.getBlockState(pos.south()).getMaterial == Material.WATER
      case _ => false
    }
  }
}
