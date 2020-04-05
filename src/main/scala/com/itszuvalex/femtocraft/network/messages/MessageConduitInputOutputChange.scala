package com.itszuvalex.femtocraft.network.messages

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.api.logistics.ConnectionDirection
import com.itszuvalex.femtocraft.logistics.tile.TileConduit
import com.itszuvalex.itszulib.ItszuLib
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.network.messages.MessageBase
import io.netty.buffer.ByteBuf
import net.minecraft.util.EnumFacing
import net.minecraftforge.fml.common.network.simpleimpl.{IMessage, MessageContext}

class MessageConduitInputOutputChange(var tile: ITileEntity, var side: EnumFacing, var index: Int, var forward: Boolean) extends MessageBase[MessageConduitInputOutputChange, IMessage] {
  def this() = this(null, null, 0, false)

  override def toBytes(buf: ByteBuf): Unit = {
    var tloc = loc
    buf.writeInt(tloc.x)
    buf.writeInt(tloc.y)
    buf.writeInt(tloc.z)
    buf.writeInt(tloc.dim)
    buf.writeInt(side.ordinal())
    buf.writeInt(index)
    buf.writeBoolean(forward)
  }

  def loc: Loc4 = Loc4(tile)

  override def fromBytes(buf: ByteBuf): Unit = {
    val x    = buf.readInt()
    val y    = buf.readInt()
    val z    = buf.readInt()
    val dim  = buf.readInt()
    val tloc = Loc4(x, y, z, dim)
    tile = tloc.getITileEntity(false).orNull
    side = EnumFacing.values()(buf.readInt())
    index = buf.readInt()
    forward = buf.readBoolean()
  }

  override def onMessage(message: MessageConduitInputOutputChange, ctx: MessageContext): IMessage = {
    if (message.tile == null) return null
    if (!message.tile.isInstanceOf[TileConduit]) return null

    val mtile = message.tile.asInstanceOf[TileConduit]

    ItszuLib.proxy.addScheduledTask(() => {
      val item = mtile.getStorage(message.side).apply(message.index)
      if (!item.isEmpty && item.hasModule(ManagerModules.ITEM_CONNECTION_PROVIDER, null)) {
        val capability = item.getModule(ManagerModules.ITEM_CONNECTION_PROVIDER, null)
        val iter       = capability.getConnections(message.loc, message.side).iterator()
        if (iter.hasNext) {
          val connection = iter.next()
          val direction  = connection.direction
          val offset     = if (message.forward) 1 else -1
          connection.setDirection(ConnectionDirection.values().apply(
            (ConnectionDirection.values().indexOf(direction) + offset + ConnectionDirection.values().length) % ConnectionDirection.values().length))
        }
      }

      message.tile.markDirtyForSave()
    })
    null
  }
}
