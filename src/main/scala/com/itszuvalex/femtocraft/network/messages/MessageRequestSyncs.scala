package com.itszuvalex.femtocraft.network.messages

import com.itszuvalex.itszulib.ItszuLib
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.container.ContainerBase
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.util.Debug
import io.netty.buffer.ByteBuf
import net.minecraftforge.fml.common.network.simpleimpl.{IMessage, IMessageHandler, MessageContext}
import org.apache.logging.log4j.Level

/**
  * Created by Christopher on 12/12/2015.
  */
class MessageRequestSyncs(var x: Int, var y: Int, var z: Int, var dim: Int) extends IMessage with IMessageHandler[MessageRequestSyncs, IMessage] {
  def this() = this(0, 0, 0, 0)

  def this(loc: Loc4, guiID: Int) = this(loc.x, loc.y, loc.z, loc.dim)

  def this(tile: TileEntityBase, guiID: Int) = this(tile.getLoc, guiID)

  override def toBytes(buf: ByteBuf): Unit = {
    buf.writeInt(x)
    buf.writeShort(y)
    buf.writeInt(z)
    buf.writeInt(dim)
  }

  override def fromBytes(buf: ByteBuf): Unit = {
    x = buf.readInt()
    y = buf.readShort()
    z = buf.readInt()
    dim = buf.readInt()
  }

  override def onMessage(message: MessageRequestSyncs, ctx: MessageContext): IMessage = {
    ItszuLib.proxy.addScheduledTask(() => {
      ctx.getServerHandler.playerEntity.openContainer match {
        case a: ContainerBase =>
          Debug.log(Level.WARN, "Received Sync Request")
          a.syncs.foreach(_.sync(ctx.getServerHandler.playerEntity))
        case _ =>
      }
    })
    null
  }
}
