package com.itszuvalex.femtocraft.network.messages

import com.itszuvalex.femtocraft.cyber.tile.TileGrowthChamber
import cpw.mods.fml.common.network.simpleimpl.{MessageContext, IMessageHandler, IMessage}
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft

/**
 * Created by Alex on 06.11.2015.
 */
class MessageGrowthChamberUpdate(var x: Int, var y: Int, var z: Int, var progress: Int) extends IMessage with IMessageHandler[MessageGrowthChamberUpdate, IMessage]  {
  def this() = this(0, 0, 0, 0)

  override def toBytes(buf: ByteBuf): Unit = {
    buf.writeInt(x)
    buf.writeShort(y)
    buf.writeInt(z)
    buf.writeByte(progress)
  }

  override def fromBytes(buf: ByteBuf): Unit = {
    x = buf.readInt()
    y = buf.readShort()
    z = buf.readInt
    progress = buf.readByte()
  }

  override def onMessage(message: MessageGrowthChamberUpdate, ctx: MessageContext): IMessage = {
    Minecraft.getMinecraft.theWorld.getTileEntity(message.x, message.y, message.z) match {
      case te: TileGrowthChamber =>
        te.progress = message.progress
    }
    null
  }

}
