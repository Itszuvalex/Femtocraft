package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.industry.tile.TileFrame
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.container.sync.SyncItemStorageItemStack
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack

/**
  * Created by Christopher on 9/21/2015.
  */
class ContainerFrame(player: EntityPlayer, inv: InventoryPlayer, tile: TileFrame, doAddSync: Boolean) extends ContainerInv[TileFrame](player, tile, 0, 0, GuiIDs.TileFrameMultiblockGuiID, doAddSync) {


  if (doAddSync) {
    (0 until 9).foreach { i =>
      addSync(new SyncItemStorageItemStack(GuiID, tile.storage, i))
    }

    addPlayerInventorySlots(inv)
  }

  override def detectAndSendChanges(): Unit = {
    super.detectAndSendChanges()
    tile.state.get match {
      case None =>
      case Some(s) =>
        if (s.isBuilding) {
          player.closeScreen()
          player.openGui(Femtocraft, GuiIDs.TileFrameConstructingGuiID, tile.getWorld, tile.getPos.getX, tile.getPos.getY, tile.getPos.getZ)
        }
    }
  }

  override def canInteractWith(p_75145_1_ : EntityPlayer): Boolean = true

  override def eligibleForInput(item: ItemStack): Boolean = false
}
