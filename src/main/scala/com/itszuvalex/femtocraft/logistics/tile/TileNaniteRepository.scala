package com.itszuvalex.femtocraft.logistics.tile

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.nanite.{INanite, NaniteTank}
import com.itszuvalex.femtocraft.logistics.tile.TileNaniteRepository._
import com.itszuvalex.femtocraft.nanite.{SidedNaniteStorageConfiguration, TileNaniteStorage}
import com.itszuvalex.femtocraft.util.TileEntityUtils
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.BlockFacing
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability

object TileNaniteRepository {
  val REPOSITORY_VOLUME       = 250
  val TICKS_FOR_AUTIO         = 5
  val VOL_PER_AUTOIO          = 1
  val TICKS_NBT               = "Ticks"
  val NANITE_SIDED_CONFIG_NBT = "NaniteConfig"
  val NANITE_TANK_KEY         = "Tank"
  val NONE_TANK_KEY           = "None"
}

class TileNaniteRepository extends TileEntityBase with TileNaniteStorage {
  private val sidedNaniteConfig = new SidedNaniteStorageConfiguration(_ => NANITE_TANK_KEY,
    Map(NONE_TANK_KEY -> null,
      NANITE_TANK_KEY -> naniteStorageTank),
    () => world.getBlockState(pos).getValue(BlockFacing.FACING))
  var ticks = 0

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T =
    (capability, facing) match {
      case (cap, _) if cap == Capabilities.NANITE_STORAGE_CONFIGURABLE => sidedNaniteConfig.asInstanceOf[T]
      case (cap, null) if cap == Capabilities.TILE_NANITE_STORAGE_TANK => naniteStorageTank.asInstanceOf[T]
      case (_, null) => null.asInstanceOf[T]
      case (cap, face) if cap == Capabilities.TILE_NANITE_STORAGE_TANK => sidedNaniteConfig.getStorageForGlobalFacing(face).asInstanceOf[T]
      case _ => super.getCapability(capability, facing)
    }

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean =
    (capability, facing) match {
      case (cap, _) if cap == Capabilities.TILE_NANITE_STORAGE_TANK => true
      case (cap, _) if cap == Capabilities.NANITE_STORAGE_CONFIGURABLE => true
      case _ => super.hasCapability(capability, facing)
    }

  override def defaultStorageTank: NaniteTank = new NaniteTank(REPOSITORY_VOLUME) {
    override def canFill(nanite: INanite, vol: Int): Boolean = {
      nanitesInTank.isEmpty || containsNanite(nanite)
    }
  }

  override def serverUpdate(): Unit = {
    super.serverUpdate()
    ticks = TileEntityUtils.incrementTicks(ticks, TICKS_FOR_AUTIO)
    TileEntityUtils.checkDoNaniteInputIO(this, sidedNaniteConfig, ticks, VOL_PER_AUTOIO)
    TileEntityUtils.checkDoNaniteOutputIO(this, sidedNaniteConfig, ticks, VOL_PER_AUTOIO)
  }

  override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(nbt)
    nbt.setInteger(TICKS_NBT, ticks)
    nbt.setTag(NANITE_SIDED_CONFIG_NBT, sidedNaniteConfig.serializeNBT())
    nbt
  }

  override def readFromNBT(nbt: NBTTagCompound): Unit = {
    super.readFromNBT(nbt)
    ticks = nbt.getInteger(TICKS_NBT)
    if (nbt.hasKey(NANITE_SIDED_CONFIG_NBT))
      sidedNaniteConfig.deserializeNBT(nbt.getCompoundTag(NANITE_SIDED_CONFIG_NBT))
  }

  override def getMod = Femtocraft

  override def hasGUI: Boolean = true

  override def getGuiID: Int = GuiIDs.TileNaniteRepositoryGuiID

  override def hasDescription: Boolean = false

}
