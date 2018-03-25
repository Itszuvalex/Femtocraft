package com.itszuvalex.femtocraft.power.node

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.power.{IPowerLeafNode, IPowerNetworkNode}
import com.itszuvalex.femtocraft.power.PowerManager
import com.itszuvalex.itszulib.api.core.Loc4
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
}

trait PowerLeafNode extends TileEntityBase with PowerStorageNode with IPowerLeafNode {
  var parent: Loc4 = _

  override def getParent: Loc4 = parent

  override def onLoad(): Unit = {
    super.onLoad()
    if (getWorld.isRemote) return
    PowerManager.addLeaf(this)
  }

  override def validate(): Unit = {
    super.validate()
    if (getWorld.isRemote) return
    PowerManager.addLeaf(this)
  }

  override def invalidate(): Unit = {
    super.invalidate()
    if (getWorld.isRemote) return
    PowerManager.removeLeaf(this)
  }

  override def onBlockBreak(): Unit = {
    super.onBlockBreak()
    if (getWorld.isRemote) return

    if (parent != null)
      Option(parent).flatMap(_.getTileEntity(true)).withFilter(_.hasCapability(Capabilities.TILE_POWER_NODE, null)).map(_.getCapability(Capabilities.TILE_POWER_NODE, null)).foreach(_.removeLeafNode(this))

    PowerManager.removeLeaf(this)
  }

  override def transferRate: Double = Option(parent).flatMap(_.getTileEntity()).withFilter(_.hasCapability(Capabilities.TILE_POWER_NODE, null)).map(_.getCapability(Capabilities.TILE_POWER_NODE, null)).map(_.leafTransferRate).getOrElse(leafTransferRate)

  def leafTransferRate: Double

  override def getStorageLoc: Loc4 = getLoc

  override def setParent(node: IPowerNetworkNode): Unit = {
    parent = node.getLoc
    setUpdate()
    setModified()
  }

  override def onParentBroken(node: IPowerNetworkNode): Unit = {
    parent = null
    setUpdate()
    setModified()

    if (getWorld.isRemote) return
    PowerManager.refreshLeaf(this)
  }

  def readParentTag(tag: NBTTagCompound): Unit = {
    parent = null
    if (tag.hasKey(PowerLeafNode.PARENT_TAG))
      parent = Loc4(tag.getCompoundTag(PowerLeafNode.PARENT_TAG))
    setRenderUpdate()
  }

  def writeParentTag(tag: NBTTagCompound): NBTTagCompound = {
    if (parent != null)
      tag.setTag(PowerLeafNode.PARENT_TAG, parent.serializeNBT())
    tag
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

  def getColor = Option(parent).flatMap(_.getTileEntity()).withFilter(_.hasCapability(com.itszuvalex.itszulib.api.Capabilities.COLORABLE, null)).map(_.getCapability(com.itszuvalex.itszulib.api.Capabilities.COLORABLE, null)).getOrElse(color)

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
    if (capability == Capabilities.TILE_POWER_LEAF_NODE) this.asInstanceOf[T]
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
    writeParentTag(compound)
    writeColorTag(compound)
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    readParentTag(compound)
    readColorTag(compound)
  }

  override def readFromNBT(par1nbtTagCompound: NBTTagCompound): Unit = {
    super.readFromNBT(par1nbtTagCompound)
    readParentTag(par1nbtTagCompound)
    setRenderUpdate()
  }

  override def writeToNBT(par1nbtTagCompound: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(par1nbtTagCompound)
    writeParentTag(par1nbtTagCompound)
  }

}
