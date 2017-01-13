package com.itszuvalex.femtocraft.network.messages

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.proxy.ProxyCommon
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.util.Color
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.common.network.simpleimpl.{IMessage, IMessageHandler, MessageContext}

import scala.util.Random

/**
  * Created by Chris on 1/12/2017.
  */
class MessageNaniteStorm(var x: Int, var y: Int, var z: Int, var dim: Int) extends IMessage with IMessageHandler[MessageNaniteStorm, IMessage] {
  def this() = this(0, 0, 0, 0)

  override def toBytes(buf: ByteBuf): Unit = {
    buf.writeInt(x)
    buf.writeInt(y)
    buf.writeInt(z)
    buf.writeInt(dim)
  }

  override def fromBytes(buf: ByteBuf): Unit = {
    x = buf.readInt()
    y = buf.readInt()
    z = buf.readInt()
    dim = buf.readInt()
  }

  override def onMessage(message: MessageNaniteStorm, ctx: MessageContext): IMessage = {
    val loc = Loc4(message.x, message.y, message.z, message.dim)
    val random = new Random()
    (0 until 20).foreach { i =>
      val xRand = random.nextDouble()
      val yRand = random.nextDouble() * 2
      val zRand = random.nextDouble()
      val color = Color(255.toByte, (random.nextFloat() * 155 + 100).toByte, (random.nextFloat() * 155 + 100).toByte, (random.nextFloat() * 155 + 100).toByte)

      Femtocraft.proxy.spawnParticle(Minecraft.getMinecraft.theWorld, ProxyCommon.PARTICLE_NANITE, loc.x + xRand, loc.y + yRand, loc.z + zRand, color.toInt)
    }

    null
  }
}
