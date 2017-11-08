package com.itszuvalex.femtocraft.logistics.container

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.logistics.tile.TileConduit
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.container.sync.SyncItemStorageItemStack
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing

/**
  * Created by Alex on 18.08.2016.
  */
class ContainerConduit(player: EntityPlayer, inv: InventoryPlayer, tile: TileConduit, shouldSync: Boolean) extends ContainerInv[TileConduit](player, tile, 0, 1, GuiIDs.TileConduitID, shouldSync) {

  if (shouldSync) {
    EnumFacing.VALUES.foreach { f =>
      val storage = tile.conduit.connectionStorage(f.getIndex)
      storage.indices.foreach { i =>
        addSync(new SyncItemStorageItemStack(GuiID, storage, i))
      }
    }

    addPlayerInventorySlots(inv, 5, 76)
  }

  override def eligibleForInput(item: ItemStack): Boolean = item == null || item.isEmpty || item.hasCapability(Capabilities.ITEM_CONNECTION_PROVIDER, null)
}
