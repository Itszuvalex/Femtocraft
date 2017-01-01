package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.industry.tile.TileNaniteInfuser
import com.itszuvalex.itszulib.container.ContainerInv
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack

class ContainerNaniteInfuser(player: EntityPlayer, inv: InventoryPlayer, tile: TileNaniteInfuser) extends ContainerInv[TileNaniteInfuser](player, tile, 0, 1, true) {
  var powerMaxlast : Double = _
  var powerCurrentLast: Double = _

  override def eligibleForInput(item: ItemStack): Boolean = true
}
