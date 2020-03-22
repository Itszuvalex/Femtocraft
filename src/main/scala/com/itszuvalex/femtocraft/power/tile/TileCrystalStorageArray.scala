package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.power.node.PowerLeafNode
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.storage.{IBattery, IItemStorage, ItemStorageArray, PowerBattery}
import com.itszuvalex.itszulib.api.wrappers.IItemStack
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.TileInventory

/**
  * Created by Chris on 1/9/2017.
  */
object TileCrystalStorageArray {
  val STORAGE_MULTIPLIER = 2d
}

class TileCrystalStorageArray extends TileEntityBase with TileInventory with PowerLeafNode {
  val istorage = storage // need to rename due to naming conflict

  override def serverUpdate(): Unit = {
    super.serverUpdate()

    storage.flatMap(_.capabilityOption(Capabilities.ITEM_POWER_CRYSTAL, null)).foreach { c =>
      val power = Math.min(c.battery.storage, c.getTransferRate())
      battery.storage = Math.min(battery.maxStorage, battery.storage + power)
      c.battery.storage = Math.max(0, c.battery.storage - power)
    }
  }

  override def getMod: AnyRef = Femtocraft

  override def hasGUI: Boolean = true

  override def getGuiID: Int = GuiIDs.TileCrystalStorageArrayID

  override def defaultStorage: IItemStorage = new ItemStorageArray(6) {
    override def canInsert(i: Int, stack: IItemStack): Boolean = {
      stack.isEmpty || stack.hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)
    }
  }

  override def powerStorageNodeType: PowerStorageNodeType = PowerStorageNodeType.STORAGE

  override def powerStorageTransferRate: Double = 50d

  override def hasDescription: Boolean = true

  override def connectionRadius: Float = 8f

  override def getFieldCount: Int = 0

  override def getField(id: Int): Int = 0

  override def setField(id: Int, value: Int): Unit = {}

  override def defaultBattery: IBattery = new PowerBattery(0) {
    override def maxStorage: Double = {
      istorage.flatMap(_.capabilityOption(Capabilities.ITEM_POWER_CRYSTAL, null)).foldLeft(0d)((sum, crystal) => sum + crystal.battery.maxStorage) * TileCrystalStorageArray.STORAGE_MULTIPLIER
    }
  }
}
