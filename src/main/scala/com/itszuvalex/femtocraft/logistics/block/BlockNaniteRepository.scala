package com.itszuvalex.femtocraft.logistics.block

import com.itszuvalex.femtocraft.logistics.tile.TileNaniteRepository
import com.itszuvalex.itszulib.core.TileContainer
import com.itszuvalex.itszulib.core.traits.tile.BlockFacing
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/**
  * Created by Christopher Harris (Itszuvalex) on 12/20/15.
  */
class BlockNaniteRepository extends TileContainer(Material.IRON) with BlockFacing {

  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TileNaniteRepository

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def isNormalCube(state: IBlockState): Boolean = false
}
