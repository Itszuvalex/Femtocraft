package com.itszuvalex.femtocraft.common.block

import com.itszuvalex.femtocraft.api.IConduitTier
import com.itszuvalex.femtocraft.power.tile.TilePowerConduitCrystal
import com.itszuvalex.itszulib.api.wrappers.{ITileEntity, IWorld}
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviors
import com.itszuvalex.itszulib.core.{BlockTileContainer, KeyedBoundingBox, NamedDynamicBoundingBoxCollection, TileBlockContainerCore}
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.util.EnumFacing.{DOWN, EAST, NORTH, SOUTH, UP, WEST, _}
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}
import net.minecraft.world.IBlockAccess

import scala.collection.mutable.ArrayBuffer

/**
 * Created by Chris on 2/16/2017.
 */
abstract class BlockConduit(tier: IConduitTier, blockFunc: () => Block, tileFunc: () => ITileEntity) extends TileBlockContainerCore(Material.IRON, new BlockTileContainer(blockFunc) {
  override def createTileEntity(world: IWorld, meta: Int): ITileEntity = tileFunc()
}, BlockBehaviors.DEFAULT) {
  val centerBB: KeyedBoundingBox = KeyedBoundingBox("Center", 0, new AxisAlignedBB(.375, .375, .375, .625, .625, .625))
  val posYBB                     = KeyedBoundingBox("PosY", 1, new AxisAlignedBB(.375, .625, .375, .625, 1, .625)) // POS Y
  val negYBB                     = KeyedBoundingBox("NegY", 2, new AxisAlignedBB(.375, 0, .375, .625, .375, .625)) // NEG Y
  val negZBB                     = KeyedBoundingBox("NegZ", 3, new AxisAlignedBB(.375, .375, 0, .625, .625, .375)) // NEG Z
  val posZBB                     = KeyedBoundingBox("PosZ", 4, new AxisAlignedBB(.375, .375, .625, .625, .625, 1)) // POS Z
  val posXBB                     = KeyedBoundingBox("PosX", 5, new AxisAlignedBB(.625, .375, .375, 1, .625, .625)) // POS X
  val negXBB                     = KeyedBoundingBox("NegX", 6, new AxisAlignedBB(0, .375, .375, .375, .625, .625)) // NEG X

  var renderBox = new AxisAlignedBB(.375, .375, .375, .625, .625, .625)

  boundingBoxes = Some(new NamedDynamicBoundingBoxCollection(centerBB _, getBoundingBoxes))

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def isNormalCube(state: IBlockState): Boolean = false

  def getBoundingBoxes(worldIn: IWorld, pos: BlockPos): ArrayBuffer[KeyedBoundingBox] = {
    val list = new ArrayBuffer[KeyedBoundingBox]()
    list += centerBB

    worldIn.getITileEntity(pos) match {
      case null =>
      case t: TilePowerConduitCrystal =>
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

  override def getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB = new AxisAlignedBB(.25, .25, .25, .75, .75, .75)
}
