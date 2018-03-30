package com.itszuvalex.femtocraft.api.power

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.power.PowerManager
import com.itszuvalex.femtocraft.util.data.{DataAssignable, DataSpec}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.IBattery
import com.itszuvalex.itszulib.core.TileEntityBase
import net.minecraft.nbt.NBTTagCompound


object PowerNetworkLeafNodeDelegate {
  val PARENT_TAG = "parent"

  def INHERIT_TRANSFER_FROM_PARENT(delegate: PowerNetworkLeafNodeDelegate, default: Double): () => Double = () =>
    delegate.parentLoc.flatMap(_.getTileEntity()).withFilter(_.hasCapability(Capabilities.TILE_POWER_NODE, null)).map(_.getCapability(Capabilities.TILE_POWER_NODE, null)).map(_.leafTransferRate).getOrElse(default)
}

class PowerNetworkLeafNodeDelegate(
  tileEntity: TileEntityBase,
  radius: () => Float,
  getBattery: () => IBattery,
  stype: PowerStorageNodeType,
  transfer: () => Double
) extends IPowerLeafNode with DataSpec {
  var parentLoc: Option[Loc4] = None

  dataSpec += new DataAssignable[Option[Loc4]](PowerNetworkLeafNodeDelegate.PARENT_TAG,
  parentLoc _,
  { case None => null; case Some(a) => a.serializeNBT() },
  parentLoc_=,
  { case t: NBTTagCompound => Some(Option(Loc4(t))); case _ => Some(None) }
  )

  override def connectionRadius: Float = radius()

  override def getParent: Loc4 = parentLoc.orNull

  override def setParent(node: IPowerNetworkNode): Unit = {
    parentLoc = Option(node.getLoc)
    tileEntity.setUpdate()
    tileEntity.setModified()
  }

  override def onParentBroken(node: IPowerNetworkNode): Unit = {
    parentLoc = None
    tileEntity.setUpdate()
    tileEntity.setModified()

    if (tileEntity.getWorld.isRemote) return
    PowerManager.refreshLeaf(this)
  }

  override def battery: IBattery = getBattery()

  override def storageType: PowerStorageNodeType = stype

  override def transferRate: Double = transfer()

  override def getStorageLoc: Loc4 = new Loc4(tileEntity)
}
