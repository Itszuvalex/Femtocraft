package com.itszuvalex.femtocraft.power.node

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.power.PowerNetworkLeafNodeDelegate
import com.itszuvalex.femtocraft.power.PowerManager
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.util.Color
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

/**
  * Created by Chris on 1/7/2017.
  */
object PowerLeafNode {
  val PARENT_TAG = "parent"
  val COLOR_TAG  = "color"

  val DEFAULT_RADIUS = 8f
}

trait PowerLeafNode extends TileEntityBase with PowerStorageNode {
  val leafDelegate: PowerNetworkLeafNodeDelegate = new PowerNetworkLeafNodeDelegate(this, connectionRadius _, battery _, powerStorageNodeType,
    PowerNetworkLeafNodeDelegate.INHERIT_TRANSFER_FROM_PARENT(leafDelegate, powerTransferRateDefault),
    () => delegate.changeForLastTick)
  leafDelegate.dataSpec.onLoad = () => setRenderUpdate()

  def connectionRadius: Float = PowerLeafNode.DEFAULT_RADIUS

  def powerTransferRateDefault: Double = powerStorageTransferRate

  override def onLoad(): Unit = {
    super.onLoad()
    if (getWorld.isRemote) return
    PowerManager.addLeaf(leafDelegate)
  }

  override def validate(): Unit = {
    super.validate()
    if (getWorld.isRemote) return
    PowerManager.addLeaf(leafDelegate)
  }

  override def invalidate(): Unit = {
    super.invalidate()
    if (getWorld.isRemote) return
    PowerManager.removeLeaf(leafDelegate)
  }

  override def onBlockBreak(): Unit = {
    super.onBlockBreak()
    if (getWorld.isRemote) return

    PowerManager.onLeafBroken(leafDelegate)
    PowerManager.removeLeaf(leafDelegate)
  }

  def writeColorTag(tag: NBTTagCompound): NBTTagCompound = {
    tag.setInteger(PowerLeafNode.COLOR_TAG, color.toInt)
    tag
  }

  def readColorTag(tag: NBTTagCompound): Unit = {
    color = new Color(tag.getInteger(PowerLeafNode.COLOR_TAG))
    setRenderUpdate()
  }

  var color = Color(255.toByte, 0.toByte, 0.toByte, 0.toByte)

  def getColor = leafDelegate.parentLoc.flatMap(_.getTileEntity()).withFilter(_.hasCapability(com.itszuvalex.itszulib.api.Capabilities.COLORABLE, null)).map(_.getCapability(com.itszuvalex.itszulib.api.Capabilities.COLORABLE, null)).getOrElse(color)

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
    if (capability == Capabilities.TILE_POWER_LEAF_NODE) leafDelegate.asInstanceOf[T]
    else if (capability == com.itszuvalex.itszulib.api.Capabilities.COLORABLE) getColor.asInstanceOf[T]
    else super.getCapability(capability, facing)
  }

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = {
    if (capability == Capabilities.TILE_POWER_LEAF_NODE) true
    else if (capability == com.itszuvalex.itszulib.api.Capabilities.COLORABLE) true
    else super.hasCapability(capability, facing)
  }

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    leafDelegate.writeToNBT(compound)
    writeColorTag(compound)
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    leafDelegate.deserializeNBT(compound)
    readColorTag(compound)
  }

  override def readFromNBT(par1nbtTagCompound: NBTTagCompound): Unit = {
    super.readFromNBT(par1nbtTagCompound)
    leafDelegate.deserializeNBT(par1nbtTagCompound)
    setRenderUpdate()
  }

  override def writeToNBT(par1nbtTagCompound: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(par1nbtTagCompound)
    leafDelegate.writeToNBT(par1nbtTagCompound)
  }

}
