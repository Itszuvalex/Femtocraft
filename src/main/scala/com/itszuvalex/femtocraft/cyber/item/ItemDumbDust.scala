package com.itszuvalex.femtocraft.cyber.item

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.cyber.CybermaterialRegistry
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.world.World

/**
  * Created by Christopher Harris (Itszuvalex) on 1/19/2016.
  */
class ItemDumbDust extends Item {
  setCreativeTab(Femtocraft.tab)

  override def onItemUse(itemStack: ItemStack, player: EntityPlayer, world: World, x: Int, y: Int, z: Int, side: Int, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    if (world.isAirBlock(x, y, z)) return false
    CybermaterialRegistry.getReplacement(world.getBlock(x, y, z), world.getBlockMetadata(x, y, z)) match {
      case Some((rblock, rdamage)) =>
        world.setBlock(x, y, z, rblock)
        world.setBlockMetadataWithNotify(x, y, z, rdamage, 3)
        itemStack.stackSize -= 1
        /*        if (world.isRemote) {
                  val random = new Random()
                  (0 until 4).foreach { i =>
                    val rbyte = (255f / 2f * random.nextFloat() + 255f / 2f).toByte
                    val gbyte = (255f / 2f * random.nextFloat() + 255f / 2f).toByte
                    val bbyte = (255f / 2f * random.nextFloat() + 255f / 2f).toByte
                    val offx = random.nextFloat() - .5f
                    val offy = random.nextFloat() - .5f
                    val offz = random.nextFloat() - .5f
                    val px = (x + player.posX) /2
                    val py = (y + player.posY) /2
                    val pz = (z + player.posZ) /2
                    Femtocraft.proxy.spawnParticle(world, ProxyCommon.PARTICLE_NANITE, px + offx, py + offy, pz + offz, new Color(255.toByte, rbyte, gbyte, bbyte).toInt)
                                      }
                } */
        true
      case None => false
    }
  }
}
