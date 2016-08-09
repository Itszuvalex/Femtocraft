package com.itszuvalex.femtocraft.cyber.item

import java.util.Random

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.cyber.CybermaterialRegistry
import com.itszuvalex.femtocraft.proxy.ProxyCommon
import com.itszuvalex.itszulib.util.Color
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{EnumActionResult, EnumFacing, EnumHand}
import net.minecraft.world.World

/**
  * Created by Christopher Harris (Itszuvalex) on 1/19/2016.
  */
class ItemDumbDust extends Item {
  setCreativeTab(Femtocraft.tab)

  override def onItemUse(stack: ItemStack, playerIn: EntityPlayer, worldIn: World, pos: BlockPos, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): EnumActionResult = {
    if (worldIn.isAirBlock(pos)) return EnumActionResult.FAIL
    CybermaterialRegistry.getReplacement(worldIn.getBlockState(pos).getBlock, worldIn.getBlockState(pos).getBlock.getMetaFromState(worldIn.getBlockState(pos))) match {
      case Some((rblock, rdamage)) =>
        worldIn.setBlockState(pos, rblock.getStateFromMeta(rdamage))
        stack.stackSize -= 1
        if (worldIn.isRemote) {
          val random = new Random()
          (0 until 4).foreach { i =>
            val rbyte = (255f / 2f * random.nextFloat() + 255f / 2f).toByte
            val gbyte = (255f / 2f * random.nextFloat() + 255f / 2f).toByte
            val bbyte = (255f / 2f * random.nextFloat() + 255f / 2f).toByte
            val offx = random.nextFloat() - .5f
            val offy = random.nextFloat() - .5f
            val offz = random.nextFloat() - .5f
            val px = (pos.getX + playerIn.posX) / 2
            val py = (pos.getY + playerIn.posY) / 2
            val pz = (pos.getZ + playerIn.posZ) / 2
            Femtocraft.proxy.spawnParticle(worldIn, ProxyCommon.PARTICLE_NANITE, px + offx, py + offy, pz + offz, new Color(255.toByte, rbyte, gbyte, bbyte).toInt)
          }
        }
        EnumActionResult.SUCCESS
      case None => EnumActionResult.FAIL
    }
  }
}
