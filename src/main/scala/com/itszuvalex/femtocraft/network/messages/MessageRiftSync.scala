package com.itszuvalex.femtocraft.network.messages

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.itszuvalex.femtocraft.api.worldgen.IRift
import com.itszuvalex.femtocraft.worldgen.{FemtocraftRiftTracker, Rift}
import com.itszuvalex.itszulib.ItszuLib
import com.itszuvalex.itszulib.api.core.Loc4
import io.netty.buffer.ByteBuf
import net.minecraft.nbt.{CompressedStreamTools, NBTTagCompound}
import net.minecraftforge.fml.common.network.simpleimpl.{IMessage, IMessageHandler, MessageContext}

/**
  * Created by Christopher on 12/12/2015.
  */
class MessageRiftSync(var x: Int, var y: Int, var z: Int, var dim: Int, var nbt: NBTTagCompound) extends IMessage with IMessageHandler[MessageRiftSync, IMessage] {
  def this() = this(0, 0, 0, 0, new NBTTagCompound)

  def this(rift: IRift) = this(rift.location.x, rift.location.y, rift.location.z, rift.location.dim, rift.serializeNBT())

  override def toBytes(buf: ByteBuf): Unit = {
    buf.writeInt(x)
    buf.writeShort(y)
    buf.writeInt(z)
    buf.writeInt(dim)

    if (nbt == null) {
      buf.writeShort(-1)
    }
    else {
      val stream = new ByteArrayOutputStream()
      CompressedStreamTools.writeCompressed(nbt, stream)
      val abyte: Array[Byte] = stream.toByteArray
      buf.writeShort(abyte.length.toShort)
      buf.writeBytes(abyte)
    }
  }

  override def fromBytes(buf: ByteBuf): Unit = {
    x = buf.readInt()
    y = buf.readShort()
    z = buf.readInt()
    dim = buf.readInt()

    val short1: Int = buf.readShort

    if (short1 < 0) {
      nbt = null
    }
    else {
      val abyte: Array[Byte] = new Array[Byte](short1)
      buf.readBytes(abyte)
      nbt = CompressedStreamTools.readCompressed(new ByteArrayInputStream(abyte))
    }
  }

  override def onMessage(message: MessageRiftSync, ctx: MessageContext): IMessage = {
    ItszuLib.proxy.addScheduledTask(() => {
      val loc = Loc4(message.x, message.y, message.z, message.dim)

      FemtocraftRiftTracker.instance.rifts.get(loc) match {
        case None =>
          val rift = new Rift(loc)
          rift.deserializeNBT(message.nbt)
          FemtocraftRiftTracker.instance.registerRift(rift)
        case Some(rift) => rift.deserializeNBT(message.nbt)
      }
    })
    null
  }
}
