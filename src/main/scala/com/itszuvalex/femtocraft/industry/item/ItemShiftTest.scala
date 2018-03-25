package com.itszuvalex.femtocraft.industry.item

import com.itszuvalex.femtocraft.FemtoSounds
import com.itszuvalex.femtocraft.network.FemtoPacketHandler
import com.itszuvalex.femtocraft.network.messages.MessageNaniteTeleport
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.itszulib.api.IPreviewable
import com.itszuvalex.itszulib.render.Vector3
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{ActionResult, EnumFacing, EnumHand, SoundCategory}
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.{Capability, ICapabilityProvider}
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint
import net.minecraftforge.fml.relauncher.SideOnly

/**
  * Created by Chris on 1/10/2017.
  */
object ItemShiftTest {
  val COOLDOWN_TICKS: Int = 5 * 20
}

class ItemShiftTest extends Item {
  override def onItemRightClick(world: World, player: EntityPlayer, hand: EnumHand): ActionResult[ItemStack] = {
    if (!world.isRemote) {
      val look = player.getLookVec
      val vec = Vector3(look.x, look.y, look.z).normalize()
      getDestination(world, player, vec, 8d) match {
        case Some(a) =>
          val old = player.getPosition
          player.setPositionAndUpdate(a.getX + .5d, a.getY, a.getZ + .5d)
          world.playSound(null /* this is a filter player who won't hear sound */ , a.getX + .5d, a.getY + .5d, a.getZ + .5d, FemtoSounds.shiftSound, SoundCategory.PLAYERS, 1, 1)
          FemtoPacketHandler.INSTANCE.sendToAllAround(new MessageNaniteTeleport(old.getX, old.getY, old.getZ, world.provider.getDimension, a.getX, a.getY, a.getZ), new TargetPoint(world.provider.getDimension, a.getX, a.getY, a.getZ, 32f))
          player.getCooldownTracker.setCooldown(this, ItemShiftTest.COOLDOWN_TICKS)
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
      val x = (player.posX + (dir.x * step)).toInt
      val y = Math.max((player.posY + (dir.y * step) + player.eyeHeight).toInt, 1)
      val z = (player.posZ + (dir.z * step)).toInt

      if (lastX == x && lastY == y && lastZ == z) false
      else {
        lastX = x
        lastY = y
        lastZ = z

        if (player.getEntityBoundingBox != null)
          (0 to Math.abs((player.getEntityBoundingBox.maxY - player.getEntityBoundingBox.minY).toInt)).forall(yOffset =>
            world.getBlockState(new BlockPos(x, y + yOffset, z)).getBlock.isPassable(world, new BlockPos(x, y + yOffset, z)))
        else true
      }
    }
    if (found && !(lastX == (player.posX + .5d).toInt && lastY == (player.posY + .5d).toInt && lastZ == (player.posZ + 5d).toInt)) Some(new BlockPos(lastX, lastY, lastZ))
    else None
  }


  override def initCapabilities(stack: ItemStack, nbt: NBTTagCompound): ICapabilityProvider = {
    new ICapabilityProvider {
      override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
        if (capability == com.itszuvalex.itszulib.api.Capabilities.ITEM_PREVIEWABLE) {
          new IPreviewable {
            @SideOnly(value = net.minecraftforge.fml.relauncher.Side.CLIENT)
            override def renderID: Int = RenderIDs.itemShiftPreviewableID
          }.asInstanceOf[T]
        }
        else null.asInstanceOf[T]
      }

      override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = capability == com.itszuvalex.itszulib.api.Capabilities.ITEM_PREVIEWABLE
    }
  }
}
