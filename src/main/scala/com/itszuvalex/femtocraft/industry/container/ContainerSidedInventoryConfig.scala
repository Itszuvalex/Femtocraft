package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.itszulib.api.ItszuLibModules
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.container.ContainerBase
import com.itszuvalex.itszulib.container.sync.{SyncEnumAutomaticIOArray, SyncStringArray}
import com.itszuvalex.itszulib.core.EnumAutomaticIO
import net.minecraft.entity.player.EntityPlayer

class ContainerSidedInventoryConfig(tile: ITileEntity) extends ContainerBase(GuiIDs.TileSidedInventoryConfigID, false) {
  addSync(new SyncStringArray(GuiID, () => tile.getModule(ItszuLibModules.ITEM_STORAGE_CONFIGURABLE, null).storageSegments.clone(),
                              (a: Array[String]) => {
                                val segments = tile.getModule(ItszuLibModules.ITEM_STORAGE_CONFIGURABLE, null).storageSegments
                                segments.indices.foreach(i => segments(i) = a(i))
                              }
                              ))

  addSync(new SyncEnumAutomaticIOArray(GuiID, () => tile.getModule(ItszuLibModules.ITEM_STORAGE_CONFIGURABLE, null).automaticIO.clone(),
                                       (a: Array[EnumAutomaticIO]) => {
                                         val segments = tile.getModule(ItszuLibModules.ITEM_STORAGE_CONFIGURABLE, null).automaticIO
                                         segments.indices.foreach(i => segments(i) = a(i))
                                       }
                                       ))

  override def canInteractWith(playerIn: EntityPlayer): Boolean = true
}
