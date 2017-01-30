package com.itszuvalex.femtocraft.power.block

import com.itszuvalex.femtocraft.power.tile.TileCrystalHeatExchanger
import com.itszuvalex.itszulib.core.TileContainer
import com.itszuvalex.itszulib.core.traits.block.DroppableInventory
import com.itszuvalex.itszulib.core.traits.tile.BlockFacing
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.{IBlockAccess, World}

/**
  * Created by Chris on 1/8/2017.
  */
class BlockCrystalHeatExchanger extends TileContainer(Material.IRON) with BlockFacing with DroppableInventory {
  override def createNewTileEntity(worldIn: World, meta: Int): TileEntity = new TileCrystalHeatExchanger

  override def isNormalCube(state: IBlockState, world: IBlockAccess, pos: BlockPos): Boolean = false

  override def isOpaqueCube(state: IBlockState): Boolean = false
}
