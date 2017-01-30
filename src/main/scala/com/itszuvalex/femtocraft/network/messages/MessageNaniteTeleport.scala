package com.itszuvalex.femtocraft.network.messages

import java.util.Random

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.proxy.ProxyCommon
import com.itszuvalex.itszulib.ItszuLib
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.render.Vector3
import com.itszuvalex.itszulib.util.Color
import io.netty.buffer.ByteBuf
import net.minecraft.world.World
import net.minecraftforge.fml.common.network.simpleimpl.{IMessage, IMessageHandler, MessageContext}

/**
  * Created by Chris on 1/12/2017.
  */
class MessageNaniteTeleport(var x: Int, var y: Int, var z: Int, var dim: Int, var endX: Int, var endY: Int, var endZ: Int) extends IMessage with IMessageHandler[MessageNaniteTeleport, IMessage] {
  def this() = this(0, 0, 0, 0, 0, 0, 0)

  override def toBytes(buf: ByteBuf): Unit = {
    buf.writeInt(x)
    buf.writeInt(y)
    buf.writeInt(z)
    buf.writeInt(dim)
    buf.writeInt(endX)
    buf.writeInt(endY)
    buf.writeInt(endZ)
  }

  override def fromBytes(buf: ByteBuf): Unit = {
    x = buf.readInt()
    y = buf.readInt()
    z = buf.readInt()
    dim = buf.readInt()
    endX = buf.readInt()
    endY = buf.readInt()
    endZ = buf.readInt()
  }

  override def onMessage(message: MessageNaniteTeleport, ctx: MessageContext): IMessage = {
    val start = Loc4(message.x, message.y, message.z, message.dim)
    val end = Loc4(message.endX, message.endY, message.endZ, message.dim)
    val world = start.getWorld.get
    ItszuLib.proxy.addScheduledTask(() => {
      spawnParticles(world, start)
      spawnParticles(world, end)
      spawnTransitionParticles(world, start, end)
    })
    null
  }

  private def spawnParticles(world: World, loc: Loc4): Unit = {
    val random = new Random()
    (0 until 10).foreach { i =>
      val xRand = random.nextDouble()
      val yRand = random.nextDouble() * 2
      val zRand = random.nextDouble()
      val color = Color(255.toByte, (random.nextFloat() * 155 + 100).toByte, (random.nextFloat() * 155 + 100).toByte, (random.nextFloat() * 155 + 100).toByte)

      Femtocraft.proxy.spawnParticle(world, ProxyCommon.PARTICLE_NANITE, loc.x + xRand, loc.y + yRand, loc.z + zRand, color.toInt)
    }
  }

  private def spawnTransitionParticles(world: World, start: Loc4, end: Loc4): Unit = {
    val random = new Random()
    (0 until 20).foreach { i =>
      val xRand = random.nextDouble()
      val yRand = random.nextDouble() * 2
      val zRand = random.nextDouble()
      val color = Color(255.toByte, (random.nextFloat() * 155 + 100).toByte, (random.nextFloat() * 155 + 100).toByte, (random.nextFloat() * 155 + 100).toByte)

      val dist = Vector3(end.x, end.y, end.z) - Vector3(start.x, start.y, start.z)

      val timeInTicks = 20
      val speed = dist / timeInTicks

      Femtocraft.proxy.spawnParticle(world, ProxyCommon.PARTICLE_NANITE, start.x + xRand, start.y + yRand, start.z + zRand, color.toInt, speed.x, speed.y, speed.z
      )
    }
  }
}
