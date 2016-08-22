package com.itszuvalex.femtocraft.player

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumFacing
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
  * Created by Chris on 8/21/2016.
  */
class PlayerEventHandler {
  @SubscribeEvent
  def handlePlayerJoin(event: EntityJoinWorldEvent): Unit = {
    if (event.isCanceled) return
    if (event.getWorld.isRemote) return

    event.getEntity match {
      case player: EntityPlayer => player.getCapability(PlayerNaniteCapabilities.NANITE_CAPABILITY, EnumFacing.NORTH).sync()
      case _ =>
    }
  }

}
