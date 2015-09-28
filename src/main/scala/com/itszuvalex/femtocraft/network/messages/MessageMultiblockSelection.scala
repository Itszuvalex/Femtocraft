package com.itszuvalex.femtocraft.network.messages

import com.itszuvalex.femtocraft.core.Industry.IFrameItem
import cpw.mods.fml.common.network.simpleimpl.{IMessage, IMessageHandler, MessageContext}
import io.netty.buffer.ByteBuf

/**
 * Created by Christopher on 9/19/2015.
 */
class MessageMultiblockSelection(var multi: String) extends IMessage with IMessageHandler[MessageMultiblockSelection, IMessage] {
  def this() = this(null)

  override def toBytes(buf: ByteBuf): Unit = {
    buf.writeInt(if (multi == null || multi.isEmpty) 0 else multi.length)
    if (multi != null && !multi.isEmpty) buf.writeBytes(multi.getBytes)
  }

  override def fromBytes(buf: ByteBuf): Unit = {
    val length = buf.readInt()
    multi = if (length > 0) {
      val bytes = new Array[Byte](length)
      buf.readBytes(bytes)
      new String(bytes)
    }
    else ""
  }

  override def onMessage(message: MessageMultiblockSelection, ctx: MessageContext): IMessage = {
    val player = ctx.getServerHandler.playerEntity
    player.getHeldItem match {
      case null  =>
      case stack =>
        stack.getItem match {
          case null             =>
          case item: IFrameItem =>
            item.setSelectedMultiblock(stack, message.multi)
            player.inventory.markDirty()
          case _                =>
        }
    }
    null
  }
}
