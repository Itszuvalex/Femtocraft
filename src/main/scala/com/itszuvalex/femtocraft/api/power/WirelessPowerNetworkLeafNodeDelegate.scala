package com.itszuvalex.femtocraft.api.power

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.power.WirelessPowerManager
import com.itszuvalex.femtocraft.util.data.{DataAssignable, DataSpec}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.storage.IBattery
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.TileEntityCore
import net.minecraft.nbt.NBTTagCompound

object WirelessPowerNetworkLeafNodeDelegate {
  val PARENT_TAG = "parent"

  def INHERIT_TRANSFER_FROM_PARENT(delegate: WirelessPowerNetworkLeafNodeDelegate, default: Double): () => Double = () =>
    delegate.parentLoc.flatMap(_.getITileEntity()).withFilter(_.hasModule(ManagerModules.TILE_WIRELESS_POWER_NODE, null)).map(_.getModule(ManagerModules.TILE_WIRELESS_POWER_NODE, null)).map(_.leafTransferRate).getOrElse(default)
}

class WirelessPowerNetworkLeafNodeDelegate(
                                    tileEntity: TileEntityCore,
                                    radius: () => Float,
                                    getBattery: () => IBattery,
                                    stype: PowerStorageNodeType,
                                    transfer: () => Double,
                                    change: () => Double
                                  ) extends IWirelessPowerLeafNode with DataSpec {
  var parentLoc: Option[Loc4] = None

  dataSpec += new DataAssignable[Option[Loc4]](WirelessPowerNetworkLeafNodeDelegate.PARENT_TAG,
                                               parentLoc _,
    { case None => null; case Some(a) => a.serializeNBT() },
                                               parentLoc_=,
    { case t: NBTTagCompound => Some(Option(Loc4(t))); case _ => Some(None) }
                                               )

  override def connectionRadius: Float = radius()

  override def getParent: Loc4 = parentLoc.orNull

  override def setParent(node: IWirelessPowerNetworkNode): Unit = {
    parentLoc = Option(node.getLoc)
    tileEntity.setUpdate()
    tileEntity.markDirtyForSave()
  }

  override def onParentBroken(node: IWirelessPowerNetworkNode): Unit = {
    parentLoc = None
    tileEntity.setUpdate()
    tileEntity.markDirtyForSave()

    if (tileEntity.getWorld.isRemote) return
    WirelessPowerManager.instance.refreshLeaf(this)
  }

  override def battery: IBattery = getBattery()

  override def storageType: PowerStorageNodeType = stype

  override def transferRate: Double = transfer()

  override def getStorageLoc: Loc4 = Loc4(tileEntity.asInstanceOf[ITileEntity])

  override def changeForLastTick: Double = change()
}
