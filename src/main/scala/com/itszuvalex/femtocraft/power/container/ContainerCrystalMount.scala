package com.itszuvalex.femtocraft.power.container

import com.itszuvalex.femtocraft.power.item.IPowerCrystal
import com.itszuvalex.femtocraft.power.tile.TileCrystalMount
import com.itszuvalex.itszulib.container.ContainerInv
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack

/**
  * Created by Christopher Harris (Itszuvalex) on 12/20/2015.
  */
class ContainerCrystalMount(parPlayer: EntityPlayer, inv: InventoryPlayer, te: TileCrystalMount) extends ContainerInv[TileCrystalMount](parPlayer, te, 0, 0) {
  addSlotToContainer(new Slot(te, 0, 80, 34))

  addPlayerInventorySlots(inv)

  override def eligibleForInput(item: ItemStack): Boolean = {
    item != null && item.getItem != null && item.getItem.isInstanceOf[IPowerCrystal]
  }
}
