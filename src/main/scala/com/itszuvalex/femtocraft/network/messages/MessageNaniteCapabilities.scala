package com.itszuvalex.femtocraft.network.messages

import com.itszuvalex.femtocraft.player.{PlayerNaniteCapabilities, PlayerNaniteCapabilitiesOverlay}
import com.itszuvalex.itszulib.network.messages.MessageUpdateNBT
import net.minecraft.client.Minecraft
import net.minecraft.util.EnumFacing
import net.minecraftforge.fml.common.network.simpleimpl.{IMessage, MessageContext}

/**
  * Created by Christopher Harris (Itszuvalex) on 1/6/16.
  */
class MessageNaniteCapabilities(cap: PlayerNaniteCapabilities) extends MessageUpdateNBT[MessageNaniteCapabilities, IMessage](Option(cap).map(_.serializeNBT()).orNull) {
  def this() = this(null)

  override def onMessage(message: MessageNaniteCapabilities, ctx: MessageContext): IMessage = {
    val player = Minecraft.getMinecraft.thePlayer
    if (player == null) return null

    val cap = player.getCapability(PlayerNaniteCapabilities.NANITE_CAPABILITY, EnumFacing.NORTH).asInstanceOf[PlayerNaniteCapabilities]
    cap.deserializeNBT(message.nbt)
    PlayerNaniteCapabilitiesOverlay.timeOfLastInteract = System.currentTimeMillis
    null
  }
}
