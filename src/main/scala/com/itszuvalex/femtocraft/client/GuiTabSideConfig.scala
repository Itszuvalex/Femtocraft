package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.{GuiIDs, Resources}
import com.itszuvalex.itszulib.core.TileEntityBase

object GuiTabSideConfig {
  val SideConfigTexLoc = Resources.TexGui("tabsideconfig.png")

  def addToGuiTabBar(bar: GuiTabBar, tile: TileEntityBase): Unit = {
    bar.addTab(new GuiTabSideConfig("Side Config", tile, bar.getActiveGuiID))
  }
}

class GuiTabSideConfig (text: String,
                        tile: TileEntityBase,
                        activeGuiID: () => Int)
  extends GuiTab(text, new GuiPanelTexture(2, 2, 18, 18, GuiTabSideConfig.SideConfigTexLoc), tile, GuiIDs.TileSidedInventoryConfigID, activeGuiID)