package com.itszuvalex.femtocraft.network.messages

import com.itszuvalex.femtocraft.cyber.tile.TileGrowthChamber
import cpw.mods.fml.common.network.simpleimpl.{IMessage, IMessageHandler, MessageContext}
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft

/**
  * Created by Alex on 06.11.2015.
  */
class MessageGrowthChamberUpdate(var x: Int, var y: Int, var z: Int, var progressTicks: Int) extends IMessage with IMessageHandler[MessageGrowthChamberUpdate, IMessage] {
  def this() = this(0, 0, 0, 0)

  override def toBytes(buf: ByteBuf): Unit = {
    buf.writeInt(x)
    buf.writeShort(y)
    buf.writeInt(z)
    buf.writeInt(progressTicks)
  }

  override def fromBytes(buf: ByteBuf): Unit = {
    x = buf.readInt()
    y = buf.readShort()
    z = buf.readInt
    progressTicks = buf.readInt()
  }

  override def onMessage(message: MessageGrowthChamberUpdate, ctx: MessageContext): IMessage = {
    Minecraft.getMinecraft.theWorld.getTileEntity(message.x, message.y, message.z) match {
      case te: TileGrowthChamber =>
        te.progressTicks = message.progressTicks
        if (te.currentRecipe != null) te.progress = math.floor((te.progressTicks * 100) / te.currentRecipe.ticks.toDouble).toInt
      case _ =>
    }
    null
  }

}
