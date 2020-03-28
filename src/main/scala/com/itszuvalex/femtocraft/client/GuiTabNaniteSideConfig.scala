package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.{GuiIDs, Resources}
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.gui.GuiPanelTexture

object GuiTabNaniteSideConfig {
  val SideConfigTexLoc = Resources.TexGui("tabnanites.png")

  def addToGuiTabBar(bar: GuiTabBar, tile: ITileEntity): Unit = {
    bar.addTab(new GuiTabNaniteSideConfig("Nanite Config", tile, bar.getActiveGuiID))
  }
}

class GuiTabNaniteSideConfig(text: String,
                             tile: ITileEntity,
                             activeGuiID: () => Int)
  extends GuiTab(text, new GuiPanelTexture(2, 2, 18, 18, GuiTabNaniteSideConfig.SideConfigTexLoc), tile, GuiIDs.TileSidedNaniteConfigID, activeGuiID)