package com.itszuvalex.femtocraft.nanite.block

import com.itszuvalex.femtocraft.nanite.tile.TileNaniteExtractor
import com.itszuvalex.femtocraft.nanite.{NaniteRegistry, NaniteStack}
import com.itszuvalex.femtocraft.player.PlayerNaniteCapabilities
import com.itszuvalex.itszulib.core.TileContainer
import com.itszuvalex.itszulib.core.traits.tile.BlockFacing
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{EnumFacing, EnumHand}
import net.minecraft.world.{IBlockAccess, World}

/**
  * Created by Chris on 8/21/2016.
  */
class BlockNaniteExtractor extends TileContainer(Material.IRON) with BlockFacing {
  override def createNewTileEntity(worldIn: World, meta: Int): TileEntity = new TileNaniteExtractor

  override def isNormalCube(state: IBlockState, world: IBlockAccess, pos: BlockPos): Boolean = false

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def onBlockActivated(worldIn: World, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer, hand: EnumHand, heldItem: ItemStack, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    //    super.onBlockActivated(worldIn, pos, state, playerIn, hand, heldItem, side, hitX, hitY, hitZ)
    if (worldIn.isRemote) {
      return true
    }
    val cap = playerIn.getCapability(PlayerNaniteCapabilities.NANITE_CAPABILITY, EnumFacing.NORTH)
    cap.tank.fill(new NaniteStack(NaniteRegistry.NANITE_DUMB, 1), true)
    cap.sync()
    true
  }
}
