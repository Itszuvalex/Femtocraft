package com.itszuvalex.femtocraft.player

import com.itszuvalex.femtocraft.api.Capabilities
import net.minecraft.util.EnumFacing
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent.{PlayerLoggedInEvent, PlayerRespawnEvent}

/**
  * Created by Chris on 8/21/2016.
  */
class PlayerEventHandler {
  @SubscribeEvent
  def handlePlayerJoin(event: PlayerLoggedInEvent): Unit = {
    if (event.player.world.isRemote) return
    event.player.getCapability(Capabilities.PLAYER_NANITE_CAPABILITY, EnumFacing.NORTH).sync()
  }

  @SubscribeEvent
  def handlePlayerRespawn(event: PlayerRespawnEvent): Unit = {
    if (event.player.world.isRemote) return
    event.player.getCapability(Capabilities.PLAYER_NANITE_CAPABILITY, EnumFacing.NORTH).sync()
  }
}
