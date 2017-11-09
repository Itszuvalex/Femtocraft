package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.power.node.PowerLeafNode
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.Burnable
import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStorageArray}
import com.itszuvalex.itszulib.api.wrappers.{IBattery, IItemStack, PowerBattery}
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
  val BURN_MAX_KEY  = "BurnMax"
}

class TileCrystalHeatExchanger extends TileEntityBase with TileInventory with PowerLeafNode {
  var burnTime = 0
  var burnMax  = 0

  override def serverUpdate(): Unit = {
    super.serverUpdate()

    if (burnTime > 0) {
      storage(TileCrystalHeatExchanger.CRYSTAL_INDEX).capabilityOption(Capabilities.ITEM_POWER_CRYSTAL, null).foreach { c =>
        val power = Math.min(c.battery.storage, c.getTransferRate())
        battery.storage = Math.min(battery.maxStorage, battery.storage + power)
        c.battery.storage = Math.max(0, c.battery.storage - power)
        val gen = powerPerTick
        battery.storage = Math.min(battery.maxStorage, battery.storage + gen)
      }

      burnTime -= 1
    }

    if (burnTime <= 0 && battery.storage < battery.maxStorage && storage(TileCrystalHeatExchanger.CRYSTAL_INDEX).hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)) {
      Burnable.getBurnTime(storage(TileCrystalHeatExchanger.FUEL_INDEX).toMinecraft).foreach { f =>
        burnTime = (f * TileCrystalHeatExchanger.BURN_TIME_MULTIPLIER).toInt
        burnMax = burnTime
        storage.split(TileCrystalHeatExchanger.FUEL_INDEX, 1)
      }
    }
  }

  def getBurnMax: Int = burnMax

  def setBurnMax(i: Int): Unit = burnMax = i

  def getBurnTime: Int = burnTime

  def setBurnTime(i: Int): Unit = burnTime = i

  def powerPerTick: Double = storage(TileCrystalHeatExchanger.CRYSTAL_INDEX).capabilityOption(Capabilities.ITEM_POWER_CRYSTAL, null).map(_.getPassiveGen() * TileCrystalHeatExchanger.CHARGING_MULTIPLIER).getOrElse(0d)

  override def getMod: AnyRef = Femtocraft

  override def hasGUI: Boolean = true

  override def getGuiID: Int = GuiIDs.TileCrystalHeatExchangerID

  override def defaultStorage: IItemStorage = new ItemStorageArray(2) {
    override def canInsert(i: Int, stack: IItemStack): Boolean = {
      stack.isEmpty || (i match {
        case TileCrystalHeatExchanger.CRYSTAL_INDEX => stack.hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)
        case TileCrystalHeatExchanger.FUEL_INDEX => Burnable.getBurnTime(stack.toMinecraft).nonEmpty
        case _ => false
      })
    }
  }

  override def hasDescription: Boolean = true

  override def leafTransferRate: Double = 50d

  override def connectionRadius: Float = 8f

  override def storageType: PowerStorageNodeType = PowerStorageNodeType.PRODUCER

  override def getFieldCount: Int = 0

  override def getField(id: Int): Int = 0

  override def setField(id: Int, value: Int): Unit = {}

  override def readFromNBT(par1nbtTagCompound: NBTTagCompound): Unit = {
    super.readFromNBT(par1nbtTagCompound)
    burnTime = par1nbtTagCompound.getInteger(TileCrystalHeatExchanger.BURN_TIME_KEY)
    burnMax = par1nbtTagCompound.getInteger(TileCrystalHeatExchanger.BURN_MAX_KEY)
  }

  override def writeToNBT(par1nbtTagCompound: NBTTagCompound): NBTTagCompound = {
    val ret = super.writeToNBT(par1nbtTagCompound)
    par1nbtTagCompound.setInteger(TileCrystalHeatExchanger.BURN_TIME_KEY, burnTime)
    par1nbtTagCompound.setInteger(TileCrystalHeatExchanger.BURN_MAX_KEY, burnMax)
    ret
  }

  override def defaultBattery: IBattery = new PowerBattery(TileCrystalHeatExchanger.POWER_STORAGE)
}
