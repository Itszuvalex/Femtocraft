package com.itszuvalex.femtocraft.network.messages

import com.itszuvalex.femtocraft.cyber.tile.TileGrowthChamber
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.network.messages.MessageBase
import io.netty.buffer.ByteBuf
import net.minecraftforge.fml.common.network.simpleimpl.{IMessage, MessageContext}

/**
  * Created by Alex on 06.11.2015.
  */
class MessageGrowthChamberUpdate(var loc: Loc4, var progressTicks: Int) extends MessageBase[MessageGrowthChamberUpdate, IMessage] {
  def this() = this(Loc4(0, 0, 0, 0), 0)

  override def toBytes(buf: ByteBuf): Unit = {
    buf.writeInt(loc.x)
    buf.writeShort(loc.y)
    buf.writeInt(loc.z)
    buf.writeInt(loc.dim)
    buf.writeInt(progressTicks)
  }

  override def fromBytes(buf: ByteBuf): Unit = {
    loc = Loc4(buf.readInt(), buf.readShort(), buf.readInt(), buf.readInt())
    progressTicks = buf.readInt()
  }

  override def onMessage(message: MessageGrowthChamberUpdate, ctx: MessageContext): IMessage = {
    message.loc.getTileEntity() match {
      case Some(te: TileGrowthChamber) =>
        te.progressTicks = message.progressTicks
        if (te.currentRecipe != null) te.progress = math.floor((te.progressTicks * 100) / te.currentRecipe.ticks.toDouble).toInt
      case _ =>
    }
    null
  }

}
