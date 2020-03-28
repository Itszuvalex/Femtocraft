package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.{GuiIDs, Resources}
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.gui.GuiPanelTexture

object GuiTabInventorySideConfig {
  val SideConfigTexLoc = Resources.TexGui("tabsideconfig.png")

  def addToGuiTabBar(bar: GuiTabBar, tile: ITileEntity): Unit = {
    bar.addTab(new GuiTabInventorySideConfig("Inventory Config", tile, bar.getActiveGuiID))
  }
}

class GuiTabInventorySideConfig(text: String,
                                tile: ITileEntity,
                                activeGuiID: () => Int)
  extends GuiTab(text, new GuiPanelTexture(2, 2, 18, 18, GuiTabInventorySideConfig.SideConfigTexLoc), tile, GuiIDs.TileSidedInventoryConfigID, activeGuiID)