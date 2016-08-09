package com.itszuvalex.femtocraft.industry.block

import java.util.Random

import com.itszuvalex.femtocraft.industry.tile.TileFrame
import com.itszuvalex.femtocraft.proxy.ProxyCommon
import com.itszuvalex.femtocraft.{FemtoItems, Femtocraft}
import com.itszuvalex.itszulib.core.TileContainer
import com.itszuvalex.itszulib.util.Color
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.{BlockPos, RayTraceResult}
import net.minecraft.world.World

/**
  * Created by Christopher on 8/27/2015.
  */
class BlockFrame extends TileContainer(Material.IRON) {
  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TileFrame()

  override def getPickBlock(state: IBlockState, target: RayTraceResult, world: World, pos: BlockPos, player: EntityPlayer): ItemStack = new ItemStack(FemtoItems.itemFrame)

  override def randomDisplayTick(stateIn: IBlockState, worldIn: World, pos: BlockPos, rand: Random): Unit = {
    worldIn.getTileEntity(pos) match {
      case null =>
      case i: TileFrame if i.isCurrentlyBuilding =>
        if (worldIn.isRemote)
          if (rand.nextInt(3) == 1)
            (0 until 1).foreach { _ =>
              val px = pos.getX + rand.nextFloat()
              val py = pos.getY + rand.nextFloat()
              val pz = pos.getZ + rand.nextFloat()
              val half = 255f / 2f
              val color = new Color(0, (rand.nextFloat() * half + half).toByte, (rand.nextFloat() * half + half).toByte, (rand.nextFloat() * half + half).toByte)
              Femtocraft.proxy.spawnParticle(worldIn, ProxyCommon.PARTICLE_NANITE, px, py, pz, color.toInt);
            }
      case _ =>
    }
  }
}
