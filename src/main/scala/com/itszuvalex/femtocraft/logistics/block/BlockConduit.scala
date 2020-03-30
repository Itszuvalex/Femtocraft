package com.itszuvalex.femtocraft.logistics.block

import java.util

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.logistics.tile.TileConduit
import com.itszuvalex.itszulib.api.wrappers.{ITileEntity, IWorld}
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviors
import com.itszuvalex.itszulib.core.{BlockTileContainer, TileBlockContainerCore}
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.{Entity, EntityLivingBase}
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing._
import net.minecraft.util.math.{AxisAlignedBB, BlockPos, RayTraceResult, Vec3d}
import net.minecraft.world.{IBlockAccess, World}

import scala.collection.JavaConversions._

/**
  * Created by Chris on 2/16/2017.
  */
class BlockConduit extends TileBlockContainerCore(Material.IRON, new BlockTileContainer(FemtoBlocks.blockConduit _) {
  override def createTileEntity(world: IWorld, meta: Int): ITileEntity = new TileConduit
}, BlockBehaviors.DEFAULT) {
  var renderBox = new AxisAlignedBB(.25, .25, .25, .75, .75, .75)

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def isNormalCube(state: IBlockState): Boolean = false


  override def observedNeighborChange(observerState: IBlockState, world: World, observerPos: BlockPos, changedBlock: Block, changedBlockPos: BlockPos): Unit = {
    super.observedNeighborChange(observerState, world, observerPos, changedBlock, changedBlockPos)
    world.getTileEntity(observerPos) match {
      case null =>
      case t: TileConduit => t.onNeighborChange(changedBlockPos)
      case _ =>
    }
  }

  override def onBlockPlacedBy(worldIn: World, pos: BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack): Unit = {
    super.onBlockPlacedBy(worldIn, pos, state, placer, stack)
    worldIn.getTileEntity(pos) match {
      case null =>
      case t: TileConduit => t.onBlockPlaced()
      case _ =>
    }
  }

  override def addCollisionBoxToList(state: IBlockState, worldIn: World, pos: BlockPos, entityBox: AxisAlignedBB, collidingBoxes: util.List[AxisAlignedBB], entityIn: Entity, p_185477_7_ : Boolean): Unit = {
    super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, p_185477_7_)

    getBoundingBoxes(worldIn, pos).withFilter(entityBox.intersects).foreach(collidingBoxes.add)
  }

  override def collisionRayTrace(blockState: IBlockState, worldIn: World, pos: BlockPos, start: Vec3d, end: Vec3d): RayTraceResult = {
    val results = getBoundingBoxes(worldIn, pos).map(box => (box, rayTrace(pos, start, end, box))).filter(a => a._2 != null).sortWith { (a, b) =>
      start.squareDistanceTo(a._2.hitVec) < start.squareDistanceTo(b._2.hitVec)
    }
    results.headOption.map { r =>
      renderBox = r._1
      r._2
    }.orNull
  }

  def getBoundingBoxes(worldIn: World, pos: BlockPos): util.List[AxisAlignedBB] = {
    val list = new util.ArrayList[AxisAlignedBB]()
    list += new AxisAlignedBB(.25, .25, .25, .75, .75, .75) // Default

    worldIn.getTileEntity(pos) match {
      case null =>
      case t: TileConduit =>
        val conduit = t.getCapability(Capabilities.TILE_CONDUIT, null)
        list ++= VALUES.withFilter(conduit.isConnected).map {
          case UP => new AxisAlignedBB(.25, .75, .25, .75, 1, .75) // POS Y
          case DOWN => new AxisAlignedBB(.25, 0, .25, .75, .25, .75) // NEG Y
          case NORTH => new AxisAlignedBB(.25, .25, 0, .75, .75, .25) // NEG Z
          case SOUTH => new AxisAlignedBB(.25, .25, .75, .75, .75, 1) // POS Z
          case EAST => new AxisAlignedBB(.75, .25, .25, 1, .75, .75) // POS X
          case WEST => new AxisAlignedBB(0, .25, .25, .25, .75, .75) // NEG X
          case _ => new AxisAlignedBB(.25, .25, .25, .75, .75, .75) // Default
        }
    }
    list
  }

  override protected def rayTrace(pos: BlockPos, start: Vec3d, end: Vec3d, boundingBox: AxisAlignedBB): RayTraceResult = {
    val vec3d         : Vec3d          = start.subtract(pos.getX.toDouble, pos.getY.toDouble, pos.getZ.toDouble)
    val vec3d1        : Vec3d          = end.subtract(pos.getX.toDouble, pos.getY.toDouble, pos.getZ.toDouble)
    val raytraceresult: RayTraceResult = boundingBox.calculateIntercept(vec3d, vec3d1)
    if (raytraceresult == null) null
    else new RayTraceResult(raytraceresult.hitVec.addVector(pos.getX.toDouble, pos.getY.toDouble, pos.getZ.toDouble), raytraceresult.sideHit, pos)
  }

  override def getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB = {
    new AxisAlignedBB(.25, .25, .25, .75, .75, .75)
  }

  override def getSelectedBoundingBox(state: IBlockState, worldIn: World, pos: BlockPos): AxisAlignedBB = {
    renderBox.offset(pos)
  }
}
