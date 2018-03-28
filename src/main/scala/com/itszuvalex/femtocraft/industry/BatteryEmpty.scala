package com.itszuvalex.femtocraft.industry

import com.itszuvalex.itszulib.api.wrappers.IBattery
import net.minecraft.nbt.NBTTagCompound

object BatteryEmpty {
  val Empty = new IBattery {
    override def storage: Double = 0

    override def storage_=(amt: Double): Unit = {}

    override def maxStorage: Double = 0

    override def maxStorage_=(max: Double): Unit = {}

    override def copy(): IBattery = this

    override def clear(): Unit = {}

    override def writeToNBT(nbt: NBTTagCompound): Unit = {}

    override def deserializeNBT(nbt: NBTTagCompound): Unit = {}

    override def serializeNBT(): NBTTagCompound = new NBTTagCompound
  }
}
