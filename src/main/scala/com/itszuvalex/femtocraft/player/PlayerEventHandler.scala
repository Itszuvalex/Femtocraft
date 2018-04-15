package com.itszuvalex.femtocraft.player

import com.itszuvalex.femtocraft.FemtoItems
import com.itszuvalex.femtocraft.api.Capabilities
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock
import net.minecraftforge.fml.common.eventhandler.{Event, SubscribeEvent}
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

  @SubscribeEvent
  def handleRightClick(event: RightClickBlock): Unit = {
    event.getEntityPlayer.getHeldItemMainhand match {
      case null =>
      case a if a.isEmpty =>
      case n if n.isItemEqual(new ItemStack(FemtoItems.itemConfigurator)) => event.setUseBlock(Event.Result.DENY)
      case _ =>
    }
  }
}
