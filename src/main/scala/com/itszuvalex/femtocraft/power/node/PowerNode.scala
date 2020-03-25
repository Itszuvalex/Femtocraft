package com.itszuvalex.femtocraft.power.node

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.power.PowerNetworkNodeDelegate
import com.itszuvalex.femtocraft.power.PowerManager
import com.itszuvalex.itszulib.api.ItszuLibCapabilities
import com.itszuvalex.itszulib.core.TileEntityCore
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.util.Color
import net.minecraft.block.state.IBlockState
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

/**
  * Created by Christopher Harris (Itszuvalex) on 8/3/15.
  */
object PowerNode {
  val POWER_COMPOUND_KEY = "PowerNode"
  val POWER_STORAGE_KEY  = "PowerStorage"
  //TODO: Fix this up
  val NODE_PARENT_KEY    = "PowerParent"
  val COLOR_KEY          = "Color"
}


trait PowerNode extends TileEntityCore {
  var powerDelegate: PowerNetworkNodeDelegate =
    new PowerNetworkNodeDelegate(this, powerRadius, powerTransfer, rendersPower)
  var color                                   = Color(255.toByte,
                                                      0.toByte,
                                                      0.toByte,
                                                      0.toByte)

  def powerRadius: Float

  def powerTransfer: Double

  def rendersPower: Boolean

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = {
    if (capability == Capabilities.TILE_POWER_NODE) true
    else if (capability == ItszuLibCapabilities.COLORABLE) true
    else super.hasCapability(capability, facing)
  }

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
    if (capability == Capabilities.TILE_POWER_NODE) powerDelegate.asInstanceOf[T]
    else if (capability == ItszuLibCapabilities.COLORABLE) getColor.asInstanceOf[T]
    else super.getCapability(capability, facing)
  }

  def getColor = color

  override def invalidate(): Unit = {
    super.invalidate()
    if (!getWorld.isRemote) PowerManager.instance.removeNode(powerDelegate)
  }

  override def onChunkUnload(): Unit = {
    super.onChunkUnload()
    if (!getWorld.isRemote) PowerManager.instance.removeNode(powerDelegate)
  }

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    savePowerChildrenInfo(compound)
    compound.setInteger(PowerNode.COLOR_KEY, color.toInt)
  }

  def savePowerChildrenInfo(compound: NBTTagCompound): Unit = {
    compound(PowerNode.POWER_STORAGE_KEY -> powerDelegate.serializeNBT())
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    loadPowerChildrenInfo(compound)
    color = new Color(compound.getInteger(PowerNode.COLOR_KEY))
    setRenderUpdate()
  }

  override def onBlockBreak(state: IBlockState): Unit = {
    super.onBlockBreak(state)
    PowerManager.instance.removeNode(powerDelegate)
    PowerManager.instance.onNodeBroken(powerDelegate)
  }

  override def writeToNBT(compound: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(compound)
    savePowerChildrenInfo(compound)
    compound
  }

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    loadPowerChildrenInfo(compound)
  }

  def loadPowerChildrenInfo(compound: NBTTagCompound): Unit = {
    powerDelegate.deserializeNBT(compound.getCompoundTag(PowerNode.POWER_STORAGE_KEY))
  }
}
