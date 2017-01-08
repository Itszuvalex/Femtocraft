package com.itszuvalex.femtocraft.power.node

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.power.IPowerStorageNode
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.IBattery
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

/**
  * Created by Chris on 1/5/2017.
  */
object PowerStorageNode {
  val BATTERY_TAG = "battery"
}

trait PowerStorageNode extends TileEntityBase with IPowerStorageNode {
  var battery: IBattery = defaultBattery

  def defaultBattery: IBattery

  override def getStorageLoc: Loc4 = getLoc

  def readBatteryTag(tag: NBTTagCompound): Unit = {
    battery.deserializeNBT(tag.getCompoundTag(PowerStorageNode.BATTERY_TAG))
  }

  def writeBatteryTag(tag: NBTTagCompound): NBTTagCompound = {
    tag(PowerStorageNode.BATTERY_TAG -> battery.serializeNBT())
    tag
  }

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
    if (capability == Capabilities.POWER_STORAGE_NODE) this.asInstanceOf[T]
    else super.getCapability(capability, facing)
  }

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = {
    if (capability == Capabilities.POWER_STORAGE_NODE) true
    else super.hasCapability(capability, facing)
  }

  override def readFromNBT(par1nbtTagCompound: NBTTagCompound): Unit = {
    super.readFromNBT(par1nbtTagCompound)
    readBatteryTag(par1nbtTagCompound)
  }

  override def writeToNBT(par1nbtTagCompound: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(par1nbtTagCompound)
    writeBatteryTag(par1nbtTagCompound)
  }
}
