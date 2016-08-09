package com.itszuvalex.femtocraft.network.messages

import com.itszuvalex.femtocraft.cyber.tile.TileCyberBase
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.network.messages.MessageBase
import io.netty.buffer.ByteBuf
import net.minecraftforge.fml.common.network.simpleimpl.{IMessage, MessageContext}

/**
  * Created by Alex on 15.10.2015.
  */
class MessageBuildMachine(var loc: Loc4, var machine: String) extends MessageBase[MessageBuildMachine, IMessage] {
  lazy val worldObj = loc.getWorld.get

  def this() = this(Loc4(0, 0, 0, 0), null)

  override def toBytes(buf: ByteBuf): Unit = {
    buf.writeInt(loc.x)
    buf.writeShort(loc.y)
    buf.writeInt(loc.z)
    buf.writeInt(loc.dim)
    buf.writeInt(if (machine == null || machine.isEmpty) 0 else machine.length)
    if (machine != null && !machine.isEmpty) buf.writeBytes(machine.getBytes)
  }

  override def fromBytes(buf: ByteBuf): Unit = {
    loc = Loc4(buf.readInt(), buf.readShort(), buf.readInt(), buf.readInt())
    val length = buf.readInt()
    machine = if (length > 0) {
      val bytes = new Array[Byte](length)
      buf.readBytes(bytes)
      new String(bytes)
    }
    else ""
  }

  override def onMessage(message: MessageBuildMachine, ctx: MessageContext): IMessage = {
    message.loc.getTileEntity() match {
      case Some(tile: TileCyberBase) =>
        tile.buildMachine(message.machine)
      case _ =>
    }
    null
  }
}
