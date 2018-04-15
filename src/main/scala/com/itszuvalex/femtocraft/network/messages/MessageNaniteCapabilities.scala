package com.itszuvalex.femtocraft.network.messages

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.player.{PlayerNaniteCapabilitiesOverlay, PlayerNaniteCapability}
import com.itszuvalex.itszulib.ItszuLib
import com.itszuvalex.itszulib.network.messages.MessageUpdateNBT
import net.minecraft.client.Minecraft
import net.minecraft.util.EnumFacing
import net.minecraftforge.fml.common.network.simpleimpl.{IMessage, MessageContext}

/**
  * Created by Christopher Harris (Itszuvalex) on 1/6/16.
  */
class MessageNaniteCapabilities(cap: PlayerNaniteCapability) extends MessageUpdateNBT[MessageNaniteCapabilities, IMessage](Option(cap).map(_.serializeNBT()).orNull) {
  def this() = this(null)

  override def onMessage(message: MessageNaniteCapabilities, ctx: MessageContext): IMessage = {
    val player = Minecraft.getMinecraft.player
    if (player == null) return null

    ItszuLib.proxy.addScheduledTask(() => {
      val cap = player.getCapability(Capabilities.PLAYER_NANITE_CAPABILITY, EnumFacing.NORTH).asInstanceOf[PlayerNaniteCapability]
      cap.deserializeNBT(message.nbt)
      PlayerNaniteCapabilitiesOverlay.interact()
    })
    null
  }
}
