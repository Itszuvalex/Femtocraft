package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.itszulib.container.ContainerBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity

class ContainerSidedInventoryConfig(tile: TileEntity) extends ContainerBase(GuiIDs.TileSidedInventoryConfigID, false) {
  addSync(new SyncStringArray(GuiID, () => tile.getCapability(Capabilities.ITEM_STORAGE_CONFIGURABLE, null).storageSegments.clone(),
    (a: Array[String]) => {
      val segments = tile.getCapability(Capabilities.ITEM_STORAGE_CONFIGURABLE, null).storageSegments
      segments.indices.foreach(i => segments(i) = a(i))
    }
  ))

  override def canInteractWith(playerIn: EntityPlayer): Boolean = true
}
