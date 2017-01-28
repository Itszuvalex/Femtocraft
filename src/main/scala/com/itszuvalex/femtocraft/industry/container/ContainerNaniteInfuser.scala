package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.industry.tile.TileNaniteInfuser
import com.itszuvalex.itszulib.container.ContainerInv
import com.itszuvalex.itszulib.container.sync.SyncDouble
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing

class ContainerNaniteInfuser(player: EntityPlayer, inv: InventoryPlayer, tile: TileNaniteInfuser) extends ContainerInv[TileNaniteInfuser](player, tile, 0, 1, GuiIDs.TileNaniteInfuserID, true) {
  addSync(new SyncDouble(GuiID, () => tile.getCapability(Capabilities.POWER_STORAGE, EnumFacing.UP).storage, (a: Double) => tile.getCapability(Capabilities.POWER_STORAGE, EnumFacing.UP).storage = a))
  addSync(new SyncDouble(GuiID, () => tile.getCapability(Capabilities.POWER_STORAGE, EnumFacing.UP).maxStorage, (a: Double) => tile.getCapability(Capabilities.POWER_STORAGE, EnumFacing.UP).maxStorage = a))

  override def eligibleForInput(item: ItemStack): Boolean = true
}
