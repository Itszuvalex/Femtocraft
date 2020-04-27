package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.api.power.{IWiredPowerLeafNode, PowerStorageNodeType}
import com.itszuvalex.itszulib.api.core.IModule
import com.itszuvalex.itszulib.api.storage.IBattery
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.TileEntityModule
import com.itszuvalex.itszulib.util.FaceBitSet
import net.minecraft.nbt.{NBTTagByte, NBTTagCompound}
import net.minecraft.util.EnumFacing

object ModuleWiredPowerLeafNode {
  val CON_NBT = "Con"
}

class ModuleWiredPowerLeafNode(tile: ITileEntity, batFunc: () => IBattery, powType: PowerStorageNodeType, transRateFunc: () => Double) extends TileEntityModule[IWiredPowerLeafNode] with IWiredPowerLeafNode {
  val connections = new FaceBitSet

  override def module: IModule[IWiredPowerLeafNode] = ManagerModules.TILE_WIRED_POWER_LEAF_NODE

  override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[IWiredPowerLeafNode] = _ => Some(this)

  override def battery: IBattery = batFunc()

  override def powerType: PowerStorageNodeType = powType

  override def transferRate: Double = transRateFunc()

  override def isConnectedWiredPower(facing: EnumFacing): Boolean = connections.get(facing)

  override def canConnectWiredPower(facing: EnumFacing): Boolean = true

  override def connectWiredPower(facing: EnumFacing): Boolean = {
    connections.set(facing)
    tile.setUpdate()
    tile.markDirtyForSave()
    true
  }

  override def disconnectWiredPower(facing: EnumFacing): Boolean = {
    connections.clear(facing)
    tile.setUpdate()
    tile.markDirtyForSave()
    true
  }

  override def hasDescriptionNBT: Boolean = true

  override def hasWorldNBT: Boolean = true

  override def writeDescriptionNBT(tag: NBTTagCompound): Unit = writeConnectionNBT(tag)

  override def readDescriptionNBT(tag: NBTTagCompound): Unit = readConnectionNBT(tag)

  override def writeWorldNBT(tag: NBTTagCompound): Unit = writeConnectionNBT(tag)

  override def readWorldNBT(tagCompound: NBTTagCompound): Unit = readConnectionNBT(tagCompound)

  def writeConnectionNBT(tag: NBTTagCompound): Unit = tag.setTag(ModuleWiredPowerLeafNode.CON_NBT, connections.serializeNBT())

  def readConnectionNBT(tag: NBTTagCompound): Unit = connections.deserializeNBT(tag.getTag(ModuleWiredPowerLeafNode.CON_NBT).asInstanceOf[NBTTagByte])
}
