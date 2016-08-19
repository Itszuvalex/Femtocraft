package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.industry.tile.TileNanoFurnace
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.gui.OutputSlot
import net.minecraft.entity.player.{InventoryPlayer, EntityPlayer}
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.FurnaceRecipes

/**
  * Created by Alex on 18.08.2016.
  */
class ContainerNanoFurnace(player: EntityPlayer, inv: InventoryPlayer, tile: TileNanoFurnace) extends ContainerInv[TileNanoFurnace](player, tile, 0, 1) {

  addSlotToContainer(new FilteredSlot(tile, 0, 45, 24))
  addSlotToContainer(new OutputSlot(tile, 1, 86, 24))

  addPlayerInventorySlots(inv, 5, 76)

  override def eligibleForInput(item: ItemStack): Boolean = FurnaceRecipes.instance().getSmeltingResult(item) != null

}
