package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.api.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.power.node.PowerLeafNode
import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStorageArray}
import com.itszuvalex.itszulib.api.wrappers.IBattery
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.TileInventory

/**
  * Created by Chris on 1/8/2017.
  */
class TileEntityCrystalChargingArray extends TileEntityBase with TileInventory with PowerLeafNode {
  override def getMod: AnyRef = Femtocraft

  override def defaultStorage: IItemStorage = new ItemStorageArray(6)

  override def leafTransferRate: Double = 50d

  override def connectionRadius: Float = 8f

  override def storageType: PowerStorageNodeType = PowerStorageNodeType.PRODUCER

  override def func_191420_l(): Boolean = false

  override def getFieldCount: Int = 0

  override def getField(id: Int): Int = 0

  override def setField(id: Int, value: Int): Unit = {}

  override def defaultBattery: IBattery = null
}
