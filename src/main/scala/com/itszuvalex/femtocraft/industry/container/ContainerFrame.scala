package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.industry.tile.TileFrame
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.container.ContainerInv
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack

/**
  * Created by Christopher on 9/21/2015.
  */
class ContainerFrame(player: EntityPlayer, inv: InventoryPlayer, tile: TileFrame) extends ContainerInv[TileFrame](player, tile, 0, 0) {


  (0 until 9).foreach { i =>
    addSlotToContainer(new Slot(tile, i, 8 + 18 * i, 62))
                      }

  addPlayerInventorySlots(inv)


  override def detectAndSendChanges(): Unit = {
    super.detectAndSendChanges()
    if (tile.isBuilding ) {
      player.closeScreen()
      player.openGui(Femtocraft, GuiIDs.TileFrameConstructingGuiID, tile.getWorldObj, tile.xCoord, tile.yCoord, tile.zCoord)
    }
  }

  override def canInteractWith(p_75145_1_ : EntityPlayer): Boolean = true

  override def eligibleForInput(item: ItemStack): Boolean = false
}
