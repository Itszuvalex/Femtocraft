package com.itszuvalex.femtocraft.power.block

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.tile.TilePowerPedestal
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.{IBlockAccess, World}

/**
  * Created by Christopher on 8/30/2015.
  */
class BlockPowerPedestal extends TileContainer(Material.IRON) {
  setCreativeTab(Femtocraft.tab)

  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TilePowerPedestal

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def isNormalCube(state: IBlockState, world: IBlockAccess, pos: BlockPos): Boolean = false

  override def onBlockPlacedBy(worldIn: World, pos: BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack): Unit = {
    worldIn.getTileEntity(pos) match {
      case i: TilePowerPedestal =>
        i.onPostBlockPlaced()
      case _ =>
    }
    super.onBlockPlacedBy(worldIn, pos, state, placer, stack)
  }
}
