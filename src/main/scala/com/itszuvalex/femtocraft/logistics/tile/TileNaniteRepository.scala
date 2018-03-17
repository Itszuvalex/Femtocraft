package com.itszuvalex.femtocraft.logistics.tile

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.nanite.{INanite, NaniteTank}
import com.itszuvalex.femtocraft.logistics.tile.TileNaniteRepository.TICKS_NBT
import com.itszuvalex.femtocraft.nanite.TileNaniteStorage
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.core.TileEntityBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

object TileNaniteRepository {
  val REPOSITORY_VOLUME = 250
  val TICKS_NBT         = "Ticks"
}

class TileNaniteRepository extends TileEntityBase with TileNaniteStorage {
  var ticks = 0

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T =
    (capability, facing) match {
      case (cap, _) if cap == Capabilities.NANITE_STORAGE_TANK => naniteStorageTank.asInstanceOf[T]
      case _ => super.getCapability(capability, facing)
    }

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean =
    (capability, facing) match {
      case (cap, _) if cap == Capabilities.NANITE_STORAGE_TANK => true
      case _ => super.hasCapability(capability, facing)
    }

  override def defaultStorageTank: NaniteTank = new NaniteTank(TileNaniteRepository.REPOSITORY_VOLUME) {
    override def canFill(nanite: INanite, vol: Int): Boolean = {
      nanitesInTank.isEmpty || containsNanite(nanite)
    }
  }

  override def serverUpdate(): Unit = {
    super.serverUpdate()
    //    ticks = TileEntityUtils.checkDoInputIO(this, sidedStorageConfig, ticks, TICKS_FOR_AUTOIO, AMT_FOR_AUTOIO)
    //    TileEntityUtils.checkDoOutputIO(this, sidedStorageConfig, ticks, AMT_FOR_AUTOIO)
  }

  override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(nbt)
    nbt.setInteger(TICKS_NBT, ticks)
    //    nbt.setTag(ITEM_SIDED_CONFIG_NBT, sidedStorageConfig.serializeNBT())
    nbt
  }

  override def readFromNBT(nbt: NBTTagCompound): Unit = {
    super.readFromNBT(nbt)
    ticks = nbt.getInteger(TICKS_NBT)
    //    if (nbt.hasKey(ITEM_SIDED_CONFIG_NBT))
    //      sidedStorageConfig.deserializeNBT(nbt.getCompoundTag(ITEM_SIDED_CONFIG_NBT))
  }

  override def getMod = Femtocraft

  override def hasGUI: Boolean = true

  override def getGuiID: Int = GuiIDs.TileNaniteRepositoryGuiID

  override def hasDescription: Boolean = false

}
