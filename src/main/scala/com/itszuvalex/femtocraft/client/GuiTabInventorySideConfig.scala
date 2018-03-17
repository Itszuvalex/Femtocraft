package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.{GuiIDs, Resources}
import com.itszuvalex.itszulib.core.TileEntityBase

object GuiTabInventorySideConfig {
  val SideConfigTexLoc = Resources.TexGui("tabsideconfig.png")

  def addToGuiTabBar(bar: GuiTabBar, tile: TileEntityBase): Unit = {
    bar.addTab(new GuiTabInventorySideConfig("Inventory Config", tile, bar.getActiveGuiID))
  }
}

class GuiTabInventorySideConfig(text: String,
  tile: TileEntityBase,
  activeGuiID: () => Int)
  extends GuiTab(text, new GuiPanelTexture(2, 2, 18, 18, GuiTabInventorySideConfig.SideConfigTexLoc), tile, GuiIDs.TileSidedInventoryConfigID, activeGuiID)