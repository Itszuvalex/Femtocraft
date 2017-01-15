package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.power.node.PowerLeafNode
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStorageArray}
import com.itszuvalex.itszulib.api.wrappers.{IBattery, PowerBattery}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.TileInventory
import net.minecraft.nbt.NBTTagCompound

object TileCrystalHeatExchanger {
  val POWER_STORAGE        = 10000
  val CHARGING_MULTIPLIER  = 10d
  val BURN_TIME_MULTIPLIER = .5d

  val FUEL_INDEX    = 0
  val CRYSTAL_INDEX = 1

  val BURN_TIME_KEY = "Burn"
}

class TileCrystalHeatExchanger extends TileEntityBase with TileInventory with PowerLeafNode {
  var burnTime = 0

  override def serverUpdate(): Unit = {
    super.serverUpdate()

    if (burnTime > 0) {
      Option(storage(TileCrystalHeatExchanger.CRYSTAL_INDEX)).withFilter(!_.isEmpty).withFilter(_.toMinecraft.hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)).map(_.toMinecraft.getCapability(Capabilities.ITEM_POWER_CRYSTAL, null)).foreach { c =>
        val power = Math.min(c.battery.storage, c.getTransferRate())
        battery.storage = Math.min(battery.maxStorage, battery.storage + power)
        c.battery.storage = Math.max(0, c.battery.storage - power)
        val gen = powerPerTick
        battery.storage = Math.min(battery.maxStorage, battery.storage + gen)
      }

      burnTime -= 1
    }

    if (burnTime <= 0 && Option(storage(TileCrystalHeatExchanger.CRYSTAL_INDEX)).withFilter(!_.isEmpty).map(_.hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)).getOrElse(false)) {
      Option(storage(TileCrystalHeatExchanger.FUEL_INDEX)).withFilter(!_.isEmpty).withFilter(_.toMinecraft.hasCapability(com.itszuvalex.itszulib.api.Capabilities.ITEM_BURNABLE, null)).map(_.toMinecraft.getCapability(com.itszuvalex.itszulib.api.Capabilities.ITEM_BURNABLE, null)).foreach { f =>
        burnTime = (f.getBurnTime * TileCrystalHeatExchanger.BURN_TIME_MULTIPLIER).toInt
        storage.split(TileCrystalHeatExchanger.FUEL_INDEX, 1)
      }
    }
  }

  def getBurnTime: Int = burnTime

  def setBurnTime(i: Int): Unit = burnTime = i

  def powerPerTick: Double = Option(storage(TileCrystalHeatExchanger.CRYSTAL_INDEX)).withFilter(!_.isEmpty).withFilter(_.toMinecraft.hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)).map(_.toMinecraft.getCapability(Capabilities.ITEM_POWER_CRYSTAL, null)).map(_.getPassiveGen() * TileCrystalHeatExchanger.CHARGING_MULTIPLIER).getOrElse(0d)

  override def getMod: AnyRef = Femtocraft

  override def hasGUI: Boolean = true

  override def getGuiID: Int = GuiIDs.TileCrystalHeatExchangerID

  override def defaultStorage: IItemStorage = new ItemStorageArray(2)

  override def hasDescription: Boolean = true

  override def leafTransferRate: Double = 50d

  override def connectionRadius: Float = 8f

  override def storageType: PowerStorageNodeType = PowerStorageNodeType.PRODUCER

  override def func_191420_l(): Boolean = false

  override def getFieldCount: Int = 0

  override def getField(id: Int): Int = 0

  override def setField(id: Int, value: Int): Unit = {}

  override def readFromNBT(par1nbtTagCompound: NBTTagCompound): Unit = {
    super.readFromNBT(par1nbtTagCompound)
    burnTime = par1nbtTagCompound.getInteger(TileCrystalHeatExchanger.BURN_TIME_KEY)
  }

  override def writeToNBT(par1nbtTagCompound: NBTTagCompound): NBTTagCompound = {
    val ret = super.writeToNBT(par1nbtTagCompound)
    par1nbtTagCompound.setInteger(TileCrystalHeatExchanger.BURN_TIME_KEY, burnTime)
    ret
  }

  override def defaultBattery: IBattery = new PowerBattery(TileCrystalHeatExchanger.POWER_STORAGE)
}
