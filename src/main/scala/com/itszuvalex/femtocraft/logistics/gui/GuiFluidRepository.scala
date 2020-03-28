package com.itszuvalex.femtocraft.logistics.gui

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.client.{FemtoGuiBase, GuiTabFluidSideConfig}
import com.itszuvalex.femtocraft.logistics.container.ContainerFluidRepository
import com.itszuvalex.femtocraft.logistics.tile.TileFluidRepository
import com.itszuvalex.itszulib.api.ItszuLibModules
import com.itszuvalex.itszulib.gui.GuiLabel
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}

class GuiFluidRepository(player: EntityPlayer, inv: InventoryPlayer, private val tile: TileFluidRepository)
  extends FemtoGuiBase(tile, new ContainerFluidRepository(player, inv, tile, false)) {
  GuiTabFluidSideConfig.addToGuiTabBar(tabBar, tile)

  val fRender = Minecraft.getMinecraft.fontRenderer

  addPlayerInventorySlots(inv)

  val tileName  = "Fluid Repository"
  val nameLabel = new GuiLabel(3, 3, fRender.getStringWidth(tileName), fRender.FONT_HEIGHT, () => tileName)
  add(nameLabel)

  val tank = new GuiFluidTank((panelWidth - 18) / 2, fRender.FONT_HEIGHT + 2, this, tile.getModule(ItszuLibModules.FLUID_STORAGE, null), 0, true)
  add(tank)

  override def GuiID: Int = GuiIDs.TileFluidRepositoryGuiID

}
