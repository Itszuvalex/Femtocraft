package com.itszuvalex.femtocraft.power.node

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.power.{IPowerStorageNode, PowerStorageNodeDelegate, PowerStorageNodeType}
import com.itszuvalex.itszulib.api.storage.IBattery
import com.itszuvalex.itszulib.core.TileEntityCoreTickable
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

trait PowerStorageNode extends TileEntityCoreTickable {
  val delegate: IPowerStorageNode = defaultStorageDelegate
  var battery : IBattery          = defaultBattery

  def defaultStorageDelegate: IPowerStorageNode = new PowerStorageNodeDelegate(this, battery _, powerStorageNodeType, () => powerStorageTransferRate)

  def defaultBattery: IBattery

  def powerStorageNodeType: PowerStorageNodeType

  def powerStorageTransferRate: Double

  override def serverUpdate(): Unit = {
    super.serverUpdate()
    delegate match {case a: PowerStorageNodeDelegate => a.updateServerTick(); case _ =>}
  }

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
    if (capability == Capabilities.TILE_POWER_STORAGE_NODE) delegate.asInstanceOf[T]
    else if (capability == Capabilities.POWER_STORAGE) battery.asInstanceOf[T]
    else super.getCapability(capability, facing)
  }

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = {
    if (capability == Capabilities.TILE_POWER_STORAGE_NODE) true
    else if (capability == Capabilities.POWER_STORAGE) true
    else super.hasCapability(capability, facing)
  }

  override def readFromNBT(par1nbtTagCompound: NBTTagCompound): Unit = {
    super.readFromNBT(par1nbtTagCompound)
    readBatteryTag(par1nbtTagCompound)
  }

  def readBatteryTag(tag: NBTTagCompound): Unit = {
    battery.deserializeNBT(tag.getCompoundTag(PowerStorageNode.BATTERY_TAG))
  }

  override def writeToNBT(par1nbtTagCompound: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(par1nbtTagCompound)
    writeBatteryTag(par1nbtTagCompound)
  }

  def writeBatteryTag(tag: NBTTagCompound): NBTTagCompound = {
    tag(PowerStorageNode.BATTERY_TAG -> battery.serializeNBT())
    tag
  }
}
