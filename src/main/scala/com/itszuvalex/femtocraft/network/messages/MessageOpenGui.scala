package com.itszuvalex.femtocraft.network.messages

import com.itszuvalex.femtocraft.Femtocraft
import cpw.mods.fml.common.network.simpleimpl.{IMessage, IMessageHandler, MessageContext}
import cpw.mods.fml.relauncher.Side
import io.netty.buffer.ByteBuf
import net.minecraftforge.common.DimensionManager

/**
  * Created by Christopher on 12/12/2015.
  */
class MessageOpenGui(var x: Int, var y: Int, var z: Int, var dim: Int, var guiID: Int) extends IMessage with IMessageHandler[MessageOpenGui, IMessage] {
  def this() = this(0, 0, 0, 0, 0)

  override def toBytes(buf: ByteBuf): Unit = {
    buf.writeInt(x)
    buf.writeShort(y)
    buf.writeInt(z)
    buf.writeInt(dim)
    buf.writeInt(guiID)
  }

  override def fromBytes(buf: ByteBuf): Unit = {
    x = buf.readInt()
    y = buf.readShort()
    z = buf.readInt()
    dim = buf.readInt()
    guiID = buf.readInt()
  }

  override def onMessage(message: MessageOpenGui, ctx: MessageContext): IMessage = {
    if (ctx.side == Side.SERVER)
      ctx.getServerHandler.playerEntity.openGui(Femtocraft, message.guiID, DimensionManager.getWorld(message.dim), message.x, message.y, message.z)
    null
  }
}
