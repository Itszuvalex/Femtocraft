package com.itszuvalex.femtocraft.industry.block

import com.itszuvalex.femtocraft.industry.tile.TileNaniteExtractor
import com.itszuvalex.itszulib.core.TileContainer
import com.itszuvalex.itszulib.core.traits.block.DroppableInventory
import com.itszuvalex.itszulib.core.traits.tile.BlockFacing
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.{IBlockAccess, World}

/**
  * Created by Chris on 8/21/2016.
  */
class BlockNaniteExtractor extends TileContainer(Material.IRON) with BlockFacing with DroppableInventory {
  override def createNewTileEntity(worldIn: World, meta: Int): TileEntity = new TileNaniteExtractor

  override def isNormalCube(state: IBlockState, world: IBlockAccess, pos: BlockPos): Boolean = false

  override def isOpaqueCube(state: IBlockState): Boolean = false
}
