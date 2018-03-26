package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.{GuiIDs, Resources}
import com.itszuvalex.itszulib.core.TileEntityBase

object GuiTabFluidSideConfig {
  val SideConfigTexLoc = Resources.TexGui("tabsideconfig.png")

  def addToGuiTabBar(bar: GuiTabBar, tile: TileEntityBase): Unit = {
    bar.addTab(new GuiTabFluidSideConfig("Fluid Config", tile, bar.getActiveGuiID))
  }
}

class GuiTabFluidSideConfig(text: String,
  tile: TileEntityBase,
  activeGuiID: () => Int)
  extends GuiTab(text, new GuiPanelTexture(2, 2, 18, 18, GuiTabFluidSideConfig.SideConfigTexLoc), tile, GuiIDs.TileSidedFluidConfigID, activeGuiID)