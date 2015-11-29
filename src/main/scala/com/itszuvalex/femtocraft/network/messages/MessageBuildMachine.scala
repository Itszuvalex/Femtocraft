package com.itszuvalex.femtocraft.network.messages

import com.itszuvalex.femtocraft.cyber.tile.TileCyberBase
import cpw.mods.fml.common.network.simpleimpl.{IMessage, IMessageHandler, MessageContext}
import io.netty.buffer.ByteBuf

/**
  * Created by Alex on 15.10.2015.
  */
class MessageBuildMachine(var x: Int, var y: Int, var z: Int, var machine: String) extends IMessage with IMessageHandler[MessageBuildMachine, IMessage] {
  def this() = this(0, 0, 0, null)

  override def toBytes(buf: ByteBuf): Unit = {
    buf.writeInt(x)
    buf.writeShort(y)
    buf.writeInt(z)
    buf.writeInt(if (machine == null || machine.isEmpty) 0 else machine.length)
    if (machine != null && !machine.isEmpty) buf.writeBytes(machine.getBytes)
  }

  override def fromBytes(buf: ByteBuf): Unit = {
    x = buf.readInt()
    y = buf.readShort()
    z = buf.readInt()
    val length = buf.readInt()
    machine = if (length > 0) {
      val bytes = new Array[Byte](length)
      buf.readBytes(bytes)
      new String(bytes)
    }
    else ""
  }

  override def onMessage(message: MessageBuildMachine, ctx: MessageContext): IMessage = {
    val player = ctx.getServerHandler.playerEntity
    player.worldObj.getTileEntity(message.x, message.y, message.z) match {
      case tile: TileCyberBase =>
        tile.buildMachine(message.machine)
      case _ =>
    }
    null
  }
}
