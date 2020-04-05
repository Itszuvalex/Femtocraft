package com.itszuvalex.femtocraft.power.block

import java.util
import java.util.Random

import com.itszuvalex.femtocraft.power.tile.TileCrystalMount
import com.itszuvalex.femtocraft.proxy.ProxyCommon
import com.itszuvalex.femtocraft.{FemtoBlocks, Femtocraft}
import com.itszuvalex.itszulib.api.ItszuLibCapabilities
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.{ITileEntity, IWorld}
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviors
import com.itszuvalex.itszulib.core.{BlockTileContainer, TileBlockContainerCore}
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.{AxisAlignedBB, BlockPos, RayTraceResult, Vec3d}
import net.minecraft.world.{IBlockAccess, World}

import scala.collection.JavaConversions._

/**
  * Created by Christopher on 8/30/2015.
  */
class BlockCrystalMount extends TileBlockContainerCore(Material.IRON, new BlockCrystalMountContainerDelegate(), BlockBehaviors.DEFAULT) {
  var renderBox = new AxisAlignedBB(.4, .3, .4, .6, .7, .6)
  setCreativeTab(Femtocraft.tab)

  override def isNormalCube(state: IBlockState, world: IBlockAccess, pos: BlockPos): Boolean = false

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def randomDisplayTick(stateIn: IBlockState, worldIn: World, pos: BlockPos, rand: Random): Unit = {
    worldIn.getTileEntity(pos) match {
      case mount: TileCrystalMount =>
        if (mount.storage.head != null && !mount.storage.head.isEmpty)
          Femtocraft.proxy.spawnParticle(worldIn, ProxyCommon.PARTICLE_POWER,
                                         pos.getX + .5 + (rand.nextDouble() * .2 - .1),
                                         pos.getY + .5 + (rand.nextDouble() * .2 - .1),
                                         pos.getZ + .5 + (rand.nextDouble() * .2 - .1),
                                         mount.getCapability(ItszuLibCapabilities.COLORABLE, null).toInt)
      case _ =>
    }
  }

  override def addCollisionBoxToList(state: IBlockState, worldIn: World, pos: BlockPos, entityBox: AxisAlignedBB, collidingBoxes: util.List[AxisAlignedBB], entityIn: Entity, p_185477_7_ : Boolean): Unit = {
    super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, p_185477_7_)

    getBoundingBoxes(worldIn, pos).withFilter(entityBox.intersects).foreach(collidingBoxes.add)
  }

  def getBoundingBoxes(worldIn: World, pos: BlockPos): util.List[AxisAlignedBB] = {
    val list = new util.ArrayList[AxisAlignedBB]()
    list += new AxisAlignedBB(.4, .3, .4, .6, .7, .6)
    if (renderAbove(worldIn, pos)) list += new AxisAlignedBB(2f / 16f, .6, 2f / 16f, 14f / 16f, 1, 14f / 16f)
    if (renderBelow(worldIn, pos) || !renderAbove(worldIn, pos)) list += new AxisAlignedBB(2f / 16f, 0, 2f / 16f, 14f / 16f, .4, 14f / 16f)
    list
  }

  def renderAbove(worldIn: World, pos: BlockPos): Boolean = {
    val loc        = Loc4(worldIn, pos)
    val stateAbove = worldIn.getBlockState(loc.getOffset(EnumFacing.UP).getPos)
    stateAbove.getBlock.isSideSolid(stateAbove, worldIn, loc.getOffset(EnumFacing.UP).getPos, EnumFacing.DOWN)
  }

  def renderBelow(worldIn: World, pos: BlockPos): Boolean = {
    val loc        = Loc4(worldIn, pos)
    val stateAbove = worldIn.getBlockState(loc.getOffset(EnumFacing.DOWN).getPos)
    stateAbove.getBlock.isSideSolid(stateAbove, worldIn, loc.getOffset(EnumFacing.DOWN).getPos, EnumFacing.UP)
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

  override protected def rayTrace(pos: BlockPos, start: Vec3d, end: Vec3d, boundingBox: AxisAlignedBB): RayTraceResult = {
    val vec3d         : Vec3d          = start.subtract(pos.getX.toDouble, pos.getY.toDouble, pos.getZ.toDouble)
    val vec3d1        : Vec3d          = end.subtract(pos.getX.toDouble, pos.getY.toDouble, pos.getZ.toDouble)
    val raytraceresult: RayTraceResult = boundingBox.calculateIntercept(vec3d, vec3d1)
    if (raytraceresult == null) null
    else new RayTraceResult(raytraceresult.hitVec.addVector(pos.getX.toDouble, pos.getY.toDouble, pos.getZ.toDouble), raytraceresult.sideHit, pos)
  }

  override def getBoundingBox(state: IBlockState, source: IBlockAccess, pos: BlockPos): AxisAlignedBB = {
    new AxisAlignedBB(.4, .3, .4, .6, .7, .6)
  }

  override def getSelectedBoundingBox(state: IBlockState, worldIn: World, pos: BlockPos): AxisAlignedBB = {
    renderBox.offset(pos)
  }
}

class BlockCrystalMountContainerDelegate() extends BlockTileContainer(FemtoBlocks.blockCrystalMount _) {
  override def createTileEntity(world: IWorld, meta: Int): ITileEntity = new TileCrystalMount
}

