package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.power.node.PowerLeafNode
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStorageArray}
import com.itszuvalex.itszulib.api.wrappers.{IBattery, IItemStack, PowerBattery}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.TileInventory

/**
  * Created by Chris on 1/8/2017.
  */
object TileCrystalChargingArray {
  val PASSIVE_GEN_MULTIPLIER = 2
  val POWER_STORAGE          = 10000
}

class TileCrystalChargingArray extends TileEntityBase with TileInventory with PowerLeafNode {
  override def serverUpdate(): Unit = {
    super.serverUpdate()

    storage.flatMap(_.capabilityOption(Capabilities.ITEM_POWER_CRYSTAL, null)).foreach { c =>
      val power = Math.min(c.battery.storage, c.getTransferRate())
      battery.storage = Math.min(battery.maxStorage, battery.storage + power)
      c.battery.storage = Math.max(0, c.battery.storage - power)
      val gen = c.getPassiveGen() * TileCrystalChargingArray.PASSIVE_GEN_MULTIPLIER
      battery.storage = Math.min(battery.maxStorage, battery.storage + gen)
                                                                                       }
  }

  def powerPerTick: Double = storage.flatMap(_.capabilityOption(Capabilities.ITEM_POWER_CRYSTAL, null)).foldLeft(0d)((s, c) => s + c.getPassiveGen() * TileCrystalChargingArray.PASSIVE_GEN_MULTIPLIER)

  override def getMod: AnyRef = Femtocraft

  override def hasGUI: Boolean = true

  override def getGuiID: Int = GuiIDs.TileCrystalChargingArrayID

  override def defaultStorage: IItemStorage = new ItemStorageArray(6) {
    override def canInsert(i: Int, stack: IItemStack): Boolean = {
      stack.isEmpty || stack.hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)
    }
  }

  override def hasDescription: Boolean = true

  override def leafTransferRate: Double = 50d

  override def connectionRadius: Float = 8f

  override def storageType: PowerStorageNodeType = PowerStorageNodeType.PRODUCER

  override def func_191420_l(): Boolean = false

  override def getFieldCount: Int = 0

  override def getField(id: Int): Int = 0

  override def setField(id: Int, value: Int): Unit = {}

  override def defaultBattery: IBattery = new PowerBattery(TileCrystalChargingArray.POWER_STORAGE)
}
