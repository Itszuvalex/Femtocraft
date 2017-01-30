package com.itszuvalex.femtocraft.network.messages

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.itszulib.ItszuLib
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.core.TileEntityBase
import io.netty.buffer.ByteBuf
import net.minecraftforge.common.DimensionManager
import net.minecraftforge.fml.common.network.simpleimpl.{IMessage, IMessageHandler, MessageContext}

/**
  * Created by Christopher on 12/12/2015.
  */
class MessageOpenGui(var x: Int, var y: Int, var z: Int, var dim: Int, var guiID: Int) extends IMessage with IMessageHandler[MessageOpenGui, IMessage] {
  def this() = this(0, 0, 0, 0, 0)

  def this(loc: Loc4, guiID: Int) = this(loc.x, loc.y, loc.z, loc.dim, guiID)

  def this(tile: TileEntityBase, guiID: Int) = this(tile.getLoc, guiID)

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
    ItszuLib.proxy.addScheduledTask(() => {
      ctx.getServerHandler.playerEntity.openGui(Femtocraft, message.guiID, DimensionManager.getWorld(message.dim), message.x, message.y, message.z)
    })
    null
  }
}
