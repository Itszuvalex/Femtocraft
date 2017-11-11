package com.itszuvalex.femtocraft.network.messages

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.network.messages.MessageBase
import io.netty.buffer.ByteBuf
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraftforge.fml.common.network.simpleimpl.{IMessage, MessageContext}

class MessageSidedInventoryConfigChange(var tile: TileEntity, var side: EnumFacing, var forward: Boolean) extends MessageBase[MessageSidedInventoryConfigChange, IMessage] {
  def loc: Loc4 = new Loc4(tile)

  def this() = this(null, null, false)

  override def toBytes(buf: ByteBuf): Unit = {
    var tloc = loc
    buf.writeInt(tloc.x)
    buf.writeInt(tloc.y)
    buf.writeInt(tloc.z)
    buf.writeInt(tloc.dim)
    buf.writeInt(side.ordinal())
    buf.writeBoolean(forward)
  }

  override def fromBytes(buf: ByteBuf): Unit = {
    val x = buf.readInt()
    val y = buf.readInt()
    val z = buf.readInt()
    val dim = buf.readInt()
    val tloc = Loc4(x, y, z, dim)
    tile = tloc.getTileEntity(false).orNull
    side = EnumFacing.values()(buf.readInt())
    forward = buf.readBoolean()
  }

  override def onMessage(message: MessageSidedInventoryConfigChange, ctx: MessageContext): IMessage = {
    if (message.tile == null) return null
    if (!message.tile.hasCapability(Capabilities.ITEM_STORAGE_CONFIGURABLE, message.side)) return null

    val cap = message.tile.getCapability(Capabilities.ITEM_STORAGE_CONFIGURABLE, message.side)
    if (cap == null) return null

    if (message.forward)
      cap.cycleRelativeFacingForward(message.side)
    else
      cap.cycleRelativeFacingBackward(message.side)

    message.tile.markDirty()
    null
  }
}
