package com.itszuvalex.femtocraft.logistics.block

import com.itszuvalex.femtocraft.logistics.tile.TileConduit
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/**
  * Created by Chris on 2/16/2017.
  */
class BlockConduit extends TileContainer(Material.IRON) {
  override def createNewTileEntity(worldIn: World, meta: Int): TileEntity = new TileConduit

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def isNormalCube(state: IBlockState): Boolean = false

}
