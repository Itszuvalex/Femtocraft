package com.itszuvalex.femtocraft.industry.block

import java.util.Random

import com.itszuvalex.femtocraft.{Femtocraft, FemtoItems}
import com.itszuvalex.femtocraft.industry.tile.TileFrame
import com.itszuvalex.femtocraft.proxy.ProxyCommon
import com.itszuvalex.itszulib.core.TileContainer
import com.itszuvalex.itszulib.util.Color
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.MovingObjectPosition
import net.minecraft.world.World

/**
  * Created by Christopher on 8/27/2015.
  */
class BlockFrame extends TileContainer(Material.iron) {
  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TileFrame()

  override def getRenderBlockPass: Int = 2

  override def renderAsNormalBlock: Boolean = false

  override def isOpaqueCube: Boolean = false

  override def breakBlock(world: World, x: Int, y: Int, z: Int, block: Block, p_149749_6_ : Int): Unit = {
    world.getTileEntity(x, y, z) match {
      case frame: TileFrame => frame.onBlockBreak()
      case _ =>
    }
    super.breakBlock(world, x, y, z, block, p_149749_6_)
  }

  override def getPickBlock(target: MovingObjectPosition, world: World, x: Int, y: Int, z: Int, player: EntityPlayer): ItemStack = new ItemStack(FemtoItems.itemFrame)

  override def randomDisplayTick(world: World, x: Int, y: Int, z: Int, random: Random): Unit = {
    world.getTileEntity(x, y, z) match {
      case null =>
      case i: TileFrame if i.isCurrentlyBuilding =>
      if (world.isRemote)
        if (random.nextInt(3) == 1)
          (0 until 1).foreach { _ =>
            val px = x + random.nextFloat()
            val py = y + random.nextFloat()
            val pz = z + random.nextFloat()
            val half = 255f / 2f
            val color = new Color(0, (random.nextFloat() * half + half).toByte, (random.nextFloat() * half + half).toByte, (random.nextFloat() * half + half).toByte)
            Femtocraft.proxy.spawnParticle(world, ProxyCommon.PARTICLE_NANITE, px, py, pz, color.toInt);
                              }
      case _ =>
    }
  }
}
