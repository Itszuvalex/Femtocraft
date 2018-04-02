package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.itszulib.container.ContainerBase
import com.itszuvalex.itszulib.container.sync.{SyncEnumAutomaticIOArray, SyncStringArray}
import com.itszuvalex.itszulib.core.EnumAutomaticIO
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity

class ContainerSidedFluidConfig(tile: TileEntity) extends ContainerBase(GuiIDs.TileSidedFluidConfigID, false) {
  addSync(new SyncStringArray(GuiID, () => tile.getCapability(Capabilities.FLUID_STORAGE_CONFIGURABLE, null).storageSegments.clone(),
    (a: Array[String]) => {
      val segments = tile.getCapability(Capabilities.FLUID_STORAGE_CONFIGURABLE, null).storageSegments
      segments.indices.foreach(i => segments(i) = a(i))
    }
  ))

  addSync(new SyncEnumAutomaticIOArray(GuiID, () => tile.getCapability(Capabilities.FLUID_STORAGE_CONFIGURABLE, null).automaticIO.clone(),
    (a: Array[EnumAutomaticIO]) => {
      val segments = tile.getCapability(Capabilities.FLUID_STORAGE_CONFIGURABLE, null).automaticIO
      segments.indices.foreach(i => segments(i) = a(i))
    }
  ))

  override def canInteractWith(playerIn: EntityPlayer): Boolean = true
}
