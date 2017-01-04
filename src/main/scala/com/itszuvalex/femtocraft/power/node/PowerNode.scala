package com.itszuvalex.femtocraft.power.node

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.power.{PowerConnectionNodeType, PowerNetworkLeafNodeDelegate, PowerNetworkNodeDelegate, PowerStorageNodeType}
import com.itszuvalex.femtocraft.power.PowerManager
import com.itszuvalex.itszulib.api.wrappers.IBattery
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.util.{Color, PlayerUtils}
import net.minecraft.entity.player.EntityPlayer
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
  var powerDelegate: PowerNetworkNodeDelegate =
    if (powerConnectionType == PowerConnectionNodeType.MAIN)
      new PowerNetworkNodeDelegate(this, powerStorageType, powerConnectionType, powerRadius, powerTransfer, rendersPower, battery)
    else
      new PowerNetworkLeafNodeDelegate(this, powerStorageType, powerConnectionType, powerRadius, powerTransfer, rendersPower, battery)

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
      if (capability == Capabilities.POWER_NODE) true
      else if (capability == Capabilities.POWER_STORAGE) true
      else if (capability == Capabilities.COLORABLE) true
      else false
    }
  }

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
    if (capability == Capabilities.POWER_STORAGE) battery.asInstanceOf[T]
    else if (capability == Capabilities.POWER_NODE) powerDelegate.asInstanceOf[T]
    else if (capability == Capabilities.COLORABLE) new Color(color).asInstanceOf[T]
    else super.getCapability(capability, facing)
  }

  var color = Color(255.toByte,
    (Random.nextInt(125) + 130).toByte,
    (Random.nextInt(125) + 130).toByte,
    (Random.nextInt(125) + 130).toByte).toInt


  override def onSideActivate(par5EntityPlayer: EntityPlayer, side: EnumFacing): Boolean = {
    if (!getWorld.isRemote) {
      PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, if (powerDelegate.network == null) "networkless " else powerDelegate.network.id.toString)
      if (powerDelegate.network != null) {
        powerDelegate.network.getConnections(getLoc).getOrElse(Set()).foreach(loc =>
          PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, "Loc4:" + loc)
        )
      }
    }
    super.onSideActivate(par5EntityPlayer, side)
  }

  override def invalidate(): Unit = {
    super.invalidate()
    if (!getWorld.isRemote) PowerManager.removeNode(powerDelegate)
  }

  override def onChunkUnload(): Unit = {
    super.onChunkUnload()
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
