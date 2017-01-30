package com.itszuvalex.femtocraft.power.container

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.power.tile.TileCrystalMount
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.container.sync.SyncItemStorageItemStack
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack

/**
  * Created by Christopher Harris (Itszuvalex) on 12/20/2015.
  */
class ContainerCrystalMount(parPlayer: EntityPlayer, inv: InventoryPlayer, te: TileCrystalMount, addSyncs: Boolean = true) extends ContainerInv[TileCrystalMount](parPlayer, te, 0, 0, GuiIDs.TileCrystalMountGuiID, addSyncs) {

  if (addSyncs) {
    addSync(new SyncItemStorageItemStack(GuiID, te.storage, 0))
    addPlayerInventorySlots(inv)
  }

  override def eligibleForInput(item: ItemStack): Boolean = {
    item != null && item.getItem != null && item.hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)
  }
}
