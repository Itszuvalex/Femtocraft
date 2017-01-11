package com.itszuvalex.femtocraft.industry.item

import com.itszuvalex.itszulib.render.Vector3
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{ActionResult, EnumHand}
import net.minecraft.world.World

/**
  * Created by Chris on 1/10/2017.
  */
class ItemShiftTest extends Item {
  override def onItemRightClick(world: World, player: EntityPlayer, hand: EnumHand): ActionResult[ItemStack] = {
    if (!world.isRemote) {
      val look = player.getLookVec
      val vec = Vector3(look.xCoord, look.yCoord, look.zCoord).normalize()
      getDestination(world, player, vec, 8d) match {
        case Some(a) => player.setPositionAndUpdate(a.getX, a.getY, a.getZ)
        case None =>
      }
      player.fallDistance = 0
    }

    super.onItemRightClick(world, player, hand)
  }

  def getDestination(world: World, player: EntityPlayer, dir: Vector3, dist: Double): Option[BlockPos] = {
    var lastX = -1
    var lastY = -1
    var lastZ = -1
    val found = (dist to(0, -.25d)).exists { step =>
      val x = (player.posX + dir.x * step + .5d).toInt
      val y = Math.max((player.posY + dir.y * step + .5d).toInt, 1)
      val z = (player.posZ + dir.z * step + .5d).toInt

      if (lastX == x && lastY == y && lastZ == z) false
      else {
        lastX = x
        lastY = y
        lastZ = z

        if (player.getEntityBoundingBox != null)
          (0 until Math.abs((player.getEntityBoundingBox.maxY - player.getEntityBoundingBox.minY).toInt)).forall(yOffset =>
            world.getBlockState(new BlockPos(x, y + yOffset, z)).getBlock.isPassable(world, new BlockPos(x, y + yOffset, z)))
        else true
      }
    }
    if (found && !(lastX == (player.posX + .5d).toInt && lastY == (player.posY + .5d).toInt && lastZ == (player.posZ + 5d).toInt)) Some(new BlockPos(lastX, lastY, lastZ))
    else None
  }

}
