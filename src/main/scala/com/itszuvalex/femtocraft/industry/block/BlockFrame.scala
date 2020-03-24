package com.itszuvalex.femtocraft.industry.block

import java.util.Random

import com.itszuvalex.femtocraft.industry.tile.TileFrame
import com.itszuvalex.femtocraft.proxy.ProxyCommon
import com.itszuvalex.femtocraft.{FemtoBlocks, FemtoItems, Femtocraft}
import com.itszuvalex.itszulib.api.wrappers.{ITileEntity, IWorld}
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviors
import com.itszuvalex.itszulib.core.{BlockTileContainer, TileBlockContainerCore}
import com.itszuvalex.itszulib.util.Color
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.math.{BlockPos, RayTraceResult}
import net.minecraft.world.{IBlockAccess, World}

/**
  * Created by Christopher on 8/27/2015.
  */
class BlockFrame extends TileBlockContainerCore(Material.IRON, new BlockFrameContainerDelegate(), BlockBehaviors.DEFAULT) {

  override def getPickBlock(state: IBlockState, target: RayTraceResult, world: World, pos: BlockPos, player: EntityPlayer): ItemStack = new ItemStack(FemtoItems.itemFrame)

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def isNormalCube(state: IBlockState, world: IBlockAccess, pos: BlockPos): Boolean = false

  override def randomDisplayTick(stateIn: IBlockState, worldIn: World, pos: BlockPos, rand: Random): Unit = {
    worldIn.getTileEntity(pos) match {
      case null =>
      case i: TileFrame if i.isCurrentlyBuilding =>
        if (worldIn.isRemote)
          if (rand.nextInt(3) == 1)
            (0 until 1).foreach { _ =>
              val px    = pos.getX + rand.nextFloat()
              val py    = pos.getY + rand.nextFloat()
              val pz    = pos.getZ + rand.nextFloat()
              val half  = 255f / 2f
              val color = new Color(0, (rand.nextFloat() * half + half).toByte, (rand.nextFloat() * half + half).toByte, (rand.nextFloat() * half + half).toByte)
              Femtocraft.proxy.spawnParticle(worldIn, ProxyCommon.PARTICLE_NANITE, px, py, pz, color.toInt);
            }
      case _ =>
    }
  }
}

class BlockFrameContainerDelegate() extends BlockTileContainer(() => FemtoBlocks.blockFrame) {

  override def createTileEntity(world: IWorld, meta: Int): ITileEntity = new TileFrame()
}
