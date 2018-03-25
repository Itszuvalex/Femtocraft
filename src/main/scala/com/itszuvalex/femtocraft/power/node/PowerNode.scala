package com.itszuvalex.femtocraft.power.node

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.power.PowerNetworkNodeDelegate
import com.itszuvalex.femtocraft.power.PowerManager
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.util.{Color, Debug, PlayerUtils}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

/**
  * Created by Christopher Harris (Itszuvalex) on 8/3/15.
  */
object PowerNode {
  val POWER_COMPOUND_KEY = "PowerNode"
  val POWER_STORAGE_KEY = "Storage"
  //TODO: Fix this up
  val NODE_PARENT_KEY = "Parent"
  val COLOR_KEY = "Color"
}


trait PowerNode extends TileEntityBase {
  var powerDelegate: PowerNetworkNodeDelegate =
    new PowerNetworkNodeDelegate(this, powerRadius, powerTransfer, rendersPower)

  def powerRadius: Float

  def powerTransfer: Double

  def rendersPower: Boolean

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = {
    if (capability == Capabilities.TILE_POWER_NODE) true
    else if (capability == com.itszuvalex.itszulib.api.Capabilities.COLORABLE) true
    else super.hasCapability(capability, facing)
  }

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
    if (capability == Capabilities.TILE_POWER_NODE) powerDelegate.asInstanceOf[T]
    else if (capability == com.itszuvalex.itszulib.api.Capabilities.COLORABLE) getColor.asInstanceOf[T]
    else super.getCapability(capability, facing)
  }

  var color = Color(255.toByte,
    0.toByte,
    0.toByte,
    0.toByte)

  def getColor = color

  override def onSideActivate(par5EntityPlayer: EntityPlayer, side: EnumFacing): Boolean = {
    Debug.only {
      if (!getWorld.isRemote) {
        PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, if (powerDelegate.network == null) "networkless " else powerDelegate.network.id.toString)
        if (powerDelegate.network != null) {
          powerDelegate.network.getConnections(getLoc).getOrElse(Set()).foreach(loc =>
            PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, "Loc4:" + loc)
          )
          PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, f"Producer Nodes: ${powerDelegate.network.countProducers}, Net Gen: ${powerDelegate.network.powerProducedLastTick}%,.1f")
          PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, f"Consumer Nodes: ${powerDelegate.network.countConsumer}, Net Con: ${powerDelegate.network.powerConsumedLastTick}%,.1f")
          PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, f"Net Dif: ${powerDelegate.network.lastTickNetworkDelta}%,.1f, 10s Avg: ${powerDelegate.network.averagePowerTrend}%,.1f")
          PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, f"Storage Nodes: ${powerDelegate.network.countStorage}, Net Trend: ${powerDelegate.network.powerStorageDelta}%,.1f")
          PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, f"Storage: ${powerDelegate.network.dedicatedPowerStored}%,.1f/${powerDelegate.network.dedicatedPowerStorage}%,.1f")
        }
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

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    savePowerChildrenInfo(compound)
    compound.setInteger(PowerNode.COLOR_KEY, color.toInt)
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    loadPowerChildrenInfo(compound)
    color = new Color(compound.getInteger(PowerNode.COLOR_KEY))
    setRenderUpdate()
  }

  override def onBlockBreak(): Unit = {
    super.onBlockBreak()
    PowerManager.removeNode(powerDelegate)
    powerDelegate.leafNodeLocs.flatMap(_.getTileEntity(true)).withFilter(_.hasCapability(Capabilities.TILE_POWER_LEAF_NODE, null)).map(_.getCapability(Capabilities.TILE_POWER_LEAF_NODE, null)).foreach(_.onParentBroken(powerDelegate))
  }

  override def writeToNBT(compound: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(compound)
    savePowerChildrenInfo(compound)
    compound
  }

  def savePowerChildrenInfo(compound: NBTTagCompound): Unit = {
    compound(PowerNode.POWER_STORAGE_KEY -> powerDelegate.serializeNBT())
  }

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    loadPowerChildrenInfo(compound)
  }

  def loadPowerChildrenInfo(compound: NBTTagCompound): Unit = {
    powerDelegate.deserializeNBT(compound.getCompoundTag(PowerNode.POWER_STORAGE_KEY))
  }
}
