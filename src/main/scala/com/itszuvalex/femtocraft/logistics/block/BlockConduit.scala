package com.itszuvalex.femtocraft.logistics.block

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.logistics.tile.TileConduit
import com.itszuvalex.itszulib.api.wrappers.{ITileEntity, IWorld}
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviors
import com.itszuvalex.itszulib.core.{BlockTileContainer, KeyedBoundingBox, NamedDynamicBoundingBoxCollection, TileBlockContainerCore}
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.util.EnumFacing.{DOWN, EAST, NORTH, SOUTH, UP, WEST, _}
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by Chris on 2/16/2017.
  */
class BlockConduit extends TileBlockContainerCore(Material.IRON, new BlockTileContainer(FemtoBlocks.blockConduit _) {
  override def createTileEntity(world: IWorld, meta: Int): ITileEntity = new TileConduit
}, BlockBehaviors.DEFAULT) {
  val centerBB: KeyedBoundingBox = KeyedBoundingBox("Center", 0, new AxisAlignedBB(.25, .25, .25, .75, .75, .75))
  val posYBB                     = KeyedBoundingBox("PosY", 1, new AxisAlignedBB(.25, .75, .25, .75, 1, .75)) // POS Y
  val negYBB                     = KeyedBoundingBox("NegY", 2, new AxisAlignedBB(.25, 0, .25, .75, .25, .75)) // NEG Y
  val negZBB                     = KeyedBoundingBox("NegZ", 3, new AxisAlignedBB(.25, .25, 0, .75, .75, .25)) // NEG Z
  val posZBB                     = KeyedBoundingBox("PosZ", 4, new AxisAlignedBB(.25, .25, .75, .75, .75, 1)) // POS Z
  val posXBB                     = KeyedBoundingBox("PosX", 5, new AxisAlignedBB(.75, .25, .25, 1, .75, .75)) // POS X
  val negXBB                     = KeyedBoundingBox("NegX", 6, new AxisAlignedBB(0, .25, .25, .25, .75, .75)) // NEG X

  var renderBox = new AxisAlignedBB(.25, .25, .25, .75, .75, .75)

  boundingBoxes =  Some(new NamedDynamicBoundingBoxCollection(centerBB _, getBoundingBoxes))

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def isNormalCube(state: IBlockState): Boolean = false

  def getBoundingBoxes(worldIn: IWorld, pos: BlockPos): ArrayBuffer[KeyedBoundingBox] = {
    val list = new ArrayBuffer[KeyedBoundingBox]()
    list += centerBB

    worldIn.getITileEntity(pos) match {
      case null =>
      case t: TileConduit =>
        list ++= VALUES.withFilter(t.conduit.isConnected).map {
          case UP => posYBB
          case DOWN => negYBB
          case NORTH => negZBB
          case SOUTH => posZBB
          case EAST => posXBB
          case WEST => negXBB
          case _ => centerBB
        }
      case _ =>
    }
    list
  }
}
