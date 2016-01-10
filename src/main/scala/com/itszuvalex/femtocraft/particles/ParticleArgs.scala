package com.itszuvalex.femtocraft.particles

import io.netty.buffer.ByteBuf
import net.minecraftforge.common.DimensionManager

/**
  * Created by Christopher Harris (Itszuvalex) on 1/6/16.
  */
class ParticleArgs(
                    var dimension: Int = 0,
                    var posX: Double = 0D,
                    var posY: Double = 0D,
                    var posZ: Double = 0D,
                    var scale: Float = 1f,
                    var motionX: Float = 0f,
                    var motionY: Float = 0f,
                    var motionZ: Float = 0f,
                    var accelX: Float = 0f,
                    var accelY: Float = 0f,
                    var accelZ: Float = 0f,
                    var alpha: Float = 0f,
                    var red: Float = 1f,
                    var green: Float = 1f,
                    var blue: Float = 1f,
                    var age: Int = 60,
                    var noClip: Boolean = true
                  ) {

  def this(buf: ByteBuf) = {
    this()
    loadFromBuf(buf)
  }

  def loadFromBuf(byteBuf: ByteBuf): Unit = {
    dimension = byteBuf.readInt()
    posX = byteBuf.readDouble()
    posY = byteBuf.readDouble()
    posZ = byteBuf.readDouble()
    scale = byteBuf.readFloat()
    motionX = byteBuf.readFloat()
    motionY = byteBuf.readFloat()
    motionZ = byteBuf.readFloat()
    accelX = byteBuf.readFloat()
    accelY = byteBuf.readFloat()
    accelZ = byteBuf.readFloat()
    alpha = byteBuf.readFloat()
    alpha = byteBuf.readFloat()
    red = byteBuf.readFloat()
    green = byteBuf.readFloat()
    blue = byteBuf.readFloat()
    age = byteBuf.readInt()
    noClip = byteBuf.readBoolean()
  }

  def worldObj = DimensionManager.getWorld(dimension)

  def saveToBuf(byteBuf: ByteBuf): Unit = {
    byteBuf.writeInt(dimension)
    byteBuf.writeDouble(posX)
    byteBuf.writeDouble(posY)
    byteBuf.writeDouble(posZ)
    byteBuf.writeFloat(scale)
    byteBuf.writeFloat(motionX)
    byteBuf.writeFloat(motionY)
    byteBuf.writeFloat(motionZ)
    byteBuf.writeFloat(accelX)
    byteBuf.writeFloat(accelY)
    byteBuf.writeFloat(accelZ)
    byteBuf.writeFloat(alpha)
    byteBuf.writeFloat(red)
    byteBuf.writeFloat(green)
    byteBuf.writeFloat(blue)
    byteBuf.writeInt(age)
    byteBuf.writeBoolean(noClip)
  }
}
