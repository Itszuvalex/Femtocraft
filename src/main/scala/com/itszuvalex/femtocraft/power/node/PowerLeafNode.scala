package com.itszuvalex.femtocraft.power.node

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.power.{IPowerLeafNode, PowerNetworkLeafNodeDelegate}
import com.itszuvalex.femtocraft.power.PowerManager
import com.itszuvalex.femtocraft.util.data.DataSpec
import com.itszuvalex.itszulib.api.{ItszuLibCapabilities, ItszuLibModules}
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
  val leafDelegate: IPowerLeafNode = defaultLeafDelegate
  var color                        = Color(255.toByte, 0.toByte, 0.toByte, 0.toByte)

  def defaultLeafDelegate: IPowerLeafNode = {
    val del: PowerNetworkLeafNodeDelegate = new PowerNetworkLeafNodeDelegate(this, connectionRadius _, battery _, powerStorageNodeType,
                                                                             PowerNetworkLeafNodeDelegate.INHERIT_TRANSFER_FROM_PARENT(leafDelegate.asInstanceOf[PowerNetworkLeafNodeDelegate], powerTransferRateDefault),
                                                                             () => delegate.changeForLastTick)
    del.dataSpec.onLoad = () => setRenderUpdate()
    del
  }

  def connectionRadius: Float = PowerLeafNode.DEFAULT_RADIUS

  def powerTransferRateDefault: Double = powerStorageTransferRate

  override def onLoad(): Unit = {
    super.onLoad()
    if (getWorld.isRemote) return
    PowerManager.instance.addLeaf(leafDelegate)
  }

  override def validate(): Unit = {
    super.validate()
    if (getWorld.isRemote) return
    PowerManager.instance.addLeaf(leafDelegate)
  }

  override def invalidate(): Unit = {
    super.invalidate()
    if (getWorld.isRemote) return
    PowerManager.instance.removeLeaf(leafDelegate)
  }

  override def onBlockBreak(): Unit = {
    super.onBlockBreak()
    if (getWorld.isRemote) return

    PowerManager.instance.onLeafBroken(leafDelegate)
    PowerManager.instance.removeLeaf(leafDelegate)
  }

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
    if (capability == Capabilities.TILE_POWER_LEAF_NODE) leafDelegate.asInstanceOf[T]
    else if (capability == ItszuLibCapabilities.COLORABLE) getColor.asInstanceOf[T]
    else super.getCapability(capability, facing)
  }

  def getColor: Color = Option(leafDelegate.getParent).flatMap(_.getITileEntity()).withFilter(_.hasModule(ItszuLibModules.COLORABLE, null)).map(_.getModule(ItszuLibModules.COLORABLE, null)).getOrElse(color)

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = {
    if (capability == Capabilities.TILE_POWER_LEAF_NODE) true
    else if (capability == ItszuLibCapabilities.COLORABLE) true
    else super.hasCapability(capability, facing)
  }

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    writeLeafTag(compound)
    writeColorTag(compound)
  }

  def writeColorTag(tag: NBTTagCompound): NBTTagCompound = {
    tag.setInteger(PowerLeafNode.COLOR_TAG, color.toInt)
    tag
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    readLeafTag(compound)
    readColorTag(compound)
  }

  def readColorTag(tag: NBTTagCompound): Unit = {
    color = new Color(tag.getInteger(PowerLeafNode.COLOR_TAG))
    setRenderUpdate()
  }

  override def readFromNBT(par1nbtTagCompound: NBTTagCompound): Unit = {
    super.readFromNBT(par1nbtTagCompound)
    readLeafTag(par1nbtTagCompound)
    setRenderUpdate()
  }

  def readLeafTag(compound: NBTTagCompound): Unit = {
    leafDelegate match {
      case a: DataSpec => a.deserializeNBT(compound)
    }
  }

  override def writeToNBT(par1nbtTagCompound: NBTTagCompound): NBTTagCompound = {
    val ret = super.writeToNBT(par1nbtTagCompound)
    writeLeafTag(par1nbtTagCompound)
    ret
  }

  def writeLeafTag(compound: NBTTagCompound): Unit = {
    leafDelegate match {
      case a: DataSpec => a.writeToNBT(compound)
    }
  }

}
