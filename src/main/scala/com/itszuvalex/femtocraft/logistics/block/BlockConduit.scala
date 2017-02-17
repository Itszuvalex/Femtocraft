package com.itszuvalex.femtocraft.logistics.block

import java.util

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.logistics.tile.TileConduit
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}
import net.minecraft.world.{IBlockAccess, World}

/**
  * Created by Chris on 2/16/2017.
  */
class BlockConduit extends TileContainer(Material.IRON) {
  override def createNewTileEntity(worldIn: World, meta: Int): TileEntity = new TileConduit

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def isNormalCube(state: IBlockState): Boolean = false

  override def addCollisionBoxToList(state: IBlockState, worldIn: World, pos: BlockPos, entityBox: AxisAlignedBB, collidingBoxes: util.List[AxisAlignedBB], entityIn: Entity, p_185477_7_ : Boolean): Unit = {
    super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, p_185477_7_)
    worldIn.getTileEntity(pos) match {
      case null =>
      case t: TileConduit =>
        val conduit = t.getCapability(Capabilities.TILE_CONDUIT, null)
        val offsetMin = .0d
        val offsetMax = .25d
        EnumFacing.VALUES.withFilter(conduit.isConnected).foreach { facing =>
          // If Facing, .75 and 1, or 0 and .25
          // other 2 are .25 and .75
          // offset by -.5
          // -.5 and -.25, .25 and .5
          val xm = facing.getFrontOffsetX
          val ym = facing.getFrontOffsetY
          val zm = facing.getFrontOffsetZ
          collidingBoxes.add(new AxisAlignedBB(.25, .25, .25, .75, .75, .75))
        }
      case _ =>
    }
  }

  override def getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB = {
    new AxisAlignedBB(.25, .25, .25, .75, .75, .75)
  }
}
