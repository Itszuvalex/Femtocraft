package com.itszuvalex.femtocraft.logistics.container

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.itszulib.api.ItszuLibCapabilities
import com.itszuvalex.itszulib.api.wrappers.IItemStack
import com.itszuvalex.itszulib.container.IItemStorageSyncBundle
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack

class ContainerNanoPack(parPlayer: EntityPlayer, inv: InventoryPlayer, nanoPack: IItemStack, shouldSync: Boolean) extends ContainerStorage(parPlayer, nanoPack.getCapability(ItszuLibCapabilities.ITEM_STORAGE, null), 0, 0, GuiIDs.ItemNanoPackID, shouldSync) {

  if (shouldSync) {
    new IItemStorageSyncBundle(GuiID, this, nanoPack.getCapability(ItszuLibCapabilities.ITEM_STORAGE, null), false)
    addPlayerInventorySlots(parPlayer.inventory)
  }

  override def eligibleForInput(item: ItemStack): Boolean = true
}
