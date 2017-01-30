package com.itszuvalex.femtocraft.power.container

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.power.PowerNetwork
import com.itszuvalex.itszulib.container.ContainerBase
import com.itszuvalex.itszulib.container.sync.{SyncDouble, SyncInt}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity

/**
  * Created by Christopher Harris (Itszuvalex) on 1/27/17.
  */
class ContainerPowerNetwork(tile: TileEntity, registerSyncs: Boolean) extends ContainerBase(GuiIDs.TilePowerNetworkID, registerSyncs) {
  var producerCount: Int = 0
  var consumerCount: Int = 0
  var storageCount : Int = 0

  var currentGen      : Double = 0d
  var currentDrain    : Double = 0d
  var lastNetworkDelta: Double = 0d
  var networkAvg      : Double = 0d

  var storageDelta       : Double = 0d
  var networkStored      : Double = 0d
  var networkStorage     : Double = 0d
  var networkTotalStored : Double = 0d
  var networkTotalStorage: Double = 0d

  addSync(new SyncInt(GuiID, () => getNetwork.map(_.countProducers).getOrElse(0), producerCount = _))
  addSync(new SyncInt(GuiID, () => getNetwork.map(_.countConsumer).getOrElse(0), consumerCount = _))
  addSync(new SyncInt(GuiID, () => getNetwork.map(_.countStorage).getOrElse(0), storageCount = _))

  addSync(new SyncDouble(GuiID, () => getNetwork.map(_.powerProducedLastTick).getOrElse(0d), currentGen = _))
  addSync(new SyncDouble(GuiID, () => getNetwork.map(_.powerConsumedLastTick).getOrElse(0d), currentDrain = _))
  addSync(new SyncDouble(GuiID, () => getNetwork.map(_.lastTickNetworkDelta).getOrElse(0d), lastNetworkDelta = _))
  addSync(new SyncDouble(GuiID, () => getNetwork.map(_.averagePowerTrend).getOrElse(0d), networkAvg = _))

  addSync(new SyncDouble(GuiID, () => getNetwork.map(_.powerStorageDelta).getOrElse(0d), storageDelta = _))
  addSync(new SyncDouble(GuiID, () => getNetwork.map(_.dedicatedPowerStored).getOrElse(0d), networkStored = _))
  addSync(new SyncDouble(GuiID, () => getNetwork.map(_.dedicatedPowerStorage).getOrElse(0d), networkStorage = _))
  addSync(new SyncDouble(GuiID, () => getNetwork.map(_.totalPowerStored).getOrElse(0d), networkTotalStored = _))
  addSync(new SyncDouble(GuiID, () => getNetwork.map(_.totalPowerStorage).getOrElse(0d), networkTotalStorage = _))

  def getNetwork: Option[PowerNetwork] = {
    if (tile.hasCapability(Capabilities.TILE_POWER_NODE, null)) {
      Option(tile.getCapability(Capabilities.TILE_POWER_NODE, null).getNetwork)
    }
    else if (tile.hasCapability(Capabilities.TILE_POWER_LEAF_NODE, null)) {
      tile.getCapability(Capabilities.TILE_POWER_LEAF_NODE, null).getParent.getTileEntity(false).withFilter(_.hasCapability(Capabilities.TILE_POWER_NODE, null)).map(_.getCapability(Capabilities.TILE_POWER_NODE, null).getNetwork)
    }
    else None
  }

  override def canInteractWith(playerIn: EntityPlayer): Boolean = true
}
