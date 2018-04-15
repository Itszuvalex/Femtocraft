package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.{GuiIDs, Resources}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.gui.GuiPanelTexture

object GuiTabNaniteSideConfig {
  val SideConfigTexLoc = Resources.TexGui("tabnanites.png")

  def addToGuiTabBar(bar: GuiTabBar, tile: TileEntityBase): Unit = {
    bar.addTab(new GuiTabNaniteSideConfig("Nanite Config", tile, bar.getActiveGuiID))
  }
}

class GuiTabNaniteSideConfig(text: String,
  tile: TileEntityBase,
  activeGuiID: () => Int)
  extends GuiTab(text, new GuiPanelTexture(2, 2, 18, 18, GuiTabNaniteSideConfig.SideConfigTexLoc), tile, GuiIDs.TileSidedNaniteConfigID, activeGuiID)