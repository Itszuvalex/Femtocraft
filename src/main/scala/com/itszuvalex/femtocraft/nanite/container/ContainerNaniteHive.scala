package com.itszuvalex.femtocraft.nanite.container

import com.itszuvalex.femtocraft.nanite.tile.TileNaniteHiveSmall
import com.itszuvalex.itszulib.api.wrappers.Converter
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.container.sync.SyncItemStorageItemStack
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack

/**
  * Created by Christopher on 9/1/2015.
  */
object ContainerNaniteHive {
  val COOK_INDEX        = 0
  val POWER_BIG_INDEX   = 1
  val POWER_SMALL_INDEX = 2
}

class ContainerNaniteHive(player: EntityPlayer, inv: InventoryPlayer, tile: TileNaniteHiveSmall, shouldSync: Boolean) extends ContainerInv[TileNaniteHiveSmall](player, tile, 0, 0, shouldSync) {
//  addSync(new SyncDouble(() => inventory.battery.storage, (a: Double) => inventory.battery.storage = a))

  if (shouldSync) {
    val storage = Converter.IItemStorageFromIInventory(tile.indInventory)

    (0 until 3).foreach { i =>
      (0 until 9).foreach { j =>
        addSync(new SyncItemStorageItemStack(storage, j + i * 9))
      }
    }

    addSync(new SyncItemStorageItemStack(storage, 27))
    addSync(new SyncItemStorageItemStack(storage, 28))
    addSync(new SyncItemStorageItemStack(storage, 29))
    addPlayerInventorySlots(inv, 32, 83)
  }


  override def eligibleForInput(item: ItemStack): Boolean = false
}
