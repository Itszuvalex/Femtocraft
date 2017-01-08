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
      Option(parent).flatMap(_.getTileEntity(true)).withFilter(_.hasCapability(Capabilities.POWER_NODE, null)).map(_.getCapability(Capabilities.POWER_NODE, null)).foreach(_.removeLeafNode(this))

    PowerManager.removeLeaf(this)
  }

  override def transferRate: Double = Option(parent).flatMap(_.getTileEntity()).withFilter(_.hasCapability(Capabilities.POWER_NODE, null)).map(_.getCapability(Capabilities.POWER_NODE, null)).map(_.leafTransferRate).getOrElse(leafTransferRate)

  def leafTransferRate: Double

  override def getStorageLoc: Loc4 = getLoc

  override def setParent(node: IPowerNetworkNode): Unit = {
    parent = node.getLoc
    setModified()
  }

  override def onParentBroken(node: IPowerNetworkNode): Unit = {
    parent = null
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

  def color = Option(parent).flatMap(_.getTileEntity()).withFilter(_.hasCapability(Capabilities.COLORABLE, null)).map(_.getCapability(Capabilities.COLORABLE, null)).getOrElse(Color(255.toByte, 100.toByte, 100.toByte, 100.toByte))

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
    if (capability == Capabilities.POWER_LEAF_NODE) this.asInstanceOf[T]
    else if (capability == Capabilities.COLORABLE) color.asInstanceOf[T]
    else super.getCapability(capability, facing)
  }

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = {
    if (capability == Capabilities.POWER_LEAF_NODE) true
    else if (capability == Capabilities.COLORABLE) true
    else super.hasCapability(capability, facing)
  }

  override def readFromNBT(par1nbtTagCompound: NBTTagCompound): Unit = {
    super.readFromNBT(par1nbtTagCompound)
    readParentTag(par1nbtTagCompound)
  }

  override def writeToNBT(par1nbtTagCompound: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(par1nbtTagCompound)
    writeParentTag(par1nbtTagCompound)
  }

}
