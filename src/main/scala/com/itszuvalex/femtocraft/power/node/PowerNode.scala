package com.itszuvalex.femtocraft.power.node

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.power.{PowerConnectionNodeType, PowerNetworkNodeDelegate, PowerStorageNodeType}
import com.itszuvalex.femtocraft.power.PowerManager
import com.itszuvalex.itszulib.api.wrappers.IBattery
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.util.Color
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

import scala.util.Random

/**
  * Created by Christopher Harris (Itszuvalex) on 8/3/15.
  */
object PowerNode {
  val POWER_COMPOUND_KEY = "FemtoPower"
  val POWER_STORAGE_KEY  = "Storage"
  //TODO: Fix this up
  val NODE_PARENT_KEY    = "Parent"
  val NODE_CHILDREN_KEY  = "Children"
  val COLOR_KEY          = "Color"
}


trait PowerNode extends TileEntityBase {
  var battery      : IBattery                 = defaultBattery
  var powerDelegate: PowerNetworkNodeDelegate = new PowerNetworkNodeDelegate(this, powerStorageType, powerConnectionType, powerRadius, powerTransfer, rendersPower, battery)

  def defaultBattery: IBattery

  def powerStorageType: PowerStorageNodeType

  def powerConnectionType: PowerConnectionNodeType

  def powerRadius: Float

  def powerTransfer: Double

  def rendersPower: Boolean

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = {
    if (super.hasCapability(capability, facing)) {
      true
    } else {
      capability match {
        case Capabilities.POWER_NODE => true
        case Capabilities.POWER_STORAGE => true
        case Capabilities.COLORABLE => true
        case _ => false
      }
    }
  }

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
    val cap = super.getCapability(capability, facing)
    if (cap != null) cap
    else {
      capability match {
        case Capabilities.POWER_STORAGE => battery.asInstanceOf[T]
        case Capabilities.POWER_NODE => powerDelegate.asInstanceOf[T]
        case Capabilities.COLORABLE => color.asInstanceOf[T]
        case _ => null.asInstanceOf[T]
      }
    }
  }

  var color = Color(255.toByte,
    (Random.nextInt(125) + 130).toByte,
    (Random.nextInt(125) + 130).toByte,
    (Random.nextInt(125) + 130).toByte).toInt

  override def onBlockBreak() = {
    PowerManager.removeNode(powerDelegate)
  }


  override def invalidate(): Unit = {
    super.invalidate()
    if (!getWorld.isRemote) PowerManager.removeNode(powerDelegate)
  }

  override def writeToNBT(compound: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(compound)
    savePowerStorageInfo(compound)
    compound
  }

  def savePowerStorageInfo(compound: NBTTagCompound): Unit = {
    compound(PowerNode.POWER_STORAGE_KEY -> battery)
  }

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    loadPowerStorageInfo(compound)
  }

  def loadPowerStorageInfo(compound: NBTTagCompound): Unit = {
    battery = IBattery.createFromNBT(compound.getCompoundTag(PowerNode.POWER_STORAGE_KEY))
  }
}
