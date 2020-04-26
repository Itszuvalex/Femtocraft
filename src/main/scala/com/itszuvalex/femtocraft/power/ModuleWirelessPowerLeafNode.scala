package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.api.power.{IWirelessPowerLeafNode, IWirelessPowerNetworkNode, PowerStorageNodeType}
import com.itszuvalex.itszulib.api.core.{IModule, Loc4}
import com.itszuvalex.itszulib.api.storage.IBattery
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.TileEntityModule
import net.minecraft.block.state.IBlockState
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing

object ModuleWirelessPowerLeafNode {
  val PARENT_LOC_NBT        = "Parent"
  val DEFAULT_RADIUS        = 8f
  val DEFAULT_TRANSFER_RATE = 10f
}

class ModuleWirelessPowerLeafNode(tile: ITileEntity,
                                  bat: IBattery,
                                  powerType: PowerStorageNodeType,
                                  conRadius: Float = ModuleWirelessPowerLeafNode.DEFAULT_RADIUS,
                                  transRate: () => Double = () => ModuleWirelessPowerLeafNode.DEFAULT_TRANSFER_RATE
                         ) extends TileEntityModule[IWirelessPowerLeafNode] with IWirelessPowerLeafNode {
  var parentLoc: Option[Loc4] = None

  override def module: IModule[IWirelessPowerLeafNode] = ManagerModules.TILE_WIRELESS_POWER_LEAF_NODE

  override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[IWirelessPowerLeafNode] = _ => Some(this)

  override def invalidate(tile: ITileEntity): Unit = {
    if (tile.getIWorld.isRemote) return
    WirelessPowerManager.instance.removeLeaf(this)
  }

  override def onBlockBreak(core: ITileEntity, state: IBlockState): Unit = {
    if (tile.getIWorld.isRemote) return

    WirelessPowerManager.instance.onLeafBroken(this)
    WirelessPowerManager.instance.removeLeaf(this)
  }

  override def onLoad(tile: ITileEntity): Unit = {
    if (tile.getIWorld.isRemote) return
    WirelessPowerManager.instance.addLeaf(this)
  }

  override def connectionRadius: Float = conRadius

  override def getParent: Loc4 = parentLoc.orNull

  override def setParent(node: IWirelessPowerNetworkNode): Unit = {
    parentLoc = Option(node.getLoc)
    tile.setUpdate()
    tile.markDirtyForSave()
  }

  override def onParentBroken(node: IWirelessPowerNetworkNode): Unit = {
    if (tile.getIWorld.isRemote) return
    parentLoc = None
    tile.setUpdate()
    tile.markDirtyForSave()
    WirelessPowerManager.instance.refreshLeaf(this)
  }

  override def battery: IBattery = bat

  override def storageType: PowerStorageNodeType = powerType

  override def transferRate: Double = transRate()

  override def getStorageLoc: Loc4 = Loc4(tile)

  override def changeForLastTick: Double = 0 //TODO Fix

  override def hasDescriptionNBT: Boolean = true

  override def hasWorldNBT: Boolean = true

  override def writeDescriptionNBT(tag: NBTTagCompound): Unit = saveToNBT(tag)

  override def readDescriptionNBT(tag: NBTTagCompound): Unit = readFromNBT(tag)

  override def writeWorldNBT(tag: NBTTagCompound): Unit = saveToNBT(tag)

  override def readWorldNBT(tag: NBTTagCompound): Unit = readFromNBT(tag)

  def saveToNBT(t: NBTTagCompound): Unit = {
    parentLoc.foreach(p => t.setTag(ModuleWirelessPowerLeafNode.PARENT_LOC_NBT, p.serializeNBT()))
  }

  def readFromNBT(t: NBTTagCompound): Unit = {
    parentLoc = if (t.hasKey(ModuleWirelessPowerLeafNode.PARENT_LOC_NBT)) {
      Option(Loc4(t.getCompoundTag(ModuleWirelessPowerLeafNode.PARENT_LOC_NBT)))
    } else None
    tile.setRenderUpdate()
  }
}
