package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.api.power.{IPowerLeafNode, IPowerNetworkNode, PowerStorageNodeType}
import com.itszuvalex.itszulib.api.core.{IModule, Loc4}
import com.itszuvalex.itszulib.api.storage.IBattery
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.{TileEntityCore, TileEntityModule}
import net.minecraft.block.state.IBlockState
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing

object ModulePowerLeafNode {
  val PARENT_LOC_NBT        = "Parent"
  val DEFAULT_RADIUS        = 8f
  val DEFAULT_TRANSFER_RATE = 10f
}

class ModulePowerLeafNode(tile: ITileEntity,
                          bat: IBattery,
                          powerType: PowerStorageNodeType,
                          conRadius: Float = ModulePowerLeafNode.DEFAULT_RADIUS,
                          transRate: () => Double = () => ModulePowerLeafNode.DEFAULT_TRANSFER_RATE
                         ) extends TileEntityModule[IPowerLeafNode] with IPowerLeafNode {
  var parentLoc: Option[Loc4] = None

  override def module: IModule[IPowerLeafNode] = ManagerModules.TILE_POWER_LEAF_NODE

  override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[IPowerLeafNode] = _ => Some(this)

  override def invalidate(tile: ITileEntity): Unit = {
    if (tile.getIWorld.isRemote) return
    PowerManager.instance.removeLeaf(this)
  }

  override def validate(tile: ITileEntity): Unit = {
    if (tile.getIWorld.isRemote) return
    PowerManager.instance.addLeaf(this)
  }

  override def onBlockBreak(core: ITileEntity, state: IBlockState): Unit = {
    if (tile.getIWorld.isRemote) return

    PowerManager.instance.onLeafBroken(this)
    PowerManager.instance.removeLeaf(this)
  }

  override def onLoad(tile: ITileEntity): Unit = {
    if (tile.getIWorld.isRemote) return
    PowerManager.instance.addLeaf(this)
  }

  override def connectionRadius: Float = conRadius

  override def getParent: Loc4 = parentLoc.orNull

  override def setParent(node: IPowerNetworkNode): Unit = {
    parentLoc = Option(node.getLoc)
    tile.asInstanceOf[TileEntityCore].setUpdate()
    tile.markDirtyForSave()
  }

  override def onParentBroken(node: IPowerNetworkNode): Unit = {
    parentLoc = None
    // TODO: Fix when ITileEntity has this
    tile.asInstanceOf[TileEntityCore].setUpdate()
    tile.markDirtyForSave()

    if (tile.getIWorld.isRemote) return
    PowerManager.instance.refreshLeaf(this)
  }

  override def battery: IBattery = bat

  override def storageType: PowerStorageNodeType = powerType

  override def transferRate: Double = transRate()

  override def getStorageLoc: Loc4 = new Loc4(tile)

  override def changeForLastTick: Double = 0 //TODO Fix

  override def hasDescriptionNBT: Boolean = true

  override def hasWorldNBT: Boolean = true

  override def writeDescriptionNBT(tag: NBTTagCompound): Unit = saveToNBT(tag)

  override def readDescriptionNBT(tag: NBTTagCompound): Unit = readFromNBT(tag)

  override def writeWorldNBT(tag: NBTTagCompound): Unit = saveToNBT(tag)

  override def readWorldNBT(tag: NBTTagCompound): Unit = readFromNBT(tag)

  def saveToNBT(t: NBTTagCompound): Unit = {
    parentLoc.foreach(p => t.setTag(ModulePowerLeafNode.PARENT_LOC_NBT, p.serializeNBT()))
  }

  def readFromNBT(t: NBTTagCompound): Unit = {
    parentLoc = if (t.hasKey(ModulePowerLeafNode.PARENT_LOC_NBT)) {
      Option(Loc4(t.getCompoundTag(ModulePowerLeafNode.PARENT_LOC_NBT)))
    } else None
  }
}
