package com.itszuvalex.femtocraft.nanite.tile

import com.itszuvalex.femtocraft.api.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.power.node.PowerLeafNode
import com.itszuvalex.itszulib.api.core.Configurable
import com.itszuvalex.itszulib.api.storage.{IBattery, PowerBattery}
import com.itszuvalex.itszulib.core.TileEntityCoreTickable
import net.minecraft.nbt.NBTTagCompound

/**
  * Created by Christopher Harris (Itszuvalex) on 8/25/15.
  */
@Configurable object TileNaniteHiveSmall {
  @Configurable val HIVE_RADIUS    = 20f
                val INVENTORY_SIZE = 30
}

@Configurable class TileNaniteHiveSmall extends TileEntityCoreTickable with PowerLeafNode {

  override def defaultBattery: IBattery = new PowerBattery(5000)

  override def powerStorageNodeType: PowerStorageNodeType = PowerStorageNodeType.STORAGE

  override def powerStorageTransferRate: Double = 50d

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    setRenderUpdate()
  }

  override def hasDescription = true
}
