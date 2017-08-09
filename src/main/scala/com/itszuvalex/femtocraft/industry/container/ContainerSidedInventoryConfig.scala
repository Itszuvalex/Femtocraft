package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.itszulib.container.ContainerBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity

class ContainerSidedInventoryConfig extends ContainerBase(GuiIDs.TileSidedInventoryConfigID, false) {

  override def canInteractWith(playerIn: EntityPlayer): Boolean = true
}
